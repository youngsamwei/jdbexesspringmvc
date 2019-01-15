package cn.sdkd.ccse.jdbexes.neo4j.entities.relationships;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Student;
import cn.sdkd.ccse.jdbexes.neo4j.entities.base.AbstractEntity;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Date;

/**
 * Created by sam on 2019/1/12.
 */
@RelationshipEntity(type = "SUBMIT")
public class Submit extends AbstractEntity {

    @StartNode
    private Student student;
    @EndNode
    private Assignment assignment;

    private Date submitDate;

    public Submit(Long id, Student student, Assignment assignment, Date submitDate) {
        this.setId(id);
        this.student = student;
        this.assignment = assignment;
        this.submitDate = submitDate;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }
}
