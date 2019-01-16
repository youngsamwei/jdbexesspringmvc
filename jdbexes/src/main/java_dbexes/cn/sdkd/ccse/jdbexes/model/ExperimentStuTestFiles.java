package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

/**
 * Created by sam on 2019/1/16.
 */
public class ExperimentStuTestFiles implements java.io.Serializable {

    @TableId
    private Integer experiment_stu_test_files_no;
    private Integer experiment_stu_test_no;
    private Integer experiment_files_stu_no;
    private Integer fileno;
    private Integer stuno;

    public ExperimentStuTestFiles() {
    }

    public ExperimentStuTestFiles(Integer experiment_stu_test_no, Integer experiment_files_stu_no, Integer fileno, Integer stuno) {
        this.experiment_stu_test_no = experiment_stu_test_no;
        this.experiment_files_stu_no = experiment_files_stu_no;
        this.fileno = fileno;
        this.stuno = stuno;
    }

    public Integer getExperiment_stu_test_files_no() {
        return experiment_stu_test_files_no;
    }

    public void setExperiment_stu_test_files_no(Integer experiment_stu_test_files_no) {
        this.experiment_stu_test_files_no = experiment_stu_test_files_no;
    }

    public Integer getExperiment_stu_test_no() {
        return experiment_stu_test_no;
    }

    public void setExperiment_stu_test_no(Integer experiment_stu_test_no) {
        this.experiment_stu_test_no = experiment_stu_test_no;
    }

    public Integer getExperiment_files_stu_no() {
        return experiment_files_stu_no;
    }

    public void setExperiment_files_stu_no(Integer experiment_files_stu_no) {
        this.experiment_files_stu_no = experiment_files_stu_no;
    }

    public Integer getFileno() {
        return fileno;
    }

    public void setFileno(Integer fileno) {
        this.fileno = fileno;
    }

    public Integer getStuno() {
        return stuno;
    }

    public void setStuno(Integer stuno) {
        this.stuno = stuno;
    }
}
