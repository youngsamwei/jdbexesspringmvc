package cn.sdkd.ccse.jdbexes.service;

import jplag.ExitException;
import jplag.Program;
import jplag.Submission;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {

    Submission getSubmission(String expno, String name);

    void putSubmission(String expno, String name, Submission submission);

    Program getProgram();

    void compare(String expno, Submission submission);

    void updateSubmission(String expno, String sno, String sname) throws ExitException;

    boolean compareSubmission(String expno, String sno, String sname);

    float compareSubmission(Submission a, Submission b);

    /*获得实验编号expno对应的submission列表*/
    ConcurrentHashMap<String, Submission> getSubmission(String expno);

    public void monitorJob();
}
