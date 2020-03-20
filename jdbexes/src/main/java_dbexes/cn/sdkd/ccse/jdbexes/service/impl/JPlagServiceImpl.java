package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.jplagjob.Config;
import cn.sdkd.ccse.jdbexes.jplagjob.JPlagJob;
import cn.sdkd.ccse.jdbexes.jplagjob.SubmissionKey;
import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.*;
import com.wangzhixuan.model.User;
import com.wangzhixuan.service.IUserService;
import jplag.*;
import jplag.options.CommandLineOptions;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.sdkd.ccse.jdbexes.jplagjob.Config.SIM_THRESHOLD;

@Service
public class JPlagServiceImpl implements IJPlagService {
    private static final Logger logger = LoggerFactory.getLogger(JPlagServiceImpl.class);
    int poolSize;
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private IUserService userService;
    @Autowired
    private IExperimentStuService experimentStuService;
    @Autowired
    private IExperimentService experimentService;
    @Autowired
    private IExperimentStuTestService experimentStuTestService;
    @Autowired
    private IExperimentStuTestFilesService experimentStuTestFilesService;
    @Autowired
    private IExperimentFilesStuService experimentFilesStuService;
    @Autowired
    private IExperimentFilesService experimentFilesService;
    @Autowired
    private INeo4jService neo4jService;
    @Autowired
    private IAssignmentRepository assignmentRepository;
    @Autowired
    private ISimilarityRepository similarityRepository;
    @Autowired
    private IStudentRepository studentRepository;
    @Autowired
    private IExperimentRepository experimentRepository;
    private String submitFilesRootDir;

    private Map<Long, GSTiling> gsTilingMap;
    private Map<Long, Program> programMap;
    private ConcurrentHashMap<Long, ConcurrentHashMap<Long, Submission>> submissions; // <实验编号, <学生编号，作业>>

    public JPlagServiceImpl() throws IOException {
        Properties props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("/config/checkmission.properties"));
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
        this.poolSize = Integer.parseInt(props.getProperty("jplagPoolSize"));

        this.threadPoolExecutor = new ThreadPoolExecutor(this.poolSize, this.poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        submissions = new ConcurrentHashMap<>();

        gsTilingMap = new ConcurrentHashMap<>();
        programMap = new ConcurrentHashMap<>();
    }

    @Override
    public Map<SubmissionKey, Float> compareSubmission(Long expno, Long stuno, Submission submission) {
        Map<SubmissionKey, Float> simResultMap = new HashMap<>();
        for (Map.Entry<Long, Submission> entry : submissions.get(expno).entrySet()) {
            Submission submissionB = entry.getValue();
            SubmissionKey key = SubmissionKey.valueOf(submissionB.name);

            float sim = compareSubmission(expno, submission, submissionB);
            simResultMap.put(key, sim);
        }
        return simResultMap;
    }

    @Override
    public void monitorJob() {
        // 定时查看线程列表，是否有进程结束，如果结束则从队列中移除；若出错，则重启线程
        logger.warn("相似度任务线程池中线程数目：" + threadPoolExecutor.getPoolSize()
                + "，队列中等待执行的任务数目：" + threadPoolExecutor.getQueue().size()
                + "，已执行完成的任务数目：" + threadPoolExecutor.getCompletedTaskCount());
    }

    @Override
    public void submitJob(Long stuno, Long expno) {
        experimentStuService.updateStatusDesc(stuno, expno, Config.SIM_STATUS_NOT_YET, Config.SIM_DESC_NOT_YET);
        // 检查 Student 和 Experiment 是否存在，不存在则创建
        createStudentAndExperimentOnNeo4JIfNotExists(stuno, expno);
        // 创建一次测试
        ExperimentStuTest test = generateTest(stuno, expno);
        // 根据 test 产生文件，解析并提交相似度比较任务
        initOneTest(test);
    }

    @Override
    public void refreshSimStatus(Long stuno, Long expno) {
        experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_NOT_YET, Config.SIM_DESC_RUNNING);

        ExperimentStuTest test = experimentStuTestService.findLatestByUserExperiment(stuno, expno);
        if (test == null) {
            logger.warn("学生实验(" + stuno + ", " + expno + ")：未查询到相应 ExperimentStuTest");
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, "未查询到相应 ExperimentStuTest");
            return;
        }
        long experiment_stu_test_no = test.getExperiment_stu_test_no();
        Assignment assignment = neo4jService.findAssignmentByExperimentStuTestNo(experiment_stu_test_no);
        if (assignment == null) {
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, "未查询到相应 Assignment");
            logger.warn("学生实验(" + stuno + ", " + expno + ")：未查询到相应 Assignment");
            return;
        }

        List<Student> lss = this.studentRepository.findBySimValueAssignmentid(SIM_THRESHOLD, assignment.getId());
        // 若相似度超过阈值的学生个数大于0，则状态是3，否则状态是0
        if (lss.size() > 0) {
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_PLAGIARISM, Config.getSimDesc(lss.size(), SIM_THRESHOLD));
        } else {
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_NORMAL, Config.SIM_DESC_NORMAL);
        }
    }

    @Override
    public void putSubmission(Long stuno, Long expno, Submission submission) {
        ConcurrentHashMap<Long, Submission> expSubmissions = submissions.computeIfAbsent(expno, v -> new ConcurrentHashMap<>());
        expSubmissions.put(stuno, submission);
    }

    /**
     * 将数据库中保存的文件写入指定文件夹
     */
    @Override
    public void generateTestFiles(Long experiment_stu_test_no, String dir) throws IOException {
        logger.debug("write " + dir);
        Files.createDirectories(Paths.get(dir));
        List<ExperimentStuTestFiles> estfList = experimentStuTestFilesService.selectListByTestno(experiment_stu_test_no);
        for (ExperimentStuTestFiles estf : estfList) {
            ExperimentFilesStu file = experimentFilesStuService.selectById(estf.getExperiment_files_stu_no());
            String filename = experimentFilesService.selectById(estf.getFileno()).getSrcfilename();
            String path = FilenameUtils.concat(dir, FilenameUtils.getName(filename));

            try (OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(new File(path)), StandardCharsets.UTF_8)) {
                op.append(file.getFile_content());
                op.flush();
            }
        }
    }

    @Override
    public boolean parseSubmission(Long stuno, Long expno, Submission submission) throws ExitException {
        submission.parse();
        if (submission.errors || submission.struct == null) {
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, Config.SIM_DESC_SYNAX_ERR);
            return false;
        }
        return true;
    }

    @Override
    public String getTestFilePath(Long expno, SubmissionKey key) {
        return FilenameUtils.concat(
                FilenameUtils.concat(submitFilesRootDir, expno.toString()),
                key.toString()
        );
    }

    @Override
    public Submission generateSubmission(Long expno, SubmissionKey submissionKey, String path) throws ExitException {
        Program program = getProgram(expno);
        return new Submission(submissionKey.toString(), new File(path), false, program, program.get_language());
    }

    @Override
    public GSTiling getGSTiling(Long expno) throws ExitException {
        GSTiling gsTiling = gsTilingMap.get(expno);
        if (gsTiling == null) {
            gsTiling = new GSTiling(getProgram(expno));
            gsTilingMap.put(expno, gsTiling);
        }
        return gsTiling;
    }

    @Override
    public Program getProgram(Long expno) throws ExitException {
        Program program = programMap.get(expno);
        if (program == null) {
            String[] paras = {
                    "-l", "c/c++",
                    "-s", submitFilesRootDir + expno,
                    "-clustertype", "min"
            };
            program = new Program(new CommandLineOptions(paras, null));
            programMap.put(expno, program);
        }
        return program;
    }

    ExperimentStuTest generateTest(Long stuno, Long expno) {
        // 创建一次测试
        ExperimentStuTest test = new ExperimentStuTest();
        test.setStuno(stuno.intValue());
        test.setExpno(expno.intValue());
        // 增加测试记录，test 得到 ID 字段 experiment_stu_test_no
        experimentStuTestService.insert(test);
        // 根据最新的提交文件，产生最近一次测试记录
        experimentStuTestFilesService.insertLatestTestFiles(test.getExperiment_stu_test_no().longValue(), stuno, expno);
        return test;
    }

    private SubmissionKey generateSubmissionKey(ExperimentStuTest test) {
        long stuno = test.getStuno().longValue();
        long experiment_stu_test_no = test.getExperiment_stu_test_no().longValue();
        User user = userService.selectById(stuno);
        String loginName = user.getLoginName();
        String name = user.getName();

        return new SubmissionKey(loginName, name, experiment_stu_test_no);
    }

    /**
     * 检查 student 和 experiment 是否存在，若不存在则在 neo4j 中创建
     *
     * @param stuno 学生编号
     * @param expno 实验编号
     */
    private void createStudentAndExperimentOnNeo4JIfNotExists(Long stuno, Long expno) {
        neo4jService.createStudentIfNotExists(userService.selectById(stuno));
        neo4jService.createExperimentIfNotExists(experimentService.selectById(expno));
    }

    private float compareSubmission(Long expno, Submission a, Submission b) {
        try {
            GSTiling gsTiling = getGSTiling(expno);
            AllMatches match = gsTiling.compare(a, b);
            return match.percent();
        } catch (Exception e) {
            logger.error("比较失败(" + a.name + ", " + b.name + ")\n" + a.structSize + ", " + b.structSize, e);
        }
        return -1;
    }

    @PostConstruct
    private void initSubmissions() {

        logger.info("恢复提交列表");
        List<Long> experiments = experimentService.selectAll().stream().map(Experiment::getExpno).collect(Collectors.toList());
        for (Long expno : experiments) {
            restoreSubmissions(expno);
        }

        logger.info("处理未计算相似度的作业");
        List<ExperimentStuTest> lest = experimentStuTestService.selectListUnCompare();
        for (ExperimentStuTest est : lest) {
            initOneTest(est);
        }

        for (Map.Entry<Long, ConcurrentHashMap<Long, Submission>> entry : this.submissions.entrySet()) {
            logger.info("* 实验" + entry.getKey() + ": " + entry.getValue().size() + "份.");
        }
    }

    private void restoreSubmissions(Long expno) {
        List<ExperimentStuTest> tests = experimentStuTestService.findLatestByExpno(expno);
        for (ExperimentStuTest test : tests) {
            if (test.getTeststatus() != null && test.getTeststatus() == 1) {
                // 跳过先前解析不成功的
                continue;
            }
            SubmissionKey submissionKey = generateSubmissionKey(test);

            String path = getTestFilePath(expno, submissionKey);

            try {
                // 1. 生成文件
                if (!new File(path).exists()) {
                    generateTestFiles(submissionKey.getExperiment_stu_test_no(), path);
                }
                // 2. 创建提交
                Submission submission = generateSubmission(expno, submissionKey, path);
                // 3. 尝试解析，失败则标记 teststatus 为 1
                if (parseSubmission(test.getStuno().longValue(), test.getExpno().longValue(), submission)) {
                    putSubmission(test.getStuno().longValue(), expno, submission);
                } else {
                    test.setTeststatus(1);
                    experimentStuTestService.updateById(test);
                }
            } catch (Exception e) {
                if (e.getClass() == ExitException.class) {
                    return; // ignore
                }
                logger.warn("恢复提交失败 " + submissionKey, e);
            }
        }
    }

    private void initOneTest(ExperimentStuTest test) {
        long expno = test.getExpno().longValue();
        long stuno = test.getStuno().longValue();
        long experiment_stu_test_no = test.getExperiment_stu_test_no().longValue();
        User user = userService.selectById(stuno);
        String loginName = user.getLoginName();
        String name = user.getName();

        SubmissionKey submissionKey = new SubmissionKey(loginName, name, experiment_stu_test_no);
        JPlagJob jPlagJob = new JPlagJob(this, submissionKey, stuno, expno,
                this.experimentStuService, this.experimentStuTestService,
                this.assignmentRepository, this.similarityRepository,
                this.studentRepository, this.experimentRepository
        );
        threadPoolExecutor.execute(jPlagJob);
    }

}

