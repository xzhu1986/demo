package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;

@ISellIndex(name="resellers", type="version")
@IsellPath("data/resellers/${serialNo}/versions/${version}.info.json")
@XStreamAlias("version-history")
public class VersionHistory extends AbstractModel {
	private int serialNo;
	private String version;
	private Date datetime;
	
	@ISellIndexKey
	public String getKey() {
		return serialNo+":"+version;
	}

	@ISellIndexValue
	@IsellPathField
	public int getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}

	@ISellIndexValue
	@IsellPathField
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@ISellIndexValue
	public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
}
