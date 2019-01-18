package cn.sdkd.ccse.jdbexes.neo4j.entities;

import cn.sdkd.ccse.jdbexes.neo4j.entities.base.AbstractEntity;
import cn.sdkd.ccse.jdbexes.neo4j.entities.base.DescriptiveEntity;
import cn.sdkd.ccse.jdbexes.neo4j.entities.relationships.Similarity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateString;

import java.util.Date;
import java.util.Set;

/**
 * Created by sam on 2019/1/12.
 * 学生提交的实验作业
 * 一个作业由一个学生提交，属于一个实验
 */
@NodeEntity
public class Assignment extends AbstractEntity {

    private Long assignmentid;

    @DateString
    private Date submitDate;

    @JsonBackReference
    @Relationship(type = "SUBMIT", direction = Relationship.INCOMING)
    private Set<Student> students;

    @JsonManagedReference
    @Relationship(type = "BELONGTO", direction = Relationship.INCOMING)
    private Set<Experiment> experiments;

    @JsonBackReference
    @Relationship(type = "SIMILARITY", direction = Relationship.UNDIRECTED)
    private Set<Similarity> similarities            ;

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

    public Set<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(Set<Experiment> experiments) {
        this.experiments = experiments;
    }

    public Set<Similarity> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(Set<Similarity> similarities) {
        this.similarities = similarities;
    }


}
