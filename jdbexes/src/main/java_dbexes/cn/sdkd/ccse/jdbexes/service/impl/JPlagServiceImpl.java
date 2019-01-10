package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.checkmission.JPlagJob;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
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
    private String submitFilesRootDir;
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

        /*初始化已有作业*/
        File root = new File(this.submitFilesRootDir);
        /*先遍历每个学生的文件夹*/
        for (File stuf : root.listFiles()) {
            /*在遍历每个实验文件夹*/
            if (stuf.isDirectory()) {
                for (File expf : stuf.listFiles()) {
                    ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expf.getName());
                    if (expSubmissions == null) {
                        expSubmissions = new ConcurrentHashMap<String, Submission>();
                        submissions.put(expf.getName(), expSubmissions);
                    }
                    Submission submission = new Submission(stuf.getName(), expf, false, this.program, this.program.get_language());
                    submission.parse();
                    expSubmissions.put(stuf.getName(), submission);
                }
            }
        }
        for(Map.Entry<String, ConcurrentHashMap<String, Submission>> entry : this.submissions.entrySet()){
            logger.info("* 实验" + entry.getKey() + ": " + entry.getValue().size() + "份.");
        }

    }

    @Override
    public void submitJob(Long stuno, Long expno) {
        UserVo u = userService.selectVoById(stuno);
        String sno = u.getLoginName();
        String sname = u.getName();
        String srcDir = this.submitFilesRootDir + "/" + sno + "_" + sname + "/" + expno + "/";
        File fSrcDir = new File(srcDir);
        if (fSrcDir.exists()) {
            JPlagJob jPlagJob = new JPlagJob(stuno, expno, sno, sname, this.submitFilesRootDir, this);
            this.threadPoolExecutor.execute(jPlagJob);

        }
    }

    @Override
    public Submission getSubmission(String expno, String name) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        if (expSubmissions != null){
            return expSubmissions.get(name);
        }
        return null;
    }

    @Override
    public void putSubmission(String expno, String name, Submission submission) {
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        if (expSubmissions == null){
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
        File f = new File(this.submitFilesRootDir + "/"  + sno + "_" + sname + "/" + expno + "/");
        Submission submission = new Submission(sno + "_" + sname, f, false, this.program, this.program.get_language());
        submission.parse();

        this.putSubmission(expno + "", sno + "_" + sname, submission);
    };

    @Override
    public boolean compareSubmission(String expno, String sno, String sname){
        ConcurrentHashMap<String, Submission> expSubmissions = submissions.get(expno);
        Submission submission = expSubmissions.get(sno + "_" + sname);
        SortedVector<AllMatches> avgmatches = new SortedVector<AllMatches>(new AllMatches.AvgComparator());
        for (Map.Entry<String, Submission> entry : expSubmissions.entrySet()) {

            if (entry.getKey().equalsIgnoreCase(submission.name)){
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
        if (avgmatches.size() <=0 ){
            return true;
        }else {
            return (avgmatches.get(0).percent() < simPassedThreshold) ;
        }
    };

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
