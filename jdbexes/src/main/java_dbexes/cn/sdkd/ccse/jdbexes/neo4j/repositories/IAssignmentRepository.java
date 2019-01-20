package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IAssignmentRepository extends GraphRepository<Assignment> {
    Assignment findByAssignmentid(Long assignmentid);

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

}