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
    private String objfilename;

    private String file_content;
    private Timestamp submittime;

    private String docker_image;
    private String testtarget;
    private Integer memory_limit;
    private Integer timeout;

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

    public String getObjfilename() {
        return objfilename;
    }

    public void setObjfilename(String objfilename) {
        this.objfilename = objfilename;
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

    public String getDocker_image() {
        return docker_image;
    }

    public void setDocker_image(String docker_image) {
        this.docker_image = docker_image;
    }

    public Integer getMemory_limit() {
        return memory_limit;
    }

    public void setMemory_limit(Integer memoty_limit) {
        this.memory_limit = memoty_limit;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getTesttarget() {
        return testtarget;
    }

    public void setTesttarget(String testtarget) {
        this.testtarget = testtarget;
    }
}
