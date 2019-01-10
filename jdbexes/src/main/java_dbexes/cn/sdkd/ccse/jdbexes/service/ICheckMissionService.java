package cn.sdkd.ccse.jdbexes.service;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by sam on 2019/1/4.
 */
public interface ICheckMissionService {

    void submitJob(Long stuno, Long expno);

    void submitJob(Long expstuno);

    void monitorJob();

    String getLogRootDir();

    boolean addProjectDir(String s);

    ConcurrentLinkedQueue<String> getProjectDirQueue();

    String newProjectDir();

}
