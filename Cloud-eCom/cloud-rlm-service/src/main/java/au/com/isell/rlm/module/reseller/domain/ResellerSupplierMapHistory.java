package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;
import java.util.UUID;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;

@IsellPath("data/reseller_supplier/${serialNo}/${supplierId}/history/${date}.info.json")
@ISellIndex(name="reseller_suppliers",type="history")
public class ResellerSupplierMapHistory extends AbstractModel{
	private String via;
	private String operate;
	private Date date;
	private Integer serialNo;
	private Integer supplierId;
	private String userName;
	private UUID userId;
	private boolean formImport = false;
	
	@ISellIndexKey
	public String getKey(){
		return serialNo+"_"+supplierId+"_"+date.getTime();
	}
	@ISellIndexValue
	public String getVia() {
		return via;
	}
	public void setVia(String via) {
		this.via = via;
	}
	@IsellPathField
	@ISellIndexValue
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@IsellPathField
	@ISellIndexValue
	public Integer getSupplierId() {
		return supplierId;
	}
	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}
	@ISellIndexValue
	public String getOperate() {
		return operate;
	}
	public void setOperate(String operate) {
		this.operate = operate;
	}
	@IsellPathField
	@ISellIndexValue
	public Integer getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}
	@ISellIndexValue
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	@ISellIndexValue
	public boolean isFormImport() {
		return formImport;
	}
	public void setFormImport(boolean formImport) {
		this.formImport = formImport;
	}

}
