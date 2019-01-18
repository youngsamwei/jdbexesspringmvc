package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.commons.utils.FileUtils;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.service.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wangzhixuan.model.User;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;
import javafx.beans.property.SimpleListProperty;
import jplag.*;
import jplag.options.CommandLineOptions;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by sam on 2019/1/7.
 * 相似度计算任务
 */
@Service
public class JPlagServiceImpl implements IJPlagService {
    private static final Logger logger = LoggerFactory.getLogger(JPlagServiceImpl.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IExperimentStuTestService experimentStuTestService;
    @Autowired
    private IExperimentStuTestFilesService experimentStuTestFilesService;
    @Autowired
    private IExperimentFilesStuService experimentFilesStuService;

    @Autowired
    private IAssignmentRepository assignmentRepository;
    @Autowired
    private ISimilarityRepository similarityRepository;

    private String submitFilesRootDir;
    private String submitTempDir;
    private Properties props = new Properties();

    private jplag.options.Options options;
    private Program program;

    /*<实验编号,<名称，作业>>*/
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Submission>> submissions;
    protected GSTiling gSTiling;

    private float simThreshold = 50;
    private float simPassedThreshold = 90;

    int poolSize;
    ThreadPoolExecutor threadPoolExecutor;

    public JPlagServiceImpl() throws ExitException {

        initProperties();
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
        this.submitTempDir = props.getProperty("submitTempDir");
        this.poolSize = Integer.parseInt(props.getProperty("poolSize"));
//        this.poolSize = 1;
        this.threadPoolExecutor = new ThreadPoolExecutor(this.poolSize, this.poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        submissions = new ConcurrentHashMap<String, ConcurrentHashMap<String, Submission>>();

        String[] paras = {
                "-l", "c/c++",
                "-s", this.submitFilesRootDir,
                "-clustertype", "min"
        };
        this.options = new CommandLineOptions(paras, null);
        this.program = new Program(this.options);
        this.gSTiling = new GSTiling(this.program);

    }

        @PostConstruct
    /*构造函数完成后开始:初始化已有作业*/
    private void initSubmissions() throws ExitException {

        EntityWrapper<ExperimentStuTest> ew = new EntityWrapper<ExperimentStuTest>();
        /*先读取每个测试*/
        List<ExperimentStuTest> lest = experimentStuTestService.selectList(ew);

        for (ExperimentStuTest est : lest) {
            initOneTest(est);
        }

        for (Map.Entry<String, ConcurrentHashMap<String, Submission>> entry : this.submissions.entrySet()) {
            logger.info("* 实验" + entry.getKey() + ": " + entry.getValue().size() + "份.");
        }
    }

    private void initOneTest(ExperimentStuTest est) {
        User u = userService.selectById(est.getStuno());
            /*在读取每个测试的代码文件*/
        List<ExperimentStuTestFiles> lestf = experimentStuTestFilesService.selectListByTestno(est.getExperiment_stu_test_no().longValue());
        String path = this.submitTempDir + "/test-" + UUID.randomUUID().toString() + "/";
        File expf = new File(path);
        if (!expf.exists()) {
            expf.mkdirs();
        }
        for (ExperimentStuTestFiles estf : lestf) {
            ExperimentFilesStu efs = experimentFilesStuService.selectById(estf.getExperiment_files_stu_no());
            OutputStreamWriter op = null;
            try {
                String fileName = path + efs.getFileno() + ".c";
                op = new OutputStreamWriter(new FileOutputStream(fileName), "utf-8");
                op.append(efs.getFile_content());
                op.flush();
                op.close();
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage());
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            } finally {
                try {
                    if (op != null) {
                        op.close();
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }

        if (expf.exists()) {
            ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(est.getExpno() + "");
            if (expSubmissions == null) {
                expSubmissions = new ConcurrentHashMap<String, Submission>();
                submissions.put(est.getExpno() + "", expSubmissions);
            }
            /*key：学生学号，姓名和测试编号*/
            String key = u.getLoginName() + "_" + u.getName() + "_" + est.getExperiment_stu_test_no();
            Submission submission = new Submission(key, expf, false, this.program, this.program.get_language());
            try {
                submission.parse();
                /*parse出现错误*/
                if (submission.struct != null) {
                    expSubmissions.put(key, submission);

                    JPlagJob jPlagJob = new JPlagJob(this, est.getExpno().longValue(), submission, assignmentRepository, this.similarityRepository);
                    threadPoolExecutor.execute(jPlagJob);
                }
            } catch (ExitException e) {
                logger.error(e.getMessage());

            }
        }
        FileUtils.removeDir(path);


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

    public Program getProgram() {
        return this.program;
    }

    @Override
    public void updateSubmission(String expno, String sno, String sname) throws ExitException {
        File f = new File(this.submitFilesRootDir + "/" + sno + "_" + sname + "/" + expno + "/");
        Submission submission = new Submission(sno + "_" + sname, f, false, this.program, this.program.get_language());
        submission.parse();

        this.putSubmission(expno + "", sno + "_" + sname, submission);
    }

    ;

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
                if (match.percent() >= this.simThreshold) {
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

    ;

    @Override
    public void compare(String expno, Submission submission) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        SortedVector<AllMatches> avgmatches = new SortedVector<AllMatches>(new AllMatches.AvgComparator());
        for (Map.Entry<String, Submission> entry : expSubmissions.entrySet()) {
            if (entry.getValue().struct != null) {
                AllMatches match = this.gSTiling.compare(entry.getValue(), submission);
                if (match.percent() >= this.simThreshold) {
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

    private void initProperties() {
        // 读取配置文件
        Resource resource = new ClassPathResource("/config/checkmission.properties");

        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*定时查看线程列表，是否有进程结束，如果结束则从队列中移除；
    * 若出错，则重启线程*/
    @Override
    public void monitorJob() {
        logger.warn("相似度任务线程池中线程数目：" + threadPoolExecutor.getPoolSize()
                + "，队列中等待执行的任务数目：" + threadPoolExecutor.getQueue().size()
                + "，已执行完成的任务数目：" + threadPoolExecutor.getCompletedTaskCount());
    }
}

class JPlagJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JPlagJob.class);
    private Long expno;
    private String sno;
    private String sname;
    private Long experimentStuTestNo;
    private IJPlagService jPlagService;
    private IAssignmentRepository assignmentRepository;
    private ISimilarityRepository similarityRepository;
    private SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);

    private Submission submission;

    public JPlagJob(IJPlagService jPlagService, Long expno, Submission submission, IAssignmentRepository assignmentRepository, ISimilarityRepository similarityRepository) {
        this.jPlagService = jPlagService;
        this.expno = expno;
        this.submission = submission;
        String[] keys = submission.name.split("_");
        sno = keys[0];
        sname = keys[1];
        experimentStuTestNo = Long.parseLong(keys[2]);
        this.assignmentRepository = assignmentRepository;
        this.similarityRepository = similarityRepository;
    }

    @Override
    public void run() {

        ConcurrentHashMap<String, Submission> submissions = jPlagService.getSubmission(this.expno + "");
        Assignment a1 = this.assignmentRepository.findByAssignmentid(this.experimentStuTestNo);
        if (experimentStuTestNo == 1245) {
            logger.info("pause.");
        }
        for (Map.Entry<String, Submission> entry : submissions.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(this.submission.name)) {
                continue;
            }
            String[] keys = entry.getKey().split("_");
            String tsno = keys[0];
            String tsname = keys[1];
            Long tExperimentStuTestNo = Long.parseLong(keys[2]);
            float sim = this.jPlagService.compareSubmission(this.submission, entry.getValue());
//            Assignment assignment = this.assignmentRepository.findBy2ExperimentStuTestNo(this.experimentStuTestNo,
//                    tExperimentStuTestNo);
            List<Similarity> sims = this.similarityRepository.findSimilarityBy2ExperimentStuTestNo(this.experimentStuTestNo,
                    tExperimentStuTestNo);
            /*不存在联系*/
            if (sims.size() == 0) {

                Assignment a2 = this.assignmentRepository.findByAssignmentid(tExperimentStuTestNo);
                if (a1 != null && a2 != null) {
                    if (a1.getSimilarities() == null) {
                        Set<Similarity> similarities = new HashSet<Similarity>();
                        a1.setSimilarities(similarities);
                    }
                    if (a2.getSimilarities() == null) {
                        Set<Similarity> similarities = new HashSet<Similarity>();
                        a2.setSimilarities(similarities);
                    }

//                    Similarity sim_relation = new Similarity(a1, a2, new Date(), sim);
                    /*保存联系，若不保存，则会出现一致性错误*/
//                    sim_relation = similarityRepository.save(sim_relation);

//                    a1.getSimilarities().add(sim_relation);
//                    a2.getSimilarities().add(sim_relation);

                    try {
                        if (a1.getSubmitDate().after(a2.getSubmitDate())) {
                            similarityRepository.createSimilarity(this.experimentStuTestNo,
                                    tExperimentStuTestNo, sdf.format(new Date()), sim);
                        } else {
                            similarityRepository.createSimilarity(tExperimentStuTestNo,
                                    this.experimentStuTestNo, sdf.format(new Date()), sim);
                        }
//                        a1 = this.assignmentRepository.save(a1);
//                        a2 = this.assignmentRepository.save(a2);
                        logger.debug("create edge " + this.submission.name + " : " + entry.getValue().name + ", " + sim);
                    } catch (Exception e) {
                        logger.error(e.getMessage() + " create edge " + this.submission.name + " : " + entry.getValue().name + ", " + sim);
                    }

                }
            }

        }

    }
}