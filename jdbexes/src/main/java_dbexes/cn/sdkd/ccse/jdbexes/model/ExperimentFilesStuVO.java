package cn.sdkd.ccse.jdbexes.model;

import java.sql.Timestamp;

/**
 * Created by sam on 2019/1/4.
 */
public class ExperimentFilesStuVO {

    private Integer expno;
    private Integer expstuno;

    private Integer stuno;
    private Integer fileno;
    private String srcfilename;
    private String dstfilename;

    private String file_content;
    private Timestamp submittime;

    private String testtarget;

    public Integer getExpno() {
        return expno;
    }

    public void setExpno(Integer expno) {
        this.expno = expno;
    }

    public Integer getExpstuno() {
        return expstuno;
    }

    public void setExpstuno(Integer expstuno) {
        this.expstuno = expstuno;
    }

    public Integer getStuno() {
        return stuno;
    }

    public void setStuno(Integer stuno) {
        this.stuno = stuno;
    }

    public Integer getFileno() {
        return fileno;
    }

    public void setFileno(Integer fileno) {
        this.fileno = fileno;
    }

    public String getSrcfilename() {
        return srcfilename;
    }

    public void setSrcfilename(String srcfilename) {
        this.srcfilename = srcfilename;
    }

    public String getDstfilename() {
        return dstfilename;
    }

    public void setDstfilename(String dstfilename) {
        this.dstfilename = dstfilename;
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

    public String getTesttarget() {
        return testtarget;
    }

    public void setTesttarget(String testtarget) {
        this.testtarget = testtarget;
    }
}
