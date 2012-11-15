package au.com.isell.rlm.module.supplier.domain;

import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.rlm.module.schedule.domain.ScheduleExecute;

@ISellIndex(name="schedules", type="supplier_update")
public class SupplierUpdateExecute extends ScheduleExecute {
	private int dataSkillLevelRequired;
	private int priority;
	private int supplierId;
	private String supplierName;
	private String summary;

	public void setDataSkillLevelRequired(int dataSkillLevelRequired) {
		this.dataSkillLevelRequired = dataSkillLevelRequired;
	}

	@ISellIndexValue
	public int getDataSkillLevelRequired() {
		return dataSkillLevelRequired;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	@ISellIndexValue(wildcard=true)
	public String getSupplierName() {
		return supplierName;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	@ISellIndexValue
	public int getPriority() {
		return priority;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
	@ISellIndexValue(index="no")
	public String getSummary() {
		return summary;
	}
	@ISellIndexValue
	public int getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}
}
