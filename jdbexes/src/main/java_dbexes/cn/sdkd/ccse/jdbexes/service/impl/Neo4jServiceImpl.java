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
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Override
    public Assignment generateStudentAssignment(long stuno, long expno, long experiment_stu_test_no) {
        Assignment a1 = new Assignment();
        a1.setAssignmentid(experiment_stu_test_no);
        a1.setSubmitDate(new Date());
        a1 = assignmentRepository.save(a1);
        Student s = studentRepository.findByStudentid(stuno);
        Experiment e = experimentRepository.findByExperimentid(expno);

        assignmentRepository.createSubmitRelationship(s.getId(), a1.getId());
        assignmentRepository.createBelongtoRelationship(e.getId(), a1.getId());
        return a1;
    }

    @Override
    public List<Similarity> findSimilarityBy2Assignment(long assignmentid1, long assignmentid2) {
        return similarityRepository.findSimilarityBy2Assignmentid(assignmentid1, assignmentid2);
    }

    @Override
    public Similarity createSimilarity(long assignmentid1, long assignmentid2, float sim) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateString.ISO_8601);
        String date = sdf.format(new Date());
        return similarityRepository.createSimilarity(assignmentid1, assignmentid2, date, sim);
    }

}
