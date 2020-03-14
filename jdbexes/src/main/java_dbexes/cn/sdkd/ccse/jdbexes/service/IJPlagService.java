package cn.sdkd.ccse.jdbexes.service;

import jplag.Submission;

import java.util.List;

/**
 * Created by sam on 2019/1/7.
 */
public interface IJPlagService {

    float compareSubmission(Long expno, Submission a, Submission b);

    /**
     * 获得实验编号 expno 对应的 submission 列表
     */
    List<Submission> getSubmission(Long expno);

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
}
