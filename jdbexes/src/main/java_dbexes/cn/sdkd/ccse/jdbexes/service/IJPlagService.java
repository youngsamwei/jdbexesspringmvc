package cn.sdkd.ccse.jdbexes.service;

import jplag.Program;
import jplag.Submission;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {
    /*测试指定学生的指定实验与其他学生的指定实验的相似程度，返回最高的相似度*/
    void submitJob(Long stuno, Long expno);

    Submission getSubmission(String name);
    void putSubmission(String name, Submission submission);
    Program getProgram();
    void compare(Submission submission);

}
