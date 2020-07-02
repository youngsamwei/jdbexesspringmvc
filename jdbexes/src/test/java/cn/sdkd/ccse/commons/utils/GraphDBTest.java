package cn.sdkd.ccse.commons.utils;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestMapper;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring-config.xml"})
public class GraphDBTest {
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

    @Test
    public void testFindStudentBySid() {
        Student s = this.studentRepository.findByStudentid(402L);

        logger.debug(s.getName());
    }

    /*查询与指定测试相似的测试*/
    @Test
    public void testFindByStunoExpno() {
//        Student s = this.studentRepository.findByName("刘子笛");
        List<Assignment> las = this.assignmentRepository.findByStudentName("刘子笛");

        for (Assignment a : las) {
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
    public void testFindBySimValueExperimentidStudentid() {
        List<Similarity> ls = this.similarityRepository.findBySimValueExperimentidStudentid(100f, 6L, 127L);
        logger.debug(ls.size() + "");
    }

    @Test
    public void testFindBySimValueExperimentid() {
        Experiment e1 = this.experimentRepository.findByExperimentid(6L);
        List<Similarity> ls1 = this.similarityRepository.findBySimValueExperimentid(100f, e1.getId());

        Experiment e2 = this.experimentRepository.findByExperimentid(7L);
        List<Similarity> ls2 = this.similarityRepository.findBySimValueExperimentid(100f, e2.getId());

        List<Similarity> ls3 = this.similarityRepository.findBySimValue(100f);

        logger.debug(ls1.size() + " : " + ls2.size());
    }

    @Test
    public void testFindStudentByName() {
        Student s1 = this.studentRepository.findByName("石乐山");
        Student s2 = this.studentRepository.findByName("吴同洲");

        List<Student> listStudent = this.studentRepository.selectAll();

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

        List<Similarity> ls = similarityRepository.findBySimValue(20f);

        Long a1id = 1373L;
        Long a2id = 1413L;
//        similarityRepository.deleteSimilarity(a1id, a2id);
//        Assignment a1 = this.assignmentRepository.findByAssignmentid(a1id);
//        Assignment a2 = this.assignmentRepository.findByAssignmentid(a2id);
//        if (a1 != null && a2 != null) {

        Similarity s = similarityRepository.createSimilarity(a1id, a2id, sdf.format(new Date()), 20.0f);

//        }
//        a1 = this.assignmentRepository.findByAssignmentid(a1id);
//        a2 = this.assignmentRepository.findByAssignmentid(a2id);

        ls = similarityRepository.findBySimValue(20f);

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

            Similarity sim_relation = new Similarity(a1, a2, new Date(), 30.30303f);

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
