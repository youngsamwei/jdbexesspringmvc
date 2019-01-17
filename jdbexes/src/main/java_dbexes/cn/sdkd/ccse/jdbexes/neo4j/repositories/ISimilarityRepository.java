package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Date;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface ISimilarityRepository extends GraphRepository<Similarity> {
    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) RETURN r")
    Similarity findSimilarityBy2ExperimentStuTestNo(Long experimentStuTestNoA, Long experimentStuTestNoB);

    @Query("MATCH (a1:Assignment {assignmentid:{0}}),(a2:Assignment {assignmentid:{1}}) " +
            " CREATE (a1)-[r:SIMILARITY {simValue : {3}, testDate:{2}}]->(a2) return r")
    Similarity createSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB, String testDate, Float simValue);

    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) " +
            " delete r")
    Similarity deleteSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB);
//    @Query("MATCH (a1:Assignment {assignmentid:{0}}),(a2:Assignment {assignmentid:{1}}) " +
//            " CREATE (a1)-[r:SIMILARITY {simValue:{3}} ]-(a2) return r")
//    Similarity createSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB, Date testDate, Float simValue);
}