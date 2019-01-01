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

	public Short getIsOpen() {
		return this.isOpen;
	}

	public void setIsOpen(Short isOpen) {
		this.isOpen = isOpen;
	}


}