package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IAssignmentRepository extends GraphRepository<Assignment> {
    Assignment findByAssignmentid(Long assignmentid);

    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) RETURN a1")
    Assignment findBy2ExperimentStuTestNo(Long experimentStuTestNoA, Long experimentStuTestNoB);


}