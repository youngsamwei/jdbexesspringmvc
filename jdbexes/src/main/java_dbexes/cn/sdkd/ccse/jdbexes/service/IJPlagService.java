package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.jplagjob.SubmissionKey;
import jplag.ExitException;
import jplag.GSTiling;
import jplag.Program;
import jplag.Submission;

import java.io.IOException;
import java.util.Map;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {

    Map<SubmissionKey, Float> compareSubmission(Long expno, Long stuno, Submission submission);

    void monitorJob();

    void submitJob(Long stuno, Long expno);

    /**
     * 刷新相似度检查结果
     * 仅对 Neo4J 进行查询，不新建测试
     *
     * @param stuno 学生编号
     * @param expno 实验编号
     */
    void refreshSimStatus(Long stuno, Long expno);

    void putSubmission(Long stuno, Long expno, Submission submission);

    void generateTestFiles(Long experiment_stu_test_no, String dir) throws IOException;

    boolean parseSubmission(Long stuno, Long expno, Submission submission) throws ExitException;

    String getTestFilePath(Long expno, SubmissionKey key);

    Submission generateSubmission(Long expno, SubmissionKey submissionKey, String path) throws ExitException;

    GSTiling getGSTiling(Long expno) throws ExitException;

    Program getProgram(Long expno) throws ExitException;
}
