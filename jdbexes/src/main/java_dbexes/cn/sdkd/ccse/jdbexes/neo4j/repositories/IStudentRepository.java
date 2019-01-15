package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * The repository to perform CRUD operations on book entities
 */
public interface IStudentRepository extends GraphRepository<Student> {
    Student findByName(String name);
    Student findBySno(String sno);
    Student findByStudentid(Long studentid);
}