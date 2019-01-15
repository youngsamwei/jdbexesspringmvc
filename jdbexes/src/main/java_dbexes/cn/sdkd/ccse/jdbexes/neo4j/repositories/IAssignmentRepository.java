package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IAssignmentRepository extends GraphRepository<Assignment> {
    Assignment findByAssignmentid(Long assignmentid);
}