package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

import java.sql.Timestamp;

/**
 * Created by sam on 2019/1/16.
 */
public class ExperimentStuTest implements java.io.Serializable {

    @TableId
    private Integer experiment_stu_test_no;
    private Integer expno;
    private Integer stuno;
    private Timestamp testtime;
    private String testdesc;
    private Integer teststatus;
    private String simdesc;
    private Integer simstatus;

    public ExperimentStuTest() {
    }

    public ExperimentStuTest(Integer expno, Integer stuno, Timestamp testtime, String testdesc, Integer teststatus, String simdesc, Integer simstatus) {
        this.expno = expno;
        this.stuno = stuno;
        this.testtime = testtime;
        this.testdesc = testdesc;
        this.teststatus = teststatus;
        this.simdesc = simdesc;
        this.simstatus = simstatus;
    }

    public Integer getExperiment_stu_test_no() {
        return experiment_stu_test_no;
    }

    public void setExperiment_stu_test_no(Integer experiment_stu_test_no) {
        this.experiment_stu_test_no = experiment_stu_test_no;
    }

    public Integer getExpno() {
        return expno;
    }

    public void setExpno(Integer expno) {
        this.expno = expno;
    }

    public Integer getStuno() {
        return stuno;
    }

    public void setStuno(Integer stuno) {
        this.stuno = stuno;
    }

    public Timestamp getTesttime() {
        return testtime;
    }

    public void setTesttime(Timestamp testtime) {
        this.testtime = testtime;
    }

    public String getTestdesc() {
        return testdesc;
    }

    public void setTestdesc(String testdesc) {
        this.testdesc = testdesc;
    }

    public Integer getTeststatus() {
        return teststatus;
    }

    public void setTeststatus(Integer teststatus) {
        this.teststatus = teststatus;
    }

    public String getSimdesc() {
        return simdesc;
    }

    public void setSimdesc(String simdesc) {
        this.simdesc = simdesc;
    }

    public Integer getSimstatus() {
        return simstatus;
    }

    public void setSimstatus(Integer simstatus) {
        this.simstatus = simstatus;
    }
}
