package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

import java.sql.Blob;
import java.sql.Timestamp;

/**
 * Created by sam on 2019/1/4.
 */
public class ExperimentFilesStu  implements java.io.Serializable {
    @TableId
    private Integer expfilestuno;
    private Integer fileno;
    private Integer expstuno;
    private String file_content;
    private Timestamp submittime;

    public Integer getExpfilestuno() {
        return expfilestuno;
    }

    public void setExpfilestuno(Integer expfilestuno) {
        this.expfilestuno = expfilestuno;
    }

    public Integer getFileno() {
        return fileno;
    }

    public void setFileno(Integer fileno) {
        this.fileno = fileno;
    }

    public Integer getExpstuno() {
        return expstuno;
    }

    public void setExpstuno(Integer expstuno) {
        this.expstuno = expstuno;
    }

    public String getFile_content() {
        return file_content;
    }

    public void setFile_content(String file_content) {
        this.file_content = file_content;
    }

    public Timestamp getSubmittime() {
        return submittime;
    }

    public void setSubmittime(Timestamp submittime) {
        this.submittime = submittime;
    }
}
