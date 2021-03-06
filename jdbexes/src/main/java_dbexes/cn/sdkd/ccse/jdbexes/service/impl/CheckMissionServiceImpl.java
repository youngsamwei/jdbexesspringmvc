package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.commons.utils.FileUtils;
import cn.sdkd.ccse.jdbexes.checkmission.CheckJob;
import cn.sdkd.ccse.jdbexes.model.ExperimentFilesStuVO;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import cn.sdkd.ccse.jdbexes.service.ICheckMissionService;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;
import jplag.ExitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by sam on 2019/1/4.
 *
 * 测试任务
 * 测试每个提交
 */
@Service
public class CheckMissionServiceImpl implements ICheckMissionService {
    private static final Logger logger = LoggerFactory.getLogger(CheckMissionServiceImpl.class);

    /*所有学生提交文件的根目录，每个学生一个文件夹，每个实验一个文件夹*/
    private String submitFilesRootDir;
    /*项目的源文件根目录*/
    private String originalProjectRootDir;

    /*临时存放学生的测试项目根目录*/
    private String projectRootDir;

    /*测试日志目录*/
    private String logRootDir;

    @Autowired
    private IExperimentFilesStuService experimentFilesStuService;
    @Autowired
    private IExperimentStuService experimentStuService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IJPlagService jPlagService;

    ConcurrentLinkedQueue<String> projectDirQueue;

    int poolSize;
    ThreadPoolExecutor threadPoolExecutor;

    Properties props = new Properties();

    public CheckMissionServiceImpl() {
        projectDirQueue = new ConcurrentLinkedQueue<String>();
        initProperties();
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
        this.originalProjectRootDir = props.getProperty("originalProjectRootDir");
        this.projectRootDir = props.getProperty("projectRootDir");
        this.logRootDir = props.getProperty("logRootDir");
        this.poolSize = Integer.parseInt(props.getProperty("poolSize"));
        this.threadPoolExecutor = new ThreadPoolExecutor(this.poolSize, this.poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        /*清理临时项目文件夹*/
        FileUtils.delFiles(this.projectRootDir);
    }

    /*提交检查作业任务的job，检查指定学号和实验的最新版本*/
    @Override
    public void submitJob(Long stuno, Long expno) {
        UserVo u = userService.selectVoById(stuno);
        String sno = u.getLoginName();
        String sname = u.getName();

        String srcDir = this.submitFilesRootDir + "/" + sno + "_" + sname + "/" + expno + "/";
        String logDir = this.logRootDir + "/" + sno + "_" + sname + "/" + expno + "/";

        List<ExperimentFilesStuVO> experimentFilesStuVOList = experimentFilesStuService.selectFilesLatest(stuno, expno);
        if (experimentFilesStuVOList.size() <= 0) {
            experimentStuService.updateStatusDesc(stuno, expno, -1, "未提交文件");
        } else {

            /*先生成文件*/
            this.generateFiles(srcDir, experimentFilesStuVOList);

            /*再测试*/
            experimentStuService.updateStatusDesc(stuno, expno, -1, "未测试");
            CheckJob cj = new CheckJob(stuno, expno, sno, sname, experimentFilesStuService,
                    experimentStuService,
                    this,
                    srcDir,
                    this.originalProjectRootDir,
                    logDir);

            threadPoolExecutor.execute(cj);

        }

    }

    private void generateFiles(String srcDir, List<ExperimentFilesStuVO> experimentFilesStuVOList) {
        /*如果srcDir所指文件夹不存在，则创建*/
        File f = new File(srcDir);
        if(!f.exists()){
            f.mkdirs();
        }
        logger.debug("获得" + experimentFilesStuVOList.size() + "文件");
        Timestamp maxtt = null;
        for (ExperimentFilesStuVO efsv : experimentFilesStuVOList) {
            if (maxtt == null){
                maxtt = efsv.getSubmittime();
            }else if (maxtt.after(efsv.getSubmittime())){
                maxtt = efsv.getSubmittime();
            }
            String fname = srcDir + efsv.getSrcfilename();
            OutputStreamWriter op = null;
            try {
                op = new OutputStreamWriter(new FileOutputStream(fname), "utf-8");
                op.append(efsv.getFile_content());
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
                    op.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        /*写提交时间至文件*/
        OutputStreamWriter op = null;
        try {
            op = new OutputStreamWriter(new FileOutputStream(srcDir + "/time.txt"), "utf-8");
            op.append(maxtt.toString());
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
                op.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /*按照学生选择实验的编号提交job*/
    @Override
    public void submitJob(Long expstuno) {
        ExperimentStu es = experimentStuService.selectById(expstuno);
        submitJob(es.getStuno(), es.getExpno());
    }

    /*定时查看线程列表，是否有进程结束，如果结束则从队列中移除；
    * 若出错，则重启线程*/
    @Override
    public void monitorJob() {
        logger.warn("测试任务线程池中线程数目：" + threadPoolExecutor.getPoolSize()
                + "，队列中等待执行的任务数目：" + threadPoolExecutor.getQueue().size()
                + "，已执行完成的任务数目：" + threadPoolExecutor.getCompletedTaskCount());
    }

    @Override
    public String getLogRootDir() {
        return this.logRootDir;
    }

    @Override
    public String newProjectDir() {
        return this.projectRootDir + "/" + UUID.randomUUID().toString() + "/";
    }

    @Override
    public boolean addProjectDir(String s) {
        return projectDirQueue.offer(s);
    }

    /**
     * 获取配置文件
     *
     * @return 配置Props
     */
    private void initProperties() {
        // 读取配置文件
        Resource resource = new ClassPathResource("/config/checkmission.properties");

        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentLinkedQueue<String> getProjectDirQueue() {
        return projectDirQueue;
    }
}
