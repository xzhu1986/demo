package au.com.isell.rlm.module.invoice.domain.report;

import java.util.List;


public class AgentBusinessTypeDataGroup {

	private String groupName;// e.g. Agent iSell - New Business
	private List<AisRowModel> rowData;
	private AisRowModel colMonthAmountsSum;
	
	public String getGroupName() {
		return NullToBlankConvertor.convert(groupName);
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public List<AisRowModel> getRowData() {
		return rowData;
	}
	
	public void setRowData(List<AisRowModel> rowData) {
		this.rowData = rowData;
	}
	
	public AisRowModel getColMthAmtsSum() {
		return colMonthAmountsSum;
	}
	
	public void setColMonthAmountsSum(AisRowModel colMonthAmountsSum) {
		this.colMonthAmountsSum = colMonthAmountsSum;
	}
	
}