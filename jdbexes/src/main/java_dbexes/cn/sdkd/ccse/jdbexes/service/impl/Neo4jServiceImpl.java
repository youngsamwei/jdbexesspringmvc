package cn.sdkd.ccse.jdbexes.service.impl;

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
import java.util.Iterator;
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
    public List<Student>  getStudents() {
        return studentRepository.selectAll();
//        List<Student> ls = new ArrayList<Student>();
//        Iterator<Student> it = studentRepository.findAll().iterator();
//        while (it.hasNext()) {
//            Student s = it.next();
//
//            Iterator<Assignment> ia = s.getAssignments().iterator();
//            while (ia.hasNext()) {
//                if (ia.next().getExperiments() != null) {
//                    Iterator<Experiment> ie = ia.next().getExperiments().iterator();
//                    while (ie.hasNext()) {
//                        ie.next().setAssignments(new HashSet<Assignment>());
//                    }
//                }
//            }
//            ls.add(s);
//        }
//        return ls;
    }

    @Override
    public  List<Experiment> getExperiments() {
        List<Experiment> ls = new ArrayList<Experiment>();
        Iterator<Experiment> it = experimentRepository.findAll().iterator();
        while (it.hasNext()) {
            ls.add(it.next());
        }
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

}
