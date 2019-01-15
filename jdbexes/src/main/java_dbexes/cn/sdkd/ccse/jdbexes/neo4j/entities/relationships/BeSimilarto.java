package cn.sdkd.ccse.jdbexes.neo4j.entities.relationships;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.base.AbstractEntity;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Date;

/**
 * Created by sam on 2019/1/12.
 * 作业间的相似关系.
 */
@RelationshipEntity(type="BESIMILAR_TO")
public class BeSimilarto extends AbstractEntity {
    @StartNode
    private Assignment assignmentA;
    @EndNode
    private Assignment AssignmentB;

    private float sim;
    private Date computeDate;

    public BeSimilarto(Long id, Assignment assignmentA, Assignment assignmentB, float sim, Date computeDate) {
        this.setId(id);
        this.assignmentA = assignmentA;
        AssignmentB = assignmentB;
        this.sim = sim;
        this.computeDate = computeDate;
    }

    public float getSim() {
        return sim;
    }

    public void setSim(float sim) {
        this.sim = sim;
    }

    public Assignment getAssignmentA() {
        return assignmentA;
    }

    public void setAssignmentA(Assignment assignmentA) {
        this.assignmentA = assignmentA;
    }

    public Assignment getAssignmentB() {
        return AssignmentB;
    }

    public void setAssignmentB(Assignment assignmentB) {
        AssignmentB = assignmentB;
    }

    public Date getComputeDate() {
        return computeDate;
    }

    public void setComputeDate(Date computeDate) {
        this.computeDate = computeDate;
    }
}
