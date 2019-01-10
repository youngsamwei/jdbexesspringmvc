package cn.sdkd.ccse.jdbexes.service;

import jplag.ExitException;
import jplag.Program;
import jplag.Submission;

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

}
