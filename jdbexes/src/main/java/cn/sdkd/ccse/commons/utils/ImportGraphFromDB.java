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
import java.util.*;

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
        List<Similarity> ls = this.similarityRepository.deleteSimilaritiesByStudentName("谭婷", "班鑫");

        logger.debug(ls.size() + "");
    }

    /*查询与指定测试相似的测试*/
    @Test
    public void testFindByStunoExpno(){
//        Student s = this.studentRepository.findByName("刘子笛");
        List<Assignment > las = this.assignmentRepository.findByStudentName("刘子笛");

        for(Assignment a : las) {
            logger.debug(a.getId() + "-----------------------------------------" + a.getAssignmentid());
            List<Similarity> ls = this.similarityRepository.findBySimValueAssignmentid(90f, a.getId());
            logger.debug("List<Similarity> : " + ls.size() + "");

            List<Student> lss = this.studentRepository.findBySimValueAssignmentid(90f, a.getId());
            logger.debug("List<Student> : " + lss.size() + "");

            List<Assignment> la = this.assignmentRepository.findBySimValueAssignmentid(90f, a.getId());
            logger.debug("List<Assignment> : " + la.size() + "");
        }
    }

    @Test
    public void testFindBySimValueExperimentidStudentid(){
        List<Similarity> ls = this.similarityRepository.findBySimValueExperimentidStudentid(100f, 6L, 127L);
        logger.debug(ls.size() + "");
    }

    @Test
    public void testFindBySimValueExperimentid(){
        Experiment e1 = this.experimentRepository.findByExperimentid(6L);
        List<Similarity> ls1 = this.similarityRepository.findBySimValueExperimentid(100f, e1.getId());

        Experiment e2 = this.experimentRepository.findByExperimentid(7L);
        List<Similarity> ls2 = this.similarityRepository.findBySimValueExperimentid(100f, e2.getId());

        List<Similarity> ls3 = this.similarityRepository.findBySimValue(100f);

        logger.debug(ls1.size() + " : " + ls2.size());
    }

    @Test
    public void testFindStudentByName(){
        Student s1 = this.studentRepository.findByName("谭婷");
        Student s2 = this.studentRepository.findByName("毛锟");
        logger.debug(s1.getName());
    }

    @Test
    public void testFindSimilaritiesByQuery() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);

        List<Similarity> ls = this.similarityRepository.checkSimilarities();
        for (Similarity s : ls) {
            logger.debug(sdf.format(s.getA1().getSubmitDate()) + " : " + sdf.format(s.getA2().getSubmitDate()) + " : "
                    + (s.getA1().getSubmitDate().after(s.getA2().getSubmitDate()) ? "after" : "not after"));
            this.similarityRepository.delete(s.getId());
            this.similarityRepository.createSimilarity(s.getA2().getAssignmentid(),
                    s.getA1().getAssignmentid(), sdf.format(s.getTestDate()), s.getSimValue());
        }
        logger.debug(ls.size() + "");

    }

    @Test
    public void testSimilarityDate() {
        List<Similarity> ls = this.similarityRepository.findSimilaritiesByStudentName(90f, "谭婷", "班鑫");
//        for (Similarity s : ls){
//            this.similarityRepository.delete(s.getId());
//        }
        logger.debug(ls.size() + "");

    }

    @Test
    public void testFindSimilarityByValue() {
        Long a1id = 1373L;
        Long a2id = 1413L;

//        Experiment e = this.experimentRepository.findByExperimentid(6L);
//        Assignment assignment = assignmentRepository.findBy2ExperimentStuTestNo(a1id, a2id);
//        Assignment assignment1 = assignmentRepository.findByAssignmentid(a1id);
//        Assignment assignment2 = assignmentRepository.findByAssignmentid(a2id);
        List<Similarity> ls = similarityRepository.findBySimValue(100f);
//        List<Similarity> sims = this.similarityRepository.findSimilarityBy2ExperimentStuTestNo(a1id, a2id);
//        List<Similarity> ls1 = similarityRepository.findAllSimilarities();

        logger.info(ls.size() + "");
    }

    /*测试日期时间属性的更新
    * 格式需要与设置的格式完全相同
    * */
    @Test
    public void testCreateSimilarity() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);

        List<Similarity> ls = similarityRepository.findBySimValue(new Float(20));

        Long a1id = 1373L;
        Long a2id = 1413L;
//        similarityRepository.deleteSimilarity(a1id, a2id);
//        Assignment a1 = this.assignmentRepository.findByAssignmentid(a1id);
//        Assignment a2 = this.assignmentRepository.findByAssignmentid(a2id);
//        if (a1 != null && a2 != null) {

        Similarity s = similarityRepository.createSimilarity(a1id, a2id, sdf.format(new Date()), new Float(20.0));

//        }
//        a1 = this.assignmentRepository.findByAssignmentid(a1id);
//        a2 = this.assignmentRepository.findByAssignmentid(a2id);

        ls = similarityRepository.findBySimValue(new Float(20));

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

    /*
     当联系和节点多时，效率较低,因为使用直接使用save保存
    第二步：增加所有学生以及测试*/
    @Test
    @Deprecated
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

    /*
    这个实现效率应该更高一些
    第二步：增加所有学生以及测试*/
    @Test
    public void importAssignmentsByQuery() {
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

            cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment e = experimentRepository.findByExperimentid(est.getExpno().longValue());
            /*如果实验不在图数据库中则创建*/
            if (e == null) {
                cn.sdkd.ccse.jdbexes.model.Experiment exp = experimentService.selectById(est.getExpno().longValue());
                e = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(est.getExpno().longValue(), exp.getExpname(), "");
                e = experimentRepository.save(e);
            }

            Assignment a = assignmentRepository.findByAssignmentid(est.getExperiment_stu_test_no().longValue());
            if (a == null) {
                a = new Assignment(est.getExperiment_stu_test_no().longValue(), est.getTesttime());
                a = assignmentRepository.save(a);
            }

            Assignment a1 = this.assignmentRepository.findBySubmit(s.getId(), a.getId());
            if (a1 == null) {
                a1 = this.assignmentRepository.createSubmitRelationship(s.getId(), a.getId());
            }
            Assignment a2 = this.assignmentRepository.findByBelongto(e.getId(), a.getId());
            if (a2 == null) {
                a2 = this.assignmentRepository.createBelongtoRelationship(e.getId(), a.getId());
            }

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
