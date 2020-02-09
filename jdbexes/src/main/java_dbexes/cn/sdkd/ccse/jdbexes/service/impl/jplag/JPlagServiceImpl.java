package cn.sdkd.ccse.jdbexes.service.impl.jplag;

import cn.sdkd.ccse.commons.utils.FileUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.sdkd.ccse.jdbexes.service.impl.jplag.Configuration.SIM_THRESHOLD;
import static cn.sdkd.ccse.jdbexes.service.impl.jplag.Configuration.SIM_THRESHOLD_MIN;

@Service("JPlagService")
public class JPlagServiceImpl implements IJPlagService {
    private static final Logger logger = LoggerFactory.getLogger(JPlagServiceImpl.class);
    protected GSTiling gSTiling;
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
    private String submitTempDir;
    private Properties props = new Properties();
    private Program program;
    // submissions <实验编号,<名称，作业>>
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Submission>> submissions;

    public JPlagServiceImpl() throws ExitException {
        initProperties();
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
        this.submitTempDir = props.getProperty("submitTempDir");
        this.poolSize = Integer.parseInt(props.getProperty("jplgPoolSize"));

        this.threadPoolExecutor = new ThreadPoolExecutor(this.poolSize, this.poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        submissions = new ConcurrentHashMap<String, ConcurrentHashMap<String, Submission>>();

        String[] paras = {
                "-l", "c/c++",
                "-s", this.submitFilesRootDir,
                "-clustertype", "min"
        };
        jplag.options.Options options = new CommandLineOptions(paras, null);
        this.program = new Program(options);
        this.gSTiling = new GSTiling(this.program);
    }

    @Override
    public Submission getSubmission(String expno, String name) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        if (expSubmissions != null) {
            return expSubmissions.get(name);
        }
        return null;
    }

    @Override
    public void putSubmission(String expno, String name, Submission submission) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        if (expSubmissions == null) {
            expSubmissions = new ConcurrentHashMap<String, Submission>();
            submissions.put(expno, expSubmissions);
        }
        expSubmissions.put(name, submission);
    }

    @Override
    public void updateSubmission(String expno, String sno, String sname) throws ExitException {
        File f = new File(this.submitFilesRootDir + "/" + sno + "_" + sname + "/" + expno + "/");
        Submission submission = new Submission(sno + "_" + sname, f, false, this.program, this.program.get_language());
        submission.parse();

        this.putSubmission(expno + "", sno + "_" + sname, submission);
    }

    @Override
    public boolean compareSubmission(String expno, String sno, String sname) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        Submission submission = expSubmissions.get(sno + "_" + sname);
        SortedVector<AllMatches> avgmatches = new SortedVector<AllMatches>(new AllMatches.AvgComparator());
        for (Map.Entry<String, Submission> entry : expSubmissions.entrySet()) {

            if (entry.getKey().equalsIgnoreCase(submission.name)) {
                continue;
            }
            if (entry.getValue().struct != null) {
                AllMatches match = this.gSTiling.compare(entry.getValue(), submission);
                if (match.percent() >= SIM_THRESHOLD_MIN) {
                    avgmatches.insert(match);
                }
            } else {
                logger.info(entry.getValue().name + " 未提交文件或解析错误.");
            }
        }
        for (AllMatches m : avgmatches) {
            logger.info("expno:" + expno + "_" + m.subA.name + " - " + m.subB.name + " : " + m.percent() + ", a:" + m.percentA() + ", b:" + m.percentB() + ", max:" + m.percentMaxAB() + ", min:" + m.percentMinAB());
        }
        if (avgmatches.size() <= 0) {
            return true;
        } else {
            float simPassedThreshold = 90;
            return (avgmatches.get(0).percent() < simPassedThreshold);
        }
    }

    @Override
    public float compareSubmission(Submission a, Submission b) {
        AllMatches match;
        try {
            match = this.gSTiling.compare(a, b);
            return match.percent();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return -1;
    }

    @Override
    public ConcurrentHashMap<String, Submission> getSubmission(String expno) {
        return submissions.get(expno);
    }

    @Override
    public void compare(String expno, Submission submission) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        SortedVector<AllMatches> avgmatches = new SortedVector<AllMatches>(new AllMatches.AvgComparator());
        for (Map.Entry<String, Submission> entry : expSubmissions.entrySet()) {
            if (entry.getValue().struct != null) {
                AllMatches match = this.gSTiling.compare(entry.getValue(), submission);
                if (match.percent() >= SIM_THRESHOLD_MIN) {
                    avgmatches.insert(match);
                }
            } else {
                logger.info(entry.getValue().name + " 未提交文件或解析错误.");
            }
        }
        for (AllMatches m : avgmatches) {
            logger.info("expno:" + expno + "_" + m.subA.name + " - " + m.subB.name + " : " + m.percent() + ", a:" + m.percentA() + ", b:" + m.percentB() + ", max:" + m.percentMaxAB() + ", min:" + m.percentMinAB());
        }
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
        // 检查 Student 和 Experiment 是否存在，不存在则创建
        createStudentAndExperimentOnNeo4JIfNotExists(stuno, expno);
        // 创建一次测试
        ExperimentStuTest test = generateTest(stuno, expno);
        // 根据 test 产生文件，解析并提交相似度比较任务
        initOneTest(test);
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

    /**
     * 刷新相似度检查结果
     * 仅对 Neo4J 进行查询，不新建测试
     * @param stuno
     * @param expno
     */
    @Override
    public void refreshSimStatus(Long stuno, Long expno) {
        ExperimentStuTest test = experimentStuTestService.findLatestByUserExperiment(stuno, expno);
        if (test == null) {
            logger.warn("学生实验(" + stuno + ", " + expno + ")：未查询到相应 ExperimentStuTest");
            experimentStuService.updateSimStatus(stuno, expno, 1, "未查询到相应 ExperimentStuTest");
//            submitJob(stuno, expno);
            return;
        }
        long experiment_stu_test_no = test.getExperiment_stu_test_no();
        Assignment assignment = neo4jService.findAssignmentByExperimentStuTestNo(experiment_stu_test_no);
        if (assignment == null) {
            experimentStuService.updateSimStatus(stuno, expno, 1, "未查询到相应 Assignment");
            logger.warn("学生实验(" + stuno + ", " + expno + ")：未查询到相应 Assignment");
//            initOneTest(test);
            return;
        }

        List<Student> lss = this.studentRepository.findBySimValueAssignmentid(SIM_THRESHOLD, assignment.getId());
        // 若相似度超过阈值的学生个数大于0，则状态是3，否则状态是0
        if (lss.size() > 0) {
            experimentStuService.updateSimStatus(stuno, expno, 3, Configuration.getSimDesc(lss.size(), SIM_THRESHOLD));
        } else {
            experimentStuService.updateSimStatus(stuno, expno, 0, Configuration.getSimDesc(lss.size(), SIM_THRESHOLD));
        }
//        initOneTest(test);
    }

    private void initProperties() {
        // 读取配置文件
        Resource resource = new ClassPathResource("/config/checkmission.properties");

        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    /*构造函数完成后开始:初始化已有作业*/
    private void initSubmissions() throws ExitException {
        logger.info("处理未计算相似度的作业");
        /*查询未计算相似度的作业*/
        List<ExperimentStuTest> lest = experimentStuTestService.selectListUnCompare();

        for (ExperimentStuTest est : lest) {
            initOneTest(est);
        }

        for (Map.Entry<String, ConcurrentHashMap<String, Submission>> entry : this.submissions.entrySet()) {
            logger.info("* 实验" + entry.getKey() + ": " + entry.getValue().size() + "份.");
        }
    }

    /**
     * 初始化相似度检查
     * @param test
     */
    private void initOneTest(ExperimentStuTest test) {
        User user = userService.selectById(test.getStuno());
        logger.info("正在处理作业: (用户: " + user.getLoginName() + "-" + user.getName() + ", 实验编号: " + test.getExpno() + ").");

        Path path = getTempPath(user, test);
        // 1. 产生临时文件
        generateTestFile(test, path);
        // 2. 检查并提交相似度比较任务
        submitJPlagJob(user, test, path);
        // 3. 删除临时文件夹
        FileUtils.removeDir(path.toString());

    }

    /**
     * 根据用户名和测试生成临时文件名
     * @param user 用户
     * @param test 测试
     * @return 创建好的临时文件路径
     */
    private Path getTempPath(User user, ExperimentStuTest test) {
        String filename = "/test-" + user.getLoginName() + "_" + user.getName() + "_" + test.getExpno() + "_" + test.getExperiment_stu_test_no();
        Path path = Paths.get(this.submitTempDir, filename);
        // Create if not exists
        File file = path.toFile();
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
        }
        return path;
    }

    /**
     * 将数据库中保存的文件写入指定文件夹
     * @param test 测试
     * @param path 临时文件路径
     */
    private void generateTestFile(ExperimentStuTest test, Path path) {
        // 获取该次测试用到的代码文件
        List<ExperimentStuTestFiles> estfList = experimentStuTestFilesService.selectListByTestno(test.getExperiment_stu_test_no().longValue());
        for (ExperimentStuTestFiles estf : estfList) {
            ExperimentFilesStu file = experimentFilesStuService.selectById(estf.getExperiment_files_stu_no());
            if (file != null) {
                try {
                    generateTestFile(path, test, file);
                } catch (IOException e) {
                    experimentStuService.updateSimStatus(test.getStuno().longValue(), test.getExpno().longValue(), 1, e.getMessage());
                    logger.error(e.getMessage());
                }
            } else {
                /*TODO: 如果为null，是否需要创建？*/
                logger.error("estf is null : " + estf.getExperiment_files_stu_no());
            }
        }
    }

    private void generateTestFile(Path path, ExperimentStuTest test, ExperimentFilesStu file) throws IOException {
        OutputStreamWriter op = null;
        try {
            // 写入相应目录
            String fileName = path.resolve(file.getFileno() + ".cpp").toString();
            op = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8);
            op.append(file.getFile_content());
            op.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (op != null) {
                op.close();
            }
        }
    }

    /**
     * 检查并提交相似度比较任务 jPlagJob
     * @param user 用户
     * @param test 测试
     * @param path 临时文件路径
     */
    private void submitJPlagJob(User user, ExperimentStuTest test, Path path) {
        if (Files.notExists(path)) {
            return;
        }

        Submission submission = createSubmission(user, test, path);

        // 尝试解析代码，解析错误则不创建任务
        if (parseSubmission(test, submission) == false) {
            return;
        }

        // 否则，创建 jPlagJob 并提交
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(String.valueOf(test.getExpno()));
        if (expSubmissions == null) {
            expSubmissions = new ConcurrentHashMap<String, Submission>();
            submissions.put(test.getExpno().toString(), expSubmissions);
        }
        expSubmissions.put(submission.name, submission);

        JPlagJob jPlagJob = new JPlagJob(this, submission,
                test.getStuno().longValue(), test.getExpno().longValue(),
                this.experimentStuService,
                this.assignmentRepository, this.similarityRepository,
                this.studentRepository, this.experimentRepository
        );
        threadPoolExecutor.execute(jPlagJob);
    }

    private Submission createSubmission(User user, ExperimentStuTest test, Path path) {
        // key: 学生学号_姓名_测试编号
        SubmissionKey submissionKey = new SubmissionKey(user.getLoginName(), user.getName(), test.getExperiment_stu_test_no().longValue());
        Submission submission = new Submission(submissionKey.toString(), path.toFile(),
                false, this.program, this.program.get_language());

        return submission;
    }

    private boolean parseSubmission(ExperimentStuTest test, Submission submission) {
        try {
            submission.parse();
            if (submission.errors == true || submission.struct == null) {
                experimentStuService.updateSimStatus(test.getStuno().longValue(), test.getExpno().longValue(), 1, "解析错误");
                return false;
            }
        } catch (ExitException e) {
            experimentStuService.updateSimStatus(test.getStuno().longValue(), test.getExpno().longValue(), 1, e.getMessage());
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 检查 student 和 experiment 是否存在，若不存在则在 neo4j 中创建
     * @param stuno 学生编号
     * @param expno 实验编号
     */
    private void createStudentAndExperimentOnNeo4JIfNotExists(Long stuno, Long expno) {
        neo4jService.createStudentIfNotExists(userService.selectById(stuno));
        neo4jService.createExperimentIfNotExists(experimentService.selectById(expno));
    }

}

