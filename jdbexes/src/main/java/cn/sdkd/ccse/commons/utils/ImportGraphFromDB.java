package cn.sdkd.ccse.commons.utils;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestMapper;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wangzhixuan.model.User;
import com.wangzhixuan.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashSet;
import java.util.List;

/**
 * Created by sam on 2019/1/15.
 */
@ContextConfiguration(locations = {"classpath*:spring-config.xml"})
public enum ImportGraphFromDB {
    INSTANCE;

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

    public static void main(String[] args) {
        ImportGraphFromDB importer = ImportGraphFromDB.INSTANCE;
        /*第一步：增加所有实验*/
        importer.importExperiments();
        /*第二步：增加所有测试及相关学生*/
        importer.importAssignmentsByQuery();
    }


    /*第一步：增加所有实验*/
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

    /* 当联系和节点多时，效率较低,因为使用直接使用save保存
    第二步：增加所有学生以及测试*/
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

    /* 这个实现效率应该更高一些
    第二步：增加所有测试及相关学生*/
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

}
