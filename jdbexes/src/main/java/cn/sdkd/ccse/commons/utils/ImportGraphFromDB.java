package cn.sdkd.ccse.commons.utils;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wangzhixuan.model.User;
import com.wangzhixuan.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Created by sam on 2019/1/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-config.xml"})
public class ImportGraphFromDB {
    private static Logger logger = LoggerFactory.getLogger(ImportGraphFromDB.class);

    @Autowired
    IExperimentService experimentService;
    @Autowired
    IUserService userService;

    @Autowired
    ExperimentStuTestMapper experimentStuTestMapper;

    @Autowired
    IExperimentRepository experimentRepository;
    @Autowired
    IStudentRepository studentRepository;
    @Autowired
    IAssignmentRepository assignmentRepository;
    @Autowired
    private ISimilarityRepository similarityRepository;

    @Test
    public void test() {

    }

    @Test
    public void testFindSimilarityByValue() {
        Long a1id = 1373L;
        Long a2id = 1413L;

//        Experiment e = this.experimentRepository.findByExperimentid(6L);
//        Assignment assignment = assignmentRepository.findBy2ExperimentStuTestNo(a1id, a2id);
//        Assignment assignment1 = assignmentRepository.findByAssignmentid(a1id);
//        Assignment assignment2 = assignmentRepository.findByAssignmentid(a2id);
        List<Similarity> ls = similarityRepository.findSimilarityByValue(new Float(100));
        List<Similarity> sims = this.similarityRepository.findSimilarityBy2ExperimentStuTestNo(a1id, a2id);
        List<Similarity> ls1 = similarityRepository.findAllSimilarities();

        logger.info(ls.size() + "");
    }

    /*测试日期时间属性的更新
    * 格式需要与设置的格式完全相同
    * */
    @Test
    public void testCreateSimilarity() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);

        List<Similarity> ls = similarityRepository.findSimilarityByValue(new Float(20));

        Long a1id = 1373L;
        Long a2id = 1413L;
//        similarityRepository.deleteSimilarity(a1id, a2id);
//        Assignment a1 = this.assignmentRepository.findByAssignmentid(a1id);
//        Assignment a2 = this.assignmentRepository.findByAssignmentid(a2id);
//        if (a1 != null && a2 != null) {

        Similarity s =  similarityRepository.createSimilarity(a1id, a2id, sdf.format(new Date()), new Float(20.0));

//        }
//        a1 = this.assignmentRepository.findByAssignmentid(a1id);
//        a2 = this.assignmentRepository.findByAssignmentid(a2id);

        ls = similarityRepository.findSimilarityByValue(new Float(20));

//        a1 = this.assignmentRepository.save(a1);
        Assignment assignment = assignmentRepository.findBy2ExperimentStuTestNo(a1id, a2id);
        assertNotNull(assignment);

//        similarityRepository.deleteSimilarity(a1id, a2id);
//        assignment = assignmentRepository.findBy2ExperimentStuTestNo(a1id, a2id);
//        assertNull(assignment);
    }

    @Test
    public void testFindAssignment() {
        Long a1id = 1373L;
        Long a2id = 1413L;
        Assignment a1 = this.assignmentRepository.findByAssignmentid(a1id);
        Assignment a2 = this.assignmentRepository.findByAssignmentid(a2id);
        if (a1 != null && a2 != null) {
            if (a1.getSimilarities() == null) {
                Set<Similarity> similarities = new HashSet<Similarity>();
                a1.setSimilarities(similarities);
            }
            if (a2.getSimilarities() == null) {
                Set<Similarity> similarities = new HashSet<Similarity>();
                a2.setSimilarities(similarities);
            }

            Similarity sim_relation = new Similarity(a1, a2, new Date(), new Float(30.30303));

            a1.getSimilarities().add(sim_relation);
            a2.getSimilarities().add(sim_relation);

            a1 = this.assignmentRepository.save(a1);
            a2 = this.assignmentRepository.save(a2);

        }
        Assignment assignment = assignmentRepository.findBy2ExperimentStuTestNo(a1id, a2id);
        logger.debug(assignment.toString());
        if (a1 != null) {
            a1.getSimilarities().clear();
            this.assignmentRepository.save(a1);
        }
        if (a2 != null) {
            a2.getSimilarities().clear();
            this.assignmentRepository.save(a2);
        }
    }

    /*第一步：增加所有实验*/
    @Test
    public void importExperiments() {
        List<cn.sdkd.ccse.jdbexes.model.Experiment> ls = experimentService.selectAll();
        for (cn.sdkd.ccse.jdbexes.model.Experiment e : ls) {
            cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment ex = experimentRepository.findByExperimentid(e.getExpno());
            if (ex == null) {
                ex = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(e.getExpno(), e.getExpname(), "");
                experimentRepository.save(ex);
            }
        }
    }

    /*第二步：增加所有学生以及测试*/
    @Test
    public void importAssignments() {
        EntityWrapper<ExperimentStuTest> wrapper = new EntityWrapper<ExperimentStuTest>();
        List<ExperimentStuTest> lses = experimentStuTestMapper.selectList(wrapper);
        for (ExperimentStuTest est : lses) {

            Student s = studentRepository.findByStudentid(est.getStuno().longValue());
            /*如果学生不在图数据库中则创建*/
            if (s == null) {
                User u = userService.selectById(est.getStuno().longValue());
                s = new Student(est.getStuno().longValue(), u.getLoginName(), u.getName());
                s = studentRepository.save(s);
            }
            if (s.getAssignments() == null) {
                s.setAssignments(new HashSet<Assignment>());
            }

            cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment e = experimentRepository.findByExperimentid(est.getExpno().longValue());
            /*如果实验不在图数据库中则创建*/
            if (e == null) {
                cn.sdkd.ccse.jdbexes.model.Experiment exp = experimentService.selectById(est.getExpno().longValue());
                e = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(est.getExpno().longValue(), exp.getExpname(), "");
                e = experimentRepository.save(e);
            }
            if (e.getAssignments() == null) {
                e.setAssignments(new HashSet<Assignment>());
            }

            Assignment a = assignmentRepository.findByAssignmentid(est.getExperiment_stu_test_no().longValue());
            if (a == null) {
                a = new Assignment(est.getExperiment_stu_test_no().longValue(), est.getTesttime());
                a = assignmentRepository.save(a);
            }
            if (a.getStudents() == null) {
                a.setStudents(new HashSet<Student>());
            }
            a.getStudents().add(s);
            a = assignmentRepository.save(a);

            if (a.getExperiments() == null) {
                a.setExperiments(new HashSet<cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment>());
            }
            a.getExperiments().add(e);

            s.getAssignments().add(a);
            e.getAssignments().add(a);

            e = experimentRepository.save(e);
            s = studentRepository.save(s);
            a = assignmentRepository.save(a);

            logger.debug(s.getSno() + ", " + s.getName() + ", " + e.getName() + ", " + est.getTesttime());
        }

    }

    @Test
    public void selectStudents() {

        Student s = studentRepository.findBySnoDepth("201601060505");

        List<Student> ls = studentRepository.selectAll();

        logger.debug(s.getSno() + ", " + s.getName());
    }

    @Test
    public void selectExperiments() {

        cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment s = experimentRepository.findByExperimentid(6L);
        logger.debug(s.getExperimentid() + ", " + s.getName());
    }

    @Test
    public void selectAssignments() {

        Assignment a = assignmentRepository.findOne(10L);
        logger.debug(a.getAssignmentid() + "");
    }

}
