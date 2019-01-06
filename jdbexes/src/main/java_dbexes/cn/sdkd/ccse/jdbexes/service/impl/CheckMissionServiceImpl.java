package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.checkmission.CheckJob;
import cn.sdkd.ccse.jdbexes.checkmission.CheckJobThread;
import cn.sdkd.ccse.jdbexes.service.ICheckMissionService;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.State.TERMINATED;

/**
 * Created by sam on 2019/1/4.
 */
@Service
public class CheckMissionServiceImpl implements ICheckMissionService {
    private static final Log logger = LogFactory.getLog(CheckMissionServiceImpl.class);

    /*所有学生提交文件的根目录，每个学生一个文件夹，每个实验一个文件夹*/
    private String submitFilesRootDir;
    /*项目的源文件根目录*/
    private String originalProjectRootDir;

    /*临时存放学生的测试项目根目录*/
    private String projectRootDir;

    @Autowired
    private IExperimentFilesStuService experimentFilesStuService;
    @Autowired
    private IExperimentStuService experimentStuService;

    ConcurrentHashMap<String, CheckJobThread> jobThreads;

    Properties props = new Properties();

    public CheckMissionServiceImpl() {
        jobThreads = new ConcurrentHashMap<String, CheckJobThread>();
        initProperties();
        this.submitFilesRootDir = props.getProperty("submitFilesRootDir");
        this.originalProjectRootDir = props.getProperty("originalProjectRootDir");
        this.projectRootDir = props.getProperty("projectRootDir");
    }

    /*提交检查作业任务的job，检查指定学号和实验的最新版本*/
    @Override
    public void submitJob(Long stuno, Long expno) {
        String srcDir = this.submitFilesRootDir + stuno + "/" + expno + "/";
        String projectDir = this.projectRootDir + "/" + stuno + "-" + expno + "-" + UUID.randomUUID().toString() + "/";

        CheckJob cj = new CheckJob(stuno, expno, experimentFilesStuService, experimentStuService, srcDir, projectDir, this.originalProjectRootDir);

        CheckJobThread cjt = new CheckJobThread(cj);
        /*以“学号_实验编号”作为key*/
        jobThreads.put(stuno + "_" + expno, cjt);
        cjt.start();
    }

    /*定时查看线程列表，是否有进程结束，如果结束则从队列中移除；
    * 若出错，则重启线程*/
    @Override
    public void monitorJob() {
        logger.debug("当前job数量为：" + jobThreads.size());
        int tcount = 0;
        for (Map.Entry<String, CheckJobThread> entry : jobThreads.entrySet()) {
            CheckJobThread cjt = entry.getValue();
            if (cjt.getState() == TERMINATED) {
                tcount++;
                jobThreads.remove(entry.getKey());
            }
        }
        logger.debug("移除" + tcount + "个已经结束的job.");
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
}
