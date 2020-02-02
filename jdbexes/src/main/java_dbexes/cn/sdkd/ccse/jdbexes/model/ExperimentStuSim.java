package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

/**
 * 相似度检测结果
 */

public class ExperimentStuSim implements java.io.Serializable {

    // Fields
    @TableId
    private Long expstuno;
    private String result;

    public ExperimentStuSim() {
    }

    public ExperimentStuSim(Long expstuno, String result) {
        this.expstuno = expstuno;
        this.result = result;
    }

    public Long getExpstuno() {
        return expstuno;
    }

    public void setExpstuno(Long expstuno) {
        this.expstuno = expstuno;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}