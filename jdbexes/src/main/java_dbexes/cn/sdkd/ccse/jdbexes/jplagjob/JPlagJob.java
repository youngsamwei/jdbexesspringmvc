package cn.sdkd.ccse.jdbexes.jplagjob;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import cn.sdkd.ccse.jdbexes.service.INeo4jService;
import jplag.ExitException;
import jplag.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static cn.sdkd.ccse.jdbexes.jplagjob.Config.SIM_THRESHOLD;

/**
 * 相似度计算任务
 */
public class JPlagJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JPlagJob.class);
    private Long stuno;
    private Long expno;
    private SubmissionKey submissionKey;
    private IJPlagService jPlagService;
    private IExperimentStuService experimentStuService;
    private INeo4jService neo4jService;

    private Submission submission;

    public JPlagJob(IJPlagService jPlagService, SubmissionKey submissionKey, Long stuno, Long expno,
                    IExperimentStuService experimentStuService, INeo4jService neo4jService) {
        this.jPlagService = jPlagService;
        this.stuno = stuno;
        this.expno = expno;
        this.submissionKey = submissionKey;
        this.experimentStuService = experimentStuService;
        this.neo4jService = neo4jService;
    }

    @Override
    public void run() {
        logger.debug("Running similarity analysers for " + submissionKey + ".");
        experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_NOT_YET, Config.SIM_DESC_RUNNING);

        // 1. 产生临时文件
        String path = jPlagService.getTestFilePath(expno, submissionKey);
        try {
            jPlagService.generateTestFiles(submissionKey.getExperiment_stu_test_no(), path);
        } catch (Exception e) {
            logger.error("无法生成临时文件 " + submissionKey, e);
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, e.getClass().getName());
        }

        // 2. 创建 Submission
        try {
            submission = jPlagService.generateSubmission(expno, submissionKey, path);
        } catch (ExitException e) {
            logger.warn("无法创建 Submission " + submissionKey);
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, e.getClass().toString());
            return;
        }

        // 3. 解析错误则跳过
        try {
            if (!jPlagService.parseSubmission(stuno, expno, submission)) {
                logger.debug("语法错误 " + submissionKey);
                experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, Config.SIM_DESC_SYNAX_ERR);
                return;
            }
        } catch (ExitException e) {
            logger.warn("解析失败 Submission " + submissionKey);
            logger.warn("解析失败 Submission (用户: " + submissionKey.getTsno() + "-" + submissionKey.getTsname() + ", 实验编号: " + expno + ").");
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, Config.SIM_DESC_PARSER_ERR);
            return;
        }

        // 4. 进行相似度比较
        Assignment assignment;
        try {
            assignment = doCompare();
        } catch (IllegalStateException e) {
            logger.warn("Assignmentid " + submissionKey.getExperiment_stu_test_no() + " 已经存在.");
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_FAILED, "Internal Error");
            return;
        }

        // 5. 更新相似度比较结果，写入数据库
        int number = getSimStatus(assignment);
        if (number > 0) {
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_PLAGIARISM, Config.getSimDesc(number, SIM_THRESHOLD));
        } else {
            experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_NORMAL, Config.SIM_DESC_NORMAL);
        }

        logger.debug("Similarity analyser for " + submissionKey + " finished.");
    }

    private Assignment doCompare() throws IllegalStateException {
        if (assignmentAlreadyExists()) {
            throw new IllegalStateException();
        }

        // 相似度检查结果
        Map<SubmissionKey, Float> simResultMap = jPlagService.compareSubmission(expno, stuno, submission);
        // 刷新 Submission 列表
        jPlagService.putSubmission(stuno, expno, submission);

        // 将提交保存于 neo4j
        Assignment assignment = neo4jService.generateStudentAssignment(stuno, expno, submissionKey.getExperiment_stu_test_no());

        // 在 Neo4J 中创建边
        for (Map.Entry<SubmissionKey, Float> result : simResultMap.entrySet()) {
            if (result.getValue() >= SIM_THRESHOLD) {
                createEdgeIfNotExist(submissionKey, result.getKey(), result.getValue());
            }
        }

        return assignment;
    }

    private int getSimStatus(Assignment assignment) {
        List<Student> students = neo4jService.findStudentBySimValueAssignmentid(SIM_THRESHOLD, assignment.getId());
        return students.size();
    }

    /**
     * 若不存在已有联系则创建
     *
     * @param key1 提交1
     * @param key2 提交2
     * @param sim  相似度
     */
    private void createEdgeIfNotExist(SubmissionKey key1, SubmissionKey key2, float sim) {
        // 存在则返回
        List<Similarity> sims = neo4jService.findSimilarityBy2Assignment(
                key1.getExperiment_stu_test_no(), key2.getExperiment_stu_test_no());
        if (!sims.isEmpty()) {
            return;
        }

        Assignment a1 = neo4jService.findAssignmentByExperimentStuTestNo(key1.getExperiment_stu_test_no());
        Assignment a2 = neo4jService.findAssignmentByExperimentStuTestNo(key2.getExperiment_stu_test_no());
        if (a1 == null || a2 == null) {
            return;
        }

        try {
            if (a1.getSubmitDate().after(a2.getSubmitDate())) {
                neo4jService.createSimilarity(key1.getExperiment_stu_test_no(), key2.getExperiment_stu_test_no(), sim);
                logger.debug("creating edge " + key1 + " -> " + key2 + ", " + sim);
            } else {
                neo4jService.createSimilarity(key2.getExperiment_stu_test_no(), key1.getExperiment_stu_test_no(), sim);
                logger.debug("creating edge " + key2 + " <- " + key1 + ", " + sim);
            }
        } catch (Exception e) {
            logger.error("creating edge " + key1 + " -- " + key2 + ", " + sim);
        }
    }

    private boolean assignmentAlreadyExists() {
        Assignment a1 = neo4jService.findAssignmentByExperimentStuTestNo(submissionKey.getExperiment_stu_test_no());
        return a1 != null;
    }

}
