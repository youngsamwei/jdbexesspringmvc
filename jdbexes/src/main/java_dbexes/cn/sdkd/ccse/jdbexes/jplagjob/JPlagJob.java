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
import jplag.Submission;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.sdkd.ccse.jdbexes.jplagjob.Config.SIM_THRESHOLD;
import static cn.sdkd.ccse.jdbexes.jplagjob.Config.SIM_THRESHOLD_SAME;

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

    public JPlagJob(IJPlagService jPlagService, Submission submission, Long stuno, Long expno,
                    IExperimentStuService experimentStuService, IExperimentStuTestService experimentStuTestService,
                    IAssignmentRepository assignmentRepository, ISimilarityRepository similarityRepository,
                    IStudentRepository studentRepository, IExperimentRepository experimentRepository) {
        this.jPlagService = jPlagService;
        this.stuno = stuno;
        this.expno = expno;
        this.submission = submission;
        this.submissionKey = SubmissionKey.valueOf(submission.name);
        this.assignmentRepository = assignmentRepository;
        this.similarityRepository = similarityRepository;
        this.studentRepository = studentRepository;
        this.experimentRepository = experimentRepository;
        this.experimentStuService = experimentStuService;
        this.experimentStuTestService = experimentStuTestService;
    }

    @Override
    public void run() {
        logger.debug("Running similarity analysers for (用户: " + this.submissionKey.getTsno() + "-" + this.submissionKey.getTsname() + ", 实验编号: " + this.expno + ").");
        experimentStuService.updateSimStatus(stuno, expno, Config.SIM_STATUS_NOT_YET, Config.SIM_DESC_RUNNING);

        boolean duplicate = false;
        Long duplicateAssignmentId = null;
        Map<SubmissionKey, Float> simResultMap = new HashMap<>();
        if (assignmentAlreadyExists()) {
            logger.warn("Assignmentid " + this.submissionKey.getExperiment_stu_test_no() + " 已经存在.");
            duplicate = true;
            duplicateAssignmentId = this.submissionKey.getExperiment_stu_test_no();
        } else {
            // 同一实验下的子提交
            ConcurrentHashMap<Long, Submission> submissions = jPlagService.getSubmission(this.expno);

            // 相似度检查结果
            for (Map.Entry<Long, Submission> entry : submissions.entrySet()) {
                Submission submission = entry.getValue();
                if (submission.name.equalsIgnoreCase(this.submission.name)) {
                    continue;
                }
                SubmissionKey key = SubmissionKey.valueOf(submission.name);
                float sim = this.jPlagService.compareSubmission(this.expno, this.submission, submission);
                simResultMap.put(key, sim);
            }

            // 如果与自己已有提交相似度接近 100%，则不创建新提交
            Set<Long> userAssignments = assignmentRepository.findByStudentIdExpId(this.stuno, this.expno).stream().map(
                    Assignment::getAssignmentid
            ).collect(Collectors.toCollection(HashSet::new));

            logger.debug("当前编号：" + this.submission.name);
            logger.debug("已提交编号：" + Arrays.toString(userAssignments.toArray()));

            for (Map.Entry<SubmissionKey, Float> result : simResultMap.entrySet()) {
                if (result.getValue() >= SIM_THRESHOLD_SAME && userAssignments.contains(result.getKey().getExperiment_stu_test_no())) {
                    logger.debug("重复提交，不重新计算相似度");
                    duplicate = true;
                    duplicateAssignmentId = result.getKey().getExperiment_stu_test_no();
                    break;
                }
            }
        }

        Assignment a1;
        if (duplicate) {
            // 若重复，仅刷新旧提交的结果
            a1 = assignmentRepository.findByAssignmentid(duplicateAssignmentId);
        } else {
            // 刷新 Submission 列表
            jPlagService.putSubmission(stuno, expno, submission);
            // 将提交保存于 neo4j，并创建联系
            a1 = generateAssignment();
            // 更新用户最新实验
            experimentStuTestService.insertLatestTest(stuno, expno, a1.getAssignmentid());
            // 在 Neo4J 中创建边
            for (Map.Entry<SubmissionKey, Float> result : simResultMap.entrySet()) {
                if (result.getValue() >= SIM_THRESHOLD) {
                    createEdgeIfNotExist(this.submissionKey, result.getKey(), result.getValue());
                }
            }
        }

        // 获取相似度比较结果，写入数据库
        updateSimStatus(a1);

        logger.debug("simcheck end");
    }

    /**
     * 获取相似度比较结果，写入数据库
     * @param assignment 提交
     */
    private void updateSimStatus(Assignment assignment) {
        List<Student> lss = this.studentRepository.findBySimValueAssignmentid(SIM_THRESHOLD, assignment.getId());

        // 若相似度超过阈值的学生个数大于0，则状态是3，否则状态是0
        if (lss.size() > 0) {
            experimentStuService.updateSimStatus(this.stuno, this.expno, Config.SIM_STATUS_PLAGIARISM, Config.getSimDesc(lss.size(), SIM_THRESHOLD));
        } else {
            experimentStuService.updateSimStatus(this.stuno, this.expno, Config.SIM_STATUS_NORMAL, Config.SIM_DESC_NORMAL);
        }
    }

    /**
     * 若不存在已有联系则创建
     * @param key1 提交1
     * @param key2 提交2
     * @param sim  相似度
     */
    private void createEdgeIfNotExist(SubmissionKey key1, SubmissionKey key2, float sim) {
        // 存在则返回
        List<Similarity> sims = this.similarityRepository.findSimilarityBy2ExperimentStuTestNo(
                key1.getExperiment_stu_test_no(), key2.getExperiment_stu_test_no());
        if (!sims.isEmpty()) {
            return;
        }

        Assignment a1 = this.assignmentRepository.findByAssignmentid(key1.getExperiment_stu_test_no());
        Assignment a2 = this.assignmentRepository.findByAssignmentid(key2.getExperiment_stu_test_no());
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
        a1.setAssignmentid(this.submissionKey.getExperiment_stu_test_no());
        a1.setSubmitDate(new Date());
        a1 = this.assignmentRepository.save(a1);
        Student s = studentRepository.findByStudentid(this.stuno);
        Experiment e = experimentRepository.findByExperimentid(this.expno);

        this.assignmentRepository.createSubmitRelationship(s.getId(), a1.getId());
        this.assignmentRepository.createBelongtoRelationship(e.getId(), a1.getId());
        return a1;
    }

    private boolean assignmentAlreadyExists() {
        Assignment a1 = this.assignmentRepository.findByAssignmentid(this.submissionKey.getExperiment_stu_test_no());
        return a1 != null;
    }

}
