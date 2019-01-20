package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sam on 2019/1/14.
 */
public interface INeo4jService {

    List<Student> getStudents();

    List<Experiment> getExperiments();

    Object getSimilarities(@Param("sim") float sim);

    Object findSimilaritiesBySimValueExperimentid(@Param("sim") float sim, @Param("expid") Long expid);

    Object findSimilaritiesBySimValueStudentid(@Param("sim") float sim, @Param("stuid") Long stuid);

    List<Similarity> findSimilaritiesBySimValueExperimentidStudentid(@Param("sim") float sim, @Param("expid") Long expid, @Param("stuid") Long stuid);
}
