package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.commons.utils.FileUtils;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.*;
import com.wangzhixuan.model.User;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;
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
        this.poolSize = Integer.parseInt(props.getProperty("jplgPoolSize"));
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

    private void initOneTest(ExperimentStuTest est) {

        User u = userService.selectById(est.getStuno());
        logger.info("正在处理" + u.getLoginName() + ":" + u.getName() +"提交的实验编号为:" + est.getExpno() + "的作业.");
            /*在读取每个测试的代码文件*/
        List<ExperimentStuTestFiles> lestf = experimentStuTestFilesService.selectListByTestno(est.getExperiment_stu_test_no().longValue());
        String path = this.submitTempDir + "/test-" + u.getLoginName() + "_" + u.getName() + "_" + est.getExpno() + "_" + est.getExperiment_stu_test_no() + "/";//UUID.randomUUID().toString() + "/";
        File expf = new File(path);
        if (!expf.exists()) {
            expf.mkdirs();
        }
        /*先产生文件*/
        for (ExperimentStuTestFiles estf : lestf) {
            ExperimentFilesStu efs = experimentFilesStuService.selectById(estf.getExperiment_files_stu_no());
            OutputStreamWriter op = null;
            try {
                if (efs != null){
                    String fileName = path + efs.getFileno() + ".cpp";
                    op = new OutputStreamWriter(new FileOutputStream(fileName), "utf-8");
                    op.append(efs.getFile_content());
                    op.flush();
                    op.close();
                }else{
                    /*TODO: 如果为null，是否需要创建？*/
                    logger.error("efs is null : " + estf.getExperiment_files_stu_no());
                }
            } catch (UnsupportedEncodingException e) {
                experimentStuService.updateSimStatus(est.getStuno().longValue(), est.getExpno().longValue(),
                        1, e.getMessage());
                logger.error(e.getMessage());
            } catch (FileNotFoundException e) {
                experimentStuService.updateSimStatus(est.getStuno().longValue(), est.getExpno().longValue(),
                        1, e.getMessage());
                logger.error(e.getMessage());
            } catch (IOException e) {
                experimentStuService.updateSimStatus(est.getStuno().longValue(), est.getExpno().longValue(),
                        1, e.getMessage());
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

        /*再解析，然后提交相似度比较任务*/
        if (expf.exists()) {
            ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(est.getExpno() + "");
            if (expSubmissions == null) {
                expSubmissions = new ConcurrentHashMap<String, Submission>();
                submissions.put(est.getExpno() + "", expSubmissions);
            }

            checkExistStudentExperiment(est.getStuno().longValue(), est.getExpno().longValue());

            /*key：学生学号，姓名和测试编号*/
            String key = u.getLoginName() + "_" + u.getName() + "_" + est.getExperiment_stu_test_no();
            Submission submission = new Submission(key, expf, false, this.program, this.program.get_language());
            try {
                submission.parse();
                /*parse出现错误*/
                if (submission.struct != null) {
                    expSubmissions.put(key, submission);

                    JPlagJob jPlagJob = new JPlagJob(this, this.experimentStuService,
                            est.getExpno().longValue(), est.getStuno().longValue(),
                            submission, assignmentRepository, this.similarityRepository,
                            this.studentRepository, this.experimentRepository,
                            this.experimentService, this.userService);
                    threadPoolExecutor.execute(jPlagJob);
                }
            } catch (ExitException e) {
                experimentStuService.updateSimStatus(est.getStuno().longValue(), est.getExpno().longValue(),
                        1, e.getMessage());
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

    /*检查student和experimetn是否存在，若不存在，则在neo4j中创建*/
    private void checkExistStudentExperiment(Long stuno, Long expno){
        Student s = studentRepository.findByStudentid(stuno);
        Experiment e = experimentRepository.findByExperimentid(expno);
        if (s == null){
            logger.error("checkExistStudentExperiment 错误：" + stuno);
        }
            /*如果不存在此学生，则增加*/
            if (s == null) {
                User u = userService.selectById(stuno);
                s = new Student(stuno, u.getLoginName(), u.getName());
                s = studentRepository.save(s);
                logger.info("checkExistStudentExperiment 增加学生：" + stuno);
            }
            /*如果不存在此实验，则增加*/
            if (e == null) {
                cn.sdkd.ccse.jdbexes.model.Experiment exp = experimentService.selectById(expno);
                e = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(expno, exp.getExpname(), "");
                e = experimentRepository.save(e);
                logger.info("checkExistStudentExperiment 增加实验：" + stuno);
            }
    }

    @Override
    public void submitJob(Long stuno, Long expno) {
        UserVo u = userService.selectVoById(stuno);
        String sno = u.getLoginName();
        String sname = u.getName();
        List<ExperimentFilesStuVO> experimentFilesStuVOList = experimentFilesStuService.selectFilesLatest(stuno, expno);


        /*创建一次测试*/
        ExperimentStuTest est = new ExperimentStuTest();
        est.setStuno(stuno.intValue());
        est.setExpno(expno.intValue());
        /*增加记录后能返回自动增长的字段值TableId*/
        experimentStuTestService.insert(est);

        /*根据最新的提交文件产生最近一次测试记录*/
        experimentStuTestFilesService.insertLatestTestFiles(est.getExperiment_stu_test_no().longValue(), stuno, expno);

        /*根据est产生文件，解析并提交相似度比较任务*/
        initOneTest(est);
    }
}

class JPlagJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JPlagJob.class);
    private Long expno;
    private Long stuno;
    private String sno;
    private String sname;
    private Long experimentStuTestNo;
    private IJPlagService jPlagService;
    private IExperimentStuService experimentStuService;
    private IAssignmentRepository assignmentRepository;
    private ISimilarityRepository similarityRepository;
    private SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);
    private IStudentRepository studentRepository;
    private IExperimentRepository experimentRepository;
    private IExperimentService experimentService;
    private IUserService userService;


    private Submission submission;

    public JPlagJob(IJPlagService jPlagService, IExperimentStuService experimentStuService, Long expno, Long stuno,
                    Submission submission, IAssignmentRepository assignmentRepository,
                    ISimilarityRepository similarityRepository, IStudentRepository studentRepository,
                    IExperimentRepository experimentRepository, IExperimentService experimentService, IUserService userService) {
        this.jPlagService = jPlagService;
        this.expno = expno;
        this.stuno = stuno;
        this.submission = submission;
        String[] keys = submission.name.split("_");
        sno = keys[0];
        sname = keys[1];
        experimentStuTestNo = Long.parseLong(keys[2]);
        this.assignmentRepository = assignmentRepository;
        this.similarityRepository = similarityRepository;
        this.studentRepository = studentRepository;
        this.experimentStuService = experimentStuService;
        this.experimentRepository = experimentRepository;
        this.experimentService = experimentService;
        this.userService = userService;
    }

    @Override
    public void run() {

        ConcurrentHashMap<String, Submission> submissions = jPlagService.getSubmission(this.expno + "");

        Assignment a1 = this.assignmentRepository.findByAssignmentid(this.experimentStuTestNo);
        if (a1 == null) {
            a1 = new Assignment();
            a1.setAssignmentid(this.experimentStuTestNo);
            a1.setSubmitDate(new Date());
            a1 = this.assignmentRepository.save(a1);
            Student s = studentRepository.findByStudentid(this.stuno);
            Experiment e = experimentRepository.findByExperimentid(this.expno);
            if (s == null){
                logger.error("错误：" + this.stuno);
            }
            /*在此添加学生失败，会出现重复*/
            /*如果不存在此学生，则增加*/
//            if (s == null) {
//                User u = userService.selectById(this.stuno);
//                s = new Student(this.stuno, u.getLoginName(), u.getName());
//                s = studentRepository.save(s);
//            }
            /*如果不存在此实验，则增加*/
//            if (e == null) {
//                cn.sdkd.ccse.jdbexes.model.Experiment exp = experimentService.selectById(this.expno);
//                e = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(this.expno, exp.getExpname(), "");
//                e = experimentRepository.save(e);
//            }

            this.assignmentRepository.createSubmitRelationship(s.getId(), a1.getId());
            this.assignmentRepository.createBelongtoRelationship(e.getId(), a1.getId());
        }
        for (Map.Entry<String, Submission> entry : submissions.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(this.submission.name)) {
                continue;
            }
            String[] keys = entry.getKey().split("_");
            if (keys.length < 3){
                logger.error("错误的Submission " + entry.getKey());
                continue;
            }
            String tsno = keys[0];
            String tsname = keys[1];
            Long tExperimentStuTestNo = Long.parseLong(keys[2]);
            float sim = this.jPlagService.compareSubmission(this.submission, entry.getValue());

            /*这个查询无关方向*/
            List<Similarity> sims = this.similarityRepository.findSimilarityBy2ExperimentStuTestNo(this.experimentStuTestNo,
                    tExperimentStuTestNo);
            /*不存在联系*/
            if (sims.size() == 0) {

                Assignment a2 = this.assignmentRepository.findByAssignmentid(tExperimentStuTestNo);
                if (a2 != null) {

                    try {
                        if (a1.getSubmitDate().after(a2.getSubmitDate())) {
                            similarityRepository.createSimilarity(this.experimentStuTestNo,
                                    tExperimentStuTestNo, sdf.format(new Date()), sim);
                        } else {
                            similarityRepository.createSimilarity(tExperimentStuTestNo,
                                    this.experimentStuTestNo, sdf.format(new Date()), sim);
                        }

                        logger.debug("create edge " + this.submission.name + " : " + entry.getValue().name + ", " + sim);
                    } catch (Exception e) {
                        logger.error(e.getMessage() + " create edge " + this.submission.name + " : " + entry.getValue().name + ", " + sim);
                    }

                }
            }

        }

        float sim = 90f;
        /*TODO:获取相似度比较结果，写入数据库*/
        List<Student> lss = this.studentRepository.findBySimValueAssignmentid(sim, a1.getId());
        /*若相似度超过阈值的学生个数大于0，则状态是3，否则状态是0*/
        if (lss.size() >0) {
            experimentStuService.updateSimStatus(this.stuno, this.expno, 3, "与" + lss.size() + "个同学的作业相似度超过" + sim + "%.");
        }else{
            experimentStuService.updateSimStatus(this.stuno, this.expno, 0, "与" + lss.size() + "个同学的作业相似度超过" + sim + "%.");
        }
    }
}