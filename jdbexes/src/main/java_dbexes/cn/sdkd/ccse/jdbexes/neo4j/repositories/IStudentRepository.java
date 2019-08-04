package cn.sdkd.ccse.jdbexes.neo4j.repositories;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
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

    @Query("MATCH (t:Student)-[r:SUBMIT]->(p:Assignment)<-[r1:BELONGTO]-(e:Experiment) RETURN t,r,p,r1,e order by t.studentid ")
    List<Student> selectAll();

    /* 查询指定测试的相似度大于指定值simValue的联系相关的所有学生 */
    @Query("START a1 = node({assignmentid}) MATCH (a1)-[r:SIMILARITY]->(a2:Assignment), " +
            "(a1)-[r2:SUBMIT]-(s1:Student), (a2)-[r3:SUBMIT]-(s2:Student) " +
            " WHERE  s2.studentid <> s1.studentid and r.simValue >= {simValue}" +
            " RETURN s2 ")
    List<Student> findBySimValueAssignmentid(@Param("simValue") float simValue, @Param("assignmentid")Long assignmentid);
}