package cn.sdkd.ccse.jdbexes.neo4j.entities;

import cn.sdkd.ccse.jdbexes.neo4j.entities.base.DescriptiveEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Created by sam on 2019/1/12.
 * 实验
 */
@NodeEntity
public class Experiment extends DescriptiveEntity {

    private Long experimentid;

    @JsonBackReference
    @Relationship(type = "BELONGTO", direction = Relationship.OUTGOING)
    private Set<Assignment> assignments;

    public Experiment() {
    }

    public Experiment(Long experimentid, String name, String desc) {
        this.experimentid = experimentid;
        this.setName(name);
        this.setDescription(desc);
    }

    public Long getExperimentid() {
        return experimentid;
    }

    public void setExperimentid(Long experimentid) {
        this.experimentid = experimentid;
    }

    public Set<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<Assignment> assignments) {
        this.assignments = assignments;
    }
}
