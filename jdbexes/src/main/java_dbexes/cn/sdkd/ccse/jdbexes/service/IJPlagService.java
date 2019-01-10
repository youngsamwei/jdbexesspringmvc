package cn.sdkd.ccse.jdbexes.service;

import jplag.ExitException;
import jplag.Program;
import jplag.Submission;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {
    /*测试指定学生的指定实验与其他学生的指定实验的相似程度，返回最高的相似度*/
    void submitJob(Long stuno, Long expno);

    Submission getSubmission(String expno, String name);
    void putSubmission(String expno, String name, Submission submission);
    Program getProgram();
    void compare(String expno, Submission submission);
    void updateSubmission(String expno, String sno, String sname) throws ExitException;
    boolean compareSubmission(String expno, String sno, String sname);

}
