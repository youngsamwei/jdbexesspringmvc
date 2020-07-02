package cn.sdkd.ccse.jdbexes.service;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import com.wangzhixuan.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    boolean createStudentIfNotExists(User user);

    boolean createExperimentIfNotExists(cn.sdkd.ccse.jdbexes.model.Experiment experiment);

    List<Student> findStudentBySimValueAssignmentid(float sim, long id);

    Assignment findAssignmentByExperimentStuTestNo(long experiment_stu_test_no);

    Assignment generateStudentAssignment(long stuno, long expno, long experiment_stu_test_no);

    List<Similarity> findSimilarityBy2Assignment(long assignmentid1, long assignmentid2);

    Similarity createSimilarity(long assignmentid1, long assignmentid2, float sim);
}
