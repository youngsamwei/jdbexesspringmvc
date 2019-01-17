package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.commons.utils.FileUtils;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStu;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTestFiles;
import cn.sdkd.ccse.jdbexes.service.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wangzhixuan.model.User;
import com.wangzhixuan.model.vo.UserVo;
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
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by sam on 2019/1/7.
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

//        initSubmissions();
    }

    @PostConstruct
    /*构造函数完成后开始:初始化已有作业*/
    private void initSubmissions() throws ExitException {

        EntityWrapper<ExperimentStuTest> ew = new EntityWrapper<ExperimentStuTest>();
        /*先读取每个测试*/
        List<ExperimentStuTest> lest = experimentStuTestService.selectList(ew);

        for (ExperimentStuTest est : lest){
            initOneTest(est);
        }

        for (Map.Entry<String, ConcurrentHashMap<String, Submission>> entry : this.submissions.entrySet()) {
            logger.info("* 实验" + entry.getKey() + ": " + entry.getValue().size() + "份.");
        }
    }

    private void initOneTest(ExperimentStuTest est) throws ExitException {
        User u = userService.selectById(est.getStuno());
            /*在读取每个测试的代码文件*/
        List<ExperimentStuTestFiles> lestf = experimentStuTestFilesService.selectListByTestno(est.getExperiment_stu_test_no().longValue());
        String path = this.submitTempDir + "/test-" + UUID.randomUUID().toString() + "/";
        File expf = new File(path);
        if (!expf.exists()){
            expf.mkdirs();
        }
        for (ExperimentStuTestFiles estf : lestf){
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
            }finally {
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
            String key = u.getLoginName() + "_" + u.getName();
            Submission submission = new Submission(key, expf, false, this.program, this.program.get_language());
            submission.parse();
            expSubmissions.put(key, submission);
            FileUtils.removeDir(path);
        }
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
}
