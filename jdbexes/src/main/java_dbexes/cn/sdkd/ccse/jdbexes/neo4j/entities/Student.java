package cn.sdkd.ccse.jdbexes.neo4j.entities;

import cn.sdkd.ccse.jdbexes.neo4j.entities.base.NamedEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by sam on 2019/1/12.
 * 学生
 */
@NodeEntity
public class Student extends NamedEntity {
    /*学号*/
    private String sno;
    private Long studentid;

    @JsonBackReference
    @Relationship(type="SUBMIT", direction=Relationship.OUTGOING)
    private Set<Assignment> assignments;

    public Student() {
    }

    public Student(Long studentid, String sno, String name) {
        this.studentid = studentid;
        this.setName(name);
        this.sno = sno;
    }

    public Long getStudentid() {
        return studentid;
    }

    public void setStudentid(Long studentid) {
        this.studentid = studentid;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public Set<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<Assignment> assignments) {
        this.assignments = assignments;
    }

}
