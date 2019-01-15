package cn.sdkd.ccse.jdbexes.neo4j.entities;

import cn.sdkd.ccse.jdbexes.neo4j.entities.base.AbstractEntity;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Date;
import java.util.Set;

/**
 * Created by sam on 2019/1/12.
 * 学生提交的实验作业
 * 一个作业由一个学生提交，属于一个实验
 */
@NodeEntity
public class Assignment extends AbstractEntity {

    @Relationship(type = "ASSIGNMENT", direction = Relationship.INCOMING)
    private Set<Student> students;

    Long assignmentid;

    private Date submitDate;

    public Assignment() {
    }

    public Assignment(Long assignmentid, Date submitDate) {
        this.assignmentid = assignmentid;
        this.submitDate = submitDate;
    }

    public Long getAssignmentid() {
        return assignmentid;
    }

    public void setAssignmentid(Long assignmentid) {
        this.assignmentid = assignmentid;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    public Date getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(Date submitDate) {
        this.submitDate = submitDate;
    }
}
