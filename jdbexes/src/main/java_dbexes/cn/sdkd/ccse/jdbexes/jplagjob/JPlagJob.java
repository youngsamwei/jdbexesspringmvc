package cn.sdkd.ccse.jdbexes.jplagjob;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuTestService;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import jplag.ExitException;
import jplag.Submission;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private IExperimentStuTestService experimentStuTestService;

    private IStudentRepository studentRepository;
    private IAssignmentRepository assignmentRepository;
    private ISimilarityRepository similarityRepository;
    private IExperimentRepository experimentRepository;

    private Submission submission;

    public JPlagJob(IJPlagService jPlagService, SubmissionKey submissionKey, Long stuno, Long expno,
                    IExperimentStuService experimentStuService, IExperimentStuTestService experimentStuTestService,
                    IAssignmentRepository assignmentRepository, ISimilarityRepository similarityRepository,
                    IStudentRepository studentRepository, IExperimentRepository experimentRepository) {
        this.jPlagService = jPlagService;
        this.stuno = stuno;
        this.expno = expno;
        this.submissionKey = submissionKey;
        this.assignmentRepository = assignmentRepository;
        this.similarityRepository = similarityRepository;
        this.studentRepository = studentRepository;
        this.experimentRepository = experimentRepository;
        this.experimentStuService = experimentStuService;
        this.experimentStuTestService = experimentStuTestService;
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
        Assignment assignment = generateAssignment();

        // 在 Neo4J 中创建边
        for (Map.Entry<SubmissionKey, Float> result : simResultMap.entrySet()) {
            if (result.getValue() >= SIM_THRESHOLD) {
                createEdgeIfNotExist(submissionKey, result.getKey(), result.getValue());
            }
        }

        return assignment;
    }

    private int getSimStatus(Assignment assignment) {
        List<Student> students = studentRepository.findBySimValueAssignmentid(SIM_THRESHOLD, assignment.getId());
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
        List<Similarity> sims = similarityRepository.findSimilarityBy2ExperimentStuTestNo(
                key1.getExperiment_stu_test_no(), key2.getExperiment_stu_test_no());
        if (!sims.isEmpty()) {
            return;
        }

        Assignment a1 = assignmentRepository.findByAssignmentid(key1.getExperiment_stu_test_no());
        Assignment a2 = assignmentRepository.findByAssignmentid(key2.getExperiment_stu_test_no());
        if (a1 == null || a2 == null) {
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);
            if (a1.getSubmitDate().after(a2.getSubmitDate())) {
                similarityRepository.createSimilarity(key1.getExperiment_stu_test_no(), key2.getExperiment_stu_test_no(), sdf.format(new Date()), sim);
                logger.debug("creating edge " + key1 + " -> " + key2 + ", " + sim);
            } else {
                similarityRepository.createSimilarity(key2.getExperiment_stu_test_no(), key1.getExperiment_stu_test_no(), sdf.format(new Date()), sim);
                logger.debug("creating edge " + key2 + " <- " + key1 + ", " + sim);
            }
        } catch (Exception e) {
            logger.error("creating edge " + key1 + " -- " + key2 + ", " + sim);
        }
    }

    /**
     * 创建一次提交
     * @return assignment
     */
    private Assignment generateAssignment() throws NullPointerException {
        Assignment a1 = new Assignment();
        a1.setAssignmentid(submissionKey.getExperiment_stu_test_no());
        a1.setSubmitDate(new Date());
        a1 = assignmentRepository.save(a1);
        Student s = studentRepository.findByStudentid(stuno);
        Experiment e = experimentRepository.findByExperimentid(expno);

        assignmentRepository.createSubmitRelationship(s.getId(), a1.getId());
        assignmentRepository.createBelongtoRelationship(e.getId(), a1.getId());
        return a1;
    }

    private boolean assignmentAlreadyExists() {
        Assignment a1 = assignmentRepository.findByAssignmentid(submissionKey.getExperiment_stu_test_no());
        return a1 != null;
    }

}
