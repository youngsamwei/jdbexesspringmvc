package cn.sdkd.ccse.jdbexes.service.impl;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IAssignmentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IExperimentRepository;
import cn.sdkd.ccse.jdbexes.neo4j.repositories.IStudentRepository;
import cn.sdkd.ccse.jdbexes.service.INeo4jService;
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

    @Override
    public Object getStudents() {
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
    public Object getExperiments() {
        List<Experiment> ls = new ArrayList<Experiment>();
        Iterator<Experiment> it = experimentRepository.findAll().iterator();
        while (it.hasNext()) {
            ls.add(it.next());
        }
        return ls;
    }
}
