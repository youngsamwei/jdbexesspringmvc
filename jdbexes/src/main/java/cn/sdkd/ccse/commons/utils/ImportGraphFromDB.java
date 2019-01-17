package cn.sdkd.ccse.commons.utils;

import cn.sdkd.ccse.jdbexes.mapper.ExperimentStuTestMapper;
import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.model.ExperimentStuTest;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wangzhixuan.model.User;
import com.wangzhixuan.model.vo.UserVo;
import com.wangzhixuan.service.IUserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

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

    @Test
    public void test(){

    }
    /*第一步：增加所有实验*/
    @Test
    public void importExperiments() {
        List<Experiment> ls = experimentService.selectAll();
        for (Experiment e : ls) {
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
            if (s.getAssignments() == null){
                s.setAssignments(new HashSet<Assignment>() );
            }

            cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment e = experimentRepository.findByExperimentid(est.getExpno().longValue());
            /*如果实验不在图数据库中则创建*/
            if (e == null) {
                Experiment exp = experimentService.selectById(est.getExpno().longValue());
                e = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(est.getExpno().longValue(), exp.getExpname(), "");
                e = experimentRepository.save(e);
            }
            if (e.getAssignments() == null){
                e.setAssignments(new HashSet<Assignment>());
            }

            Assignment a = assignmentRepository.findByAssignmentid(est.getExperiment_stu_test_no().longValue());
            if (a == null) {
                a = new Assignment(est.getExperiment_stu_test_no().longValue(), est.getTesttime());
                a = assignmentRepository.save(a);
            }
            if(a.getStudents() == null){
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

        logger.debug(s.getSno() + ", " + s.getName() );
    }

    @Test
    public void selectExperiments() {

        cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment s = experimentRepository.findByExperimentid(6L);
        logger.debug(s.getExperimentid() + ", " + s.getName() );
    }

    @Test
    public void selectAssignments() {

        Assignment a = assignmentRepository.findOne(10L);
        logger.debug(a.getAssignmentid() + "" );
    }

}
