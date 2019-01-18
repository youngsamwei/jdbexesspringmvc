package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IStudentRepository extends GraphRepository<Student> {
    Student findByName(@Param("name") String name);

    Student findByNameLike(@Param("name") String name);

    Student findBySno(@Param("sno")String sno);

    Student findByStudentid(@Param("studentid")Long studentid);

    @Query("MATCH (t:Student {sno:{0}})-[r:SUBMIT]->(p:Assignment)<-[r1:BELONGTO]-(e:Experiment) RETURN t,r,p,r1,e")
    Student findBySnoDepth(String sno);

    @Query("MATCH (t:Student)-[r:SUBMIT]->(p:Assignment)<-[r1:BELONGTO]-(e:Experiment) RETURN t,r,p,r1,e")
    List<Student> selectAll();

}