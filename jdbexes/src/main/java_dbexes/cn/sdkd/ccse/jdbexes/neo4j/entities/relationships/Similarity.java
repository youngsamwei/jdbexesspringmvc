package cn.sdkd.ccse.jdbexes.neo4j.entities.relationships;

import cn.sdkd.ccse.jdbexes.neo4j.entities.Assignment;
import cn.sdkd.ccse.jdbexes.neo4j.entities.base.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.typeconversion.DateString;

import java.util.Date;

@RelationshipEntity(type="SIMILARITY")
public class Similarity extends AbstractEntity {
   @JsonManagedReference
   @StartNode
   private Assignment a1;

   @JsonManagedReference
   @EndNode
   private Assignment a2;

   @DateString
   private Date testDate;

   private Float simValue;

   public Similarity() {
   }

   public Similarity(Assignment a1, Assignment a2, Date testDate, Float simValue) {
      this.a1 = a1;
      this.a2 = a2;
      this.testDate = testDate;
      this.simValue = simValue;
   }

   public Assignment getA1() {
      return a1;
   }

   public void setA1(Assignment a1) {
      this.a1 = a1;
   }

   public Assignment getA2() {
      return a2;
   }

   public void setA2(Assignment a2) {
      this.a2 = a2;
   }

   public Date getTestDate() {
      return testDate;
   }

   public void setTestDate(Date testDate) {
      this.testDate = testDate;
   }

   public Float getSimValue() {
      return simValue;
   }

   public void setSimValue(Float simValue) {
      this.simValue = simValue;
   }
}