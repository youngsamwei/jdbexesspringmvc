package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IAssignmentRepository extends GraphRepository<Assignment> {
    @Query("MATCH (a1:Assignment {assignmentid:{assignmentid}}) RETURN a1")
    Assignment findByAssignmentid(@Param("assignmentid") Long assignmentid);

    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]->(a2:Assignment {assignmentid:{1}}) RETURN a1,r")
    Assignment findBy2ExperimentStuTestNo(Long experimentStuTestNoA, Long experimentStuTestNoB);

    @Query("START s = node({sid}), a = node({aid}) MATCH (s)-[r:SUBMIT]->(a) RETURN a,s")
    Assignment findBySubmit(@Param("sid")Long sid, @Param("aid")Long aid);

    @Query("START e = node({eid}), a = node({aid}) MATCH (e)-[r:BELONGTO]->(a) RETURN a,e")
    Assignment findByBelongto(@Param("eid")Long eid, @Param("aid")Long aid);

    @Query("START s = node({sid}), a = node({aid}) " +
            " CREATE (s)-[r:SUBMIT]->(a) RETURN a,r,s")
    Assignment createSubmitRelationship(@Param("sid")Long sid, @Param("aid")Long aid);

    @Query("START e = node({eid}), a = node({aid}) " +
            " CREATE (e)-[r:BELONGTO]->(a) RETURN a,r,e")
    Assignment createBelongtoRelationship(@Param("eid")Long eid, @Param("aid")Long aid);

    @Query("MATCH (a:Assignment) RETURN a")
    List<Assignment> findList();

    @Query("MATCH (s1:Student { name : {studentName}})-[r2:SUBMIT]-(a1:Assignment) RETURN a1, r2, s1")
    List<Assignment> findByStudentName(@Param("studentName")String studentName);

    @Query("MATCH (s:Student {studentid : {studentid}})-[r1:SUBMIT]-(a:Assignment)" +
            "<-[r2:BELONGTO]-(e:Experiment { experimentid : {experimentid}}) RETURN a, r1, r2, s, e")
    List<Assignment> findByStudentIdExpId(@Param("studentid") Long studentid, @Param("experimentid") Long experimentid);

    /* 查询指定测试的相似度大于指定值simValue的联系 */
    @Query("START a1 = node({assignmentid}) MATCH (a1)-[r:SIMILARITY]->(a2:Assignment), " +
            "(a1)-[r2:SUBMIT]-(s1:Student), (a2)-[r3:SUBMIT]-(s2:Student) " +
            " WHERE  s2.studentid <> s1.studentid and r.simValue >= {simValue}" +
            " RETURN a2 ")
    List<Assignment> findBySimValueAssignmentid(@Param("simValue") float simValue, @Param("assignmentid")Long assignmentid);

}