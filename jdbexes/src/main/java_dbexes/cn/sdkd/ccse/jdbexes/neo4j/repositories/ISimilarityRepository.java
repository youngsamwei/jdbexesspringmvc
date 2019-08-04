package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface ISimilarityRepository extends GraphRepository<Similarity> {
    /*查询两个测试之间的相似度联系*/
    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) RETURN r, a1, a2")
    List<Similarity> findSimilarityBy2ExperimentStuTestNo(Long experimentStuTestNoA, Long experimentStuTestNoB);

    /*建立两个测试之间的相似度联系*/
    @Query("MATCH (a1:Assignment {assignmentid:{0}}),(a2:Assignment {assignmentid:{1}}) " +
            " CREATE (a1)-[r:SIMILARITY {simValue : {3}, testDate:{2}}]->(a2) return r")
    Similarity createSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB, String testDate, Float simValue);

    /*删除两个测试之间的相似度联系*/
    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) " +
            " delete r")
    Similarity deleteSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB);

    /*查询所有实验的测试相似度大于等于simValue的联系*/
    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment), " +
            " (s1:Student)-[r2:SUBMIT]-(a1), (s2:Student)-[r3:SUBMIT]-(a2)" +
            " where r.simValue >= {simValue}  return r, a1, a2, r2, r3,s1, s2 order by r.simValue desc")
    List<Similarity> findBySimValue(@Param("simValue") Float simValue);

    /*查询指定实验的测试相似度大于等于simValue的联系*/
    @Query("MATCH (exp:Experiment {experimentid:{expid}}), (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment), " +
            " (s1:Student)-[r2:SUBMIT]-(a1), (s2:Student)-[r3:SUBMIT]-(a2)," +
            " (exp)-[r4:BELONGTO]->(a1),  (exp)-[r5:BELONGTO]->(a2) " +
            " where r.simValue >= {simValue}  return r, a1, a2, r2, r3,s1, s2, exp order by r.simValue desc")
    List<Similarity> findBySimValueExperimentid(@Param("simValue") Float simValue, @Param("expid")Long expid);

    /*查询指定实验的测试相似度大于等于simValue的联系，同一个学生提交的不同的测试也比较相似度*/
    @Query("MATCH (exp:Experiment {experimentid:{expid}}), (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment), " +
            " (s1:Student)-[r2:SUBMIT]-(a1), (s2:Student)-[r3:SUBMIT]-(a2)," +
            " (exp)-[r4:BELONGTO]->(a1),  (exp)-[r5:BELONGTO]->(a2) " +
            " where r.simValue >= {simValue} and (s1.studentid = {stuid} or s2.studentid ={stuid}) " +
            " return r, a1, a2, r2, r3,s1, s2, exp order by r.simValue desc")
    List<Similarity> findBySimValueExperimentidStudentid(@Param("simValue") Float simValue, @Param("expid")Long expid, @Param("stuid") Long stuid);

    /*查询指定学生的测试相似度大于等于simValue的联系*/
    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment), " +
            " (s1:Student)-[r2:SUBMIT]-(a1), (s2:Student)-[r3:SUBMIT]-(a2) " +
            " where r.simValue >= {simValue} and (s1.studentid = {stuid} or s2.studentid ={stuid}) " +
            " return r, a1, a2, r2, r3,s1, s2 order by r.simValue desc")
    List<Similarity> findBySimValueStudentid(@Param("simValue") Float simValue, @Param("stuid") Long stuid);

    /*查询所有的相似度*/
    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment) return r, a1, a2 order by r.simValue desc")
    List<Similarity> findAllSimilarities();

    /*查询两个学生姓名之间的相似度联系，且相似度大于等于simValue*/
    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]-(a2:Assignment), " +
            " (s1:Student {name:{s1Name}})-[r2:SUBMIT]-(a1), (s2:Student {name:{s2Name}})-[r3:SUBMIT]-(a2)" +
            " where r.simValue >= {simValue}  return r, a1, a2, r2, r3,s1, s2 order by r.simValue desc")
    List<Similarity> findSimilaritiesByStudentName(@Param("simValue") Float simValue, @Param("s1Name") String s1Name, @Param("s2Name") String s2Name);

    /*删除两个学生之间的相似度联系*/
    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]-(a2:Assignment), " +
            " (s1:Student {name:{s1Name}})-[r2:SUBMIT]-(a1), (s2:Student {name:{s2Name}})-[r3:SUBMIT]-(a2)" +
            " delete r")
    List<Similarity> deleteSimilaritiesByStudentName(@Param("s1Name") String s1Name, @Param("s2Name") String s2Name);

    /*检查违反日期先后顺序约束的相似度联系*/
    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment), " +
            " (s1:Student)-[r2:SUBMIT]-(a1), (s2:Student)-[r3:SUBMIT]-(a2)" +
            " where a1.submitDate < a2.submitDate  return r, a1, a2, r2, r3,s1, s2 ")
    List<Similarity> checkSimilarities();

    /* 查询指定测试的相似度大于指定值simValue的联系 */
    @Query("START a1 = node({assignmentid}) MATCH (a1)-[r:SIMILARITY]->(a2:Assignment), " +
            "(a1)-[r2:SUBMIT]-(s1:Student), (a2)-[r3:SUBMIT]-(s2:Student) " +
            " WHERE  s2.studentid <> s1.studentid and r.simValue >= {simValue}" +
            " RETURN r,a1,a2,s1,s2,r2,r3 ")
    List<Similarity> findBySimValueAssignmentid(@Param("simValue") float simValue, @Param("assignmentid")Long assignmentid);

}