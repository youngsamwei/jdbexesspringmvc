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
public interface ISimilarityRepository extends GraphRepository<Similarity>  {
    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) RETURN r, a1, a2")
    List<Similarity> findSimilarityBy2ExperimentStuTestNo(Long experimentStuTestNoA, Long experimentStuTestNoB);

    @Query("MATCH (a1:Assignment {assignmentid:{0}}),(a2:Assignment {assignmentid:{1}}) " +
            " CREATE (a1)-[r:SIMILARITY {simValue : {3}, testDate:{2}}]->(a2) return r")
    Similarity createSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB, String testDate, Float simValue);

    @Query("MATCH (a1:Assignment {assignmentid:{0}})-[r:SIMILARITY]-(a2:Assignment {assignmentid:{1}}) " +
            " delete r")
    Similarity deleteSimilarity(Long experimentStuTestNoA, Long experimentStuTestNoB);

    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment), " +
            " (s1:Student)-[r2:SUBMIT]-(a1), (s2:Student)-[r3:SUBMIT]-(a2)" +
            " where r.simValue >= {simValue}  return r, a1, a2, r2, r3,s1, s2 order by r.simValue desc")
    List<Similarity> findBySimValue(@Param("simValue")Float simValue);

    @Query("MATCH (a1:Assignment)-[r:SIMILARITY]->(a2:Assignment) return r, a1, a2 order by r.simValue desc")
    List<Similarity> findAllSimilarities();
}