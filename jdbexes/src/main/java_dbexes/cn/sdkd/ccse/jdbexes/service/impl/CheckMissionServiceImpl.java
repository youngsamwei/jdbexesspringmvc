package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.checkmission.CheckJob;
import cn.sdkd.ccse.jdbexes.model.ExperimentStu;
import cn.sdkd.ccse.jdbexes.service.ICheckMissionService;
import cn.sdkd.ccse.jdbexes.service.IExperimentFilesStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 代码测试服务
 * 代码测试任务的生成、提交、监控
 */
@Service
public class CheckMissionServiceImpl implements ICheckMissionService {
    private static final Logger logger = LoggerFactory.getLogger(CheckMissionServiceImpl.class);

    private final IExperimentFilesStuService experimentFilesStuService;
    private final IExperimentStuService experimentStuService;
    private final IUserService userService;
    private final IExperimentService experimentService;

    ConcurrentLinkedQueue<String> projectDirQueue;
    ThreadPoolExecutor threadPoolExecutor;

    Properties props;

    public CheckMissionServiceImpl(IExperimentFilesStuService experimentFilesStuService,
                                   IExperimentStuService experimentStuService,
                                   IUserService userService, IExperimentService experimentService) throws IOException {
        this.experimentFilesStuService = experimentFilesStuService;
        this.experimentStuService = experimentStuService;
        this.userService = userService;
        this.experimentService = experimentService;

        props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("/config/checkmission.properties"));
        int poolSize = Integer.parseInt(props.getProperty("poolSize"));

        projectDirQueue = new ConcurrentLinkedQueue<>();
        threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 提交检查作业任务的job，检查指定学号和实验的最新版本
     */
    @Override
    public void submitJob(Long stuno, Long expno) {
        UserVo u = userService.selectVoById(stuno);
        String sno = u.getLoginName();
        String sname = u.getName();

        String dockerHost = props.getProperty("docker.host");

        experimentStuService.updateStatusDesc(stuno, expno, -1, "未测试");
        CheckJob cj = new CheckJob(dockerHost, stuno, expno, sno, sname,
                experimentFilesStuService, experimentStuService, experimentService);

        threadPoolExecutor.execute(cj);

    }

    /**
     * 按照学生选择实验的编号提交job
     */
    @Override
    public void submitJob(Long expstuno) {
        ExperimentStu es = experimentStuService.selectById(expstuno);
        submitJob(es.getStuno(), es.getExpno());
    }

    /**
     * 定时查看线程列表，是否有进程结束，如果结束则从队列中移除；若出错，则重启线程
     */
    @Override
    public void monitorJob() {
        logger.warn("测试任务线程池中线程数目：" + threadPoolExecutor.getPoolSize()
                + "，队列中等待执行的任务数目：" + threadPoolExecutor.getQueue().size()
                + "，已执行完成的任务数目：" + threadPoolExecutor.getCompletedTaskCount());
    }

}
