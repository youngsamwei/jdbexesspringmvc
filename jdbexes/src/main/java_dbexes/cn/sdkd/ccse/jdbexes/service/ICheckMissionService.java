package cn.sdkd.ccse.jdbexes.service;

/**
 * Created by sam on 2019/1/4.
 */
public interface ICheckMissionService {

    void submitJob(Long stuno, Long expno);

    void monitorJob();
}
