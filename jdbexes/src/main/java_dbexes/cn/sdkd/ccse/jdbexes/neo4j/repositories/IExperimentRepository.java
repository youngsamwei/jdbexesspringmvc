package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IExperimentRepository extends GraphRepository<Experiment> {
    Experiment findByName(@Param("name") String name);

    Experiment findByNameLike(@Param("name") String name);

    Experiment findByExperimentid(@Param("experimentid")Long experimentid);
}