package au.com.isell.rlm.module.report.domain;

import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.model.AbstractModel;

@IsellPath("data/report/template/${tplName}.info")
@XStreamAlias("report-tpl-history")
public class ReportTplHistory extends AbstractModel {
	private String tplName;
	private String tplHash;
	private Date uploadDate;

	public ReportTplHistory() {

	}

	public ReportTplHistory(String tplName, String tplHash, Date uploadDate) {
		super();
		this.tplName = tplName;
		this.tplHash = tplHash;
		this.uploadDate = uploadDate;
	}

	@IsellPathField
	public String getTplName() {
		return tplName;
	}

	public void setTplName(String tplName) {
		this.tplName = tplName;
	}

	public String getTplHash() {
		return tplHash;
	}

	public void setTplHash(String tplHash) {
		this.tplHash = tplHash;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

}
