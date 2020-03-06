package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

public class ExperimentStuTestLog implements java.io.Serializable {
    @TableId
    private Integer experiment_stu_test_log_no;

    private Integer expno;
    private Integer stuno;
    private String content;

    public ExperimentStuTestLog() {
    }

    public ExperimentStuTestLog(Integer experiment_stu_test_log_no, Integer expno, Integer stuno, String content) {
        this.experiment_stu_test_log_no = experiment_stu_test_log_no;
        this.expno = expno;
        this.stuno = stuno;
        this.content = content;
    }

    public Integer getExperiment_stu_test_log_no() {
        return experiment_stu_test_log_no;
    }

    public void setExperiment_stu_test_log_no(Integer experiment_stu_test_log_no) {
        this.experiment_stu_test_log_no = experiment_stu_test_log_no;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
