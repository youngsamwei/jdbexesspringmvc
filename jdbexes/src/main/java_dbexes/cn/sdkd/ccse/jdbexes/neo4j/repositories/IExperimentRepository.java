package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IExperimentRepository extends GraphRepository<Experiment> {
    Experiment findByName(String name);
    Experiment findByExperimentid(Long experimentid);
}