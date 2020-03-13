package cn.sdkd.ccse.jdbexes.service;

import jplag.Submission;

import java.util.List;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {

    void putSubmission(Long expno, Submission submission);

    float compareSubmission(Submission a, Submission b);

    /*获得实验编号expno对应的submission列表*/
    List<Submission> getSubmission(Long expno);

    void monitorJob();

    void submitJob(Long stuno, Long expno);

    void refreshSimStatus(Long stuno, Long expno);
}
