package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;

/**
 * Examination entity. @author MyEclipse Persistence Tools
 */

public class Experiment implements java.io.Serializable {

	// Fields
	@TableId
	private Long expno;
	private String expname;

	private String docker_image;
	private String testtarget;
	private Integer memory_limit;
	private Integer timeout;

	private Short isOpen;

	// Constructors

	/** default constructor */
	public Experiment() {
	}

	/** minimal constructor */
	public Experiment(String expname) {
		this.expname = expname;
	}

	/** full constructor */
	public Experiment(String expname, Short isOpen) {
		this.expname = expname;
		this.isOpen = isOpen;
	}

	// Property accessors

	public Long getExpno() {
		return this.expno;
	}

	public void setExpno(Long expno) {
		this.expno = expno;
	}

	public String getExpname() {
		return this.expname;
	}

	public void setExpname(String expname) {
		this.expname = expname;
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

	public Short getIsOpen() {
		return this.isOpen;
	}

	public void setIsOpen(Short isOpen) {
		this.isOpen = isOpen;
	}

}