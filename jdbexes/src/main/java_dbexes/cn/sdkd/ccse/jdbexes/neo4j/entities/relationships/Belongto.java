package cn.sdkd.ccse.jdbexes.neo4j.entities.relationships;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.Experiment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.base.AbstractEntity;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * Created by sam on 2019/1/12.
 */
@RelationshipEntity(type="BELONG_TO")
public class Belongto extends AbstractEntity {

    @StartNode
    private Assignment assignment;
    @EndNode
    private Experiment experiment;

    public Belongto(Long id, Assignment assignment, Experiment experiment) {
        this.setId(id);
        this.assignment = assignment;
        this.experiment = experiment;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }
}
