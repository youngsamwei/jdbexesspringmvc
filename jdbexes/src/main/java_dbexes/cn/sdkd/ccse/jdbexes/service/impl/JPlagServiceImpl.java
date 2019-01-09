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

    private ConcurrentHashMap<String, Submission> submissions;
    protected GSTiling gSTiling;

    int poolSize;
    ThreadPoolExecutor threadPoolExecutor;

    public JPlagServiceImpl() throws ExitException {

        initProperties();
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
        this.poolSize = Integer.parseInt(props.getProperty("poolSize"));
        this.threadPoolExecutor = new ThreadPoolExecutor(this.poolSize, this.poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        submissions = new ConcurrentHashMap<String, Submission>();

        String[] paras = {
                "-l", "c/c++",
                "-s",this.submitFilesRootDir,
                "-clustertype", "min"
        };
        this.options = new CommandLineOptions(paras, null);
        this.program = new Program(this.options);
        this.gSTiling = new GSTiling(this.program);

        File root = new File(this.submitFilesRootDir);
        for(File f : root.listFiles()){
            Submission submission = new Submission(f.getName(), f, true, this.program, this.program.get_language());
            submission.parse();
            submissions.put(f.getName(), submission);
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
    public Submission getSubmission(String name) {
        return this.submissions.get(name);
    }

    @Override
    public void putSubmission(String name, Submission submission) {
        this.submissions.put(name, submission);
    }

    public Program getProgram() {
        return this.program;
    }

    @Override
    public void compare(Submission submission) {
        for (Map.Entry<String, Submission> entry : this.submissions.entrySet()) {
            if (entry.getValue().struct != null) {
                AllMatches match = this.gSTiling.compare(entry.getValue(), submission);
                logger.info(entry.getValue().name + " - " + submission.name + " : " + match.percent());
            }else{
                logger.info(entry.getValue().name + " 未提交文件或解析错误.");
            }
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
