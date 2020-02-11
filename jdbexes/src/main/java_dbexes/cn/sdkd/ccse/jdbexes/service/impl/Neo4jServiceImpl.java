package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.ISimilarityRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.INeo4jService;
import com.wangzhixuan.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 2019/1/14.
 */
@Service
public class Neo4jServiceImpl implements INeo4jService {
    @Autowired
    private IStudentRepository studentRepository;
    @Autowired
    private IAssignmentRepository assignmentRepository;
    @Autowired
    private IExperimentRepository experimentRepository;
    @Autowired
    private ISimilarityRepository similarityRepository;

    @Override
    public Object getSimilarities(float simValue) {
        return similarityRepository.findBySimValue(simValue);
    }

    @Override
    public Object findSimilaritiesBySimValueExperimentid(float sim, Long expid) {
        return similarityRepository.findBySimValueExperimentid(sim, expid);
    }

    @Override
    public Object findSimilaritiesBySimValueStudentid(float sim, Long stuid) {
        return similarityRepository.findBySimValueStudentid(sim, stuid);
    }

    @Override
    public List<Similarity> findSimilaritiesBySimValueExperimentidStudentid(float sim, Long expid, Long stuid) {
        return similarityRepository.findBySimValueExperimentidStudentid(sim, expid, stuid);
    }

    @Override
    public List<Student> getStudents() {
        return studentRepository.selectAll();
    }

    @Override
    public List<Experiment> getExperiments() {
        List<Experiment> ls = new ArrayList<>();
        experimentRepository.findAll().forEach(ls::add);
        return ls;
    }

    @Override
    public boolean createStudentIfNotExists(User user) {
        Student s = studentRepository.findByStudentid(user.getId());
        if (s == null) {
            s = new Student(user.getId(), user.getLoginName(), user.getName());
            s = studentRepository.save(s);
            return s.getId() != null;
        }
        return true;
    }

    @Override
    public boolean createExperimentIfNotExists(cn.sdkd.ccse.jdbexes.model.Experiment experiment) {
        Experiment e = experimentRepository.findByExperimentid(experiment.getExpno());
        if (e == null) {
            e = new Experiment(experiment.getExpno(), experiment.getExpname(), "");
            experimentRepository.save(e);
        }
        return true;
    }

    @Override
    public List<Student> findStudentBySimValueAssignmentid(float sim, long id) {
        return studentRepository.findBySimValueAssignmentid(sim, id);
    }

    @Override
    public Assignment findAssignmentByExperimentStuTestNo(long experiment_stu_test_no) {
        // 要求维持 experiment_stu_test_no === assignmentid
        return assignmentRepository.findByAssignmentid(experiment_stu_test_no);
    }

}
