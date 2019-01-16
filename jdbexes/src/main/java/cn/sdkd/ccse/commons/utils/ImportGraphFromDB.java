package cn.sdkd.ccse.commons.utils;

import cn.sdkd.ccse.jdbexes.model.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.IExperimentService;
import cn.sdkd.ccse.jdbexes.service.IExperimentStuService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.wangzhixuan.model.User;
import com.wangzhixuan.service.IUserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    IExperimentStuService experimentStuService;

    @Autowired
    IExperimentRepository experimentRepository;
    @Autowired
    IStudentRepository studentRepository;
    @Autowired
    IAssignmentRepository assignmentRepository;

    /*第一步：增加所有实验*/
    @Test
    public void importExperiments(){
        List<Experiment> ls = experimentService.selectAll();
        for (Experiment e : ls){
            cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment ex = experimentRepository.findByExperimentid(e.getExpno());
            if (ex == null){
                ex = new cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment(e.getExpno(), e.getExpname(), "");
                experimentRepository.save(ex);
            }
        }
    }

    /*第二步：增加所有学生*/
    @Test
    public void importAssignments(){


        EntityWrapper<User> wrapper = new EntityWrapper<User>();
        wrapper.where("organization_Id = {0} or organization_Id = {1}", 12, 14);

        List<User> ls = userService.selectList(wrapper);
        for (User u : ls){
            Student s = studentRepository.findBySno(u.getLoginName());
            if (s == null){
                s = new Student(u.getId(), u.getLoginName(), u.getName());
                logger.info(s.getName());
//                studentRepository.save(s);
            }
        }
    }
    @Test
    public void test() {






        /*第三步：增加所有作业*/

        /*第四步：增加所有作业相似度*/

    }


}
