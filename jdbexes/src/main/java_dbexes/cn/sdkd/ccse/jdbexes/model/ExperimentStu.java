package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

import java.sql.Timestamp;

/**
 * Examination entity. @author MyEclipse Persistence Tools
 */

public class ExperimentStu implements java.io.Serializable {

    // Fields
    @TableId
    private Long expstuno;
    private Long expno;
    private String expname;

    private Long stuno;
    private String stuname;

    private Timestamp selectedtime;

    private int status;

    private int teststatus;
    private String testdesc;
    private int simstatus;
    private String simdesc;
    // Constructors

    /**
     * default constructor
     */
    public ExperimentStu() {
    }

    public ExperimentStu(Long expstuno, Long expno, String expname, Long stuno,
                         String stuname, Timestamp selectedtime, int status) {
        this.expstuno = expstuno;
        this.expno = expno;
        this.expname = expname;
        this.stuno = stuno;
        this.stuname = stuname;
        this.selectedtime = selectedtime;
        this.status = status;
    }

    // Property accessors


    public Long getExpstuno() {
        return expstuno;
    }

    public void setExpstuno(Long expstuno) {
        this.expstuno = expstuno;
    }

    public Long getExpno() {
        return expno;
    }

    public void setExpno(Long expno) {
        this.expno = expno;
    }

    public String getExpname() {
        return expname;
    }

    public void setExpname(String expname) {
        this.expname = expname;
    }

    public Long getStuno() {
        return stuno;
    }

    public void setStuno(Long stuno) {
        this.stuno = stuno;
    }

    public String getStuname() {
        return stuname;
    }

    public void setStuname(String stuname) {
        this.stuname = stuname;
    }

    public Timestamp getSelectedtime() {
        return selectedtime;
    }

    public void setSelectedtime(Timestamp selectedtime) {
        this.selectedtime = selectedtime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTeststatus() {
        return teststatus;
    }

    public void setTeststatus(int teststatus) {
        this.teststatus = teststatus;
    }

    public String getTestdesc() {
        return testdesc;
    }

    public void setTestdesc(String testdesc) {
        this.testdesc = testdesc;
    }

    public int getSimstatus() {
        return simstatus;
    }

    public void setSimstatus(int simstatus) {
        this.simstatus = simstatus;
    }

    public String getSimdesc() {
        return simdesc;
    }

    public void setSimdesc(String simdesc) {
        this.simdesc = simdesc;
    }
}