package cn.sdkd.ccse.jdbexes.service.impl.jplug;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import cn.sdkd.ccse.jdbexes.service.IJPlagService;
import jplag.Submission;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 相似度计算任务
 */
class JPlagJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(JPlagJob.class);
    private Long stuno;
    private Long expno;
    private SubmissionKey submissionKey;
    private IJPlagService jPlagService;
    private IExperimentStuService experimentStuService;

    private IStudentRepository studentRepository;
    private IAssignmentRepository assignmentRepository;
    private ISimilarityRepository similarityRepository;
    private IExperimentRepository experimentRepository;

    private final float SIM_THRESHOLD = 0.9f;

    private Submission submission;

    public JPlagJob(IJPlagService jPlagService, Submission submission, Long stuno, Long expno,
                    IExperimentStuService experimentStuService,
                    IAssignmentRepository assignmentRepository,
                    ISimilarityRepository similarityRepository, IStudentRepository studentRepository,
                    IExperimentRepository experimentRepository) {
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
    }

    @Override
    public void run() {
        // 获取同一实验下的子提交
        ConcurrentHashMap<String, Submission> submissions = jPlagService.getSubmission(String.valueOf(this.expno));

        // 创建提交
        Assignment a1 = generateAssignment();

        for (Map.Entry<String, Submission> entry : submissions.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(this.submission.name)) {
                continue;
            }

            assert entry.getValue().name.equals(entry.getKey());

            // 若不存在已有联系，则创建相似度联系
            SubmissionKey key = SubmissionKey.valueOf(entry.getKey());
            float sim = this.jPlagService.compareSubmission(this.submission, entry.getValue());
            if (sim >= SIM_THRESHOLD) {
                createEdgeIfNotExist(this.submissionKey, key, sim);
            }
        }

        // 获取相似度比较结果，写入数据库
        updateSimStatus(a1);
    }

    /**
     * 获取相似度比较结果，写入数据库
     * @param assignment 提交
     */
    private void updateSimStatus(Assignment assignment) {
        List<Student> lss = this.studentRepository.findBySimValueAssignmentid(SIM_THRESHOLD, assignment.getId());

        // 若相似度超过阈值的学生个数大于0，则状态是3，否则状态是0
        if (lss.size() > 0) {
            experimentStuService.updateSimStatus(this.stuno, this.expno, 3, "与" + lss.size() + "个同学的作业相似度超过" + SIM_THRESHOLD + "%.");
        } else {
            experimentStuService.updateSimStatus(this.stuno, this.expno, 0, "与" + lss.size() + "个同学的作业相似度超过" + SIM_THRESHOLD + "%.");
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
                key1.experiment_stu_test_no, key2.experiment_stu_test_no);
        if (!sims.isEmpty()) {
            return;
        }

        Assignment a1 = this.assignmentRepository.findByAssignmentid(key1.experiment_stu_test_no);
        Assignment a2 = this.assignmentRepository.findByAssignmentid(key2.experiment_stu_test_no);
        if (a1 == null || a2 == null) {
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);
            if (a1.getSubmitDate().after(a2.getSubmitDate())) {
                similarityRepository.createSimilarity(key1.experiment_stu_test_no, key2.experiment_stu_test_no, sdf.format(new Date()), sim);
                logger.debug("creating edge " + key1 + " -> " + key2 + ", " + sim);
            } else {
                similarityRepository.createSimilarity(key2.experiment_stu_test_no, key1.experiment_stu_test_no, sdf.format(new Date()), sim);
                logger.debug("creating edge " + key2 + " <- " + key1 + ", " + sim);
            }
        } catch (Exception e) {
            logger.error("creating edge " + key1 + " -- " + key2 + ", " + sim);
        }
    }

    /**
     * 创建一次提交，若提交已存在则使用先前提交
     * @return assignment
     */
    private Assignment generateAssignment() throws NullPointerException {
        Assignment a1 = this.assignmentRepository.findByAssignmentid(this.submissionKey.experiment_stu_test_no);
        if (a1 != null) {
            return a1;
        }

        a1 = new Assignment();
        a1.setAssignmentid(this.submissionKey.experiment_stu_test_no);
        a1.setSubmitDate(new Date());
        a1 = this.assignmentRepository.save(a1);
        Student s = studentRepository.findByStudentid(this.stuno);
        Experiment e = experimentRepository.findByExperimentid(this.expno);

        this.assignmentRepository.createSubmitRelationship(s.getId(), a1.getId());
        this.assignmentRepository.createBelongtoRelationship(e.getId(), a1.getId());
        return a1;
    }

}
