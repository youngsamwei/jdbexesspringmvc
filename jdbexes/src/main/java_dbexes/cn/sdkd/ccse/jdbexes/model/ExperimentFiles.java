package cn.sdkd.ccse.jdbexes.model;

import com.baomidou.mybatisplus.annotations.TableId;


/**
 * Question entity. @author MyEclipse Persistence Tools
 */

public class ExperimentFiles implements java.io.Serializable {

	// Fields
	@TableId
	private Long fileno;

	private Long expno;
	private String srcfilename;
	private String dstfilename;

	// Constructors

	/** default constructor */
	public ExperimentFiles() {
	}

	/** minimal constructor */
	public ExperimentFiles(Long fileno, String srcfilename, String dstfilename) {
		this.fileno = fileno;
		this.srcfilename = srcfilename;
		this.dstfilename = dstfilename;
	}

	/** full constructor */
	public ExperimentFiles(Long quesid, Long experiment, String quesname,
                           String quescontent, String queseval) {
		this.expno = experiment;
		this.fileno = fileno;
		this.srcfilename = srcfilename;
		this.dstfilename = dstfilename;

	}

	// Property accessors


	public Long getFileno() {
		return fileno;
	}

	public void setFileno(Long fileno) {
		this.fileno = fileno;
	}

	public Long getExpno() {
		return expno;
	}

	public void setExpno(Long expno) {
		this.expno = expno;
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
}