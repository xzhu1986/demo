package au.com.isell.rlm.module.invoice.domain.report;

import java.math.BigDecimal;

import au.com.isell.rlm.common.utils.LocaleNumberFormatter;

public class AgentInvoiceSummary {
	private String agentId;
	private String agentName;
	private String businessType;
	private String salesRepName;
	private BigDecimal[] amounts;

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getSalesRepName() {
		return salesRepName;
	}

	public void setSalesRepName(String salesRepName) {
		this.salesRepName = salesRepName;
	}

	public BigDecimal[] getAmounts() {
		return amounts;
	}

	public void setAmounts(BigDecimal[] amounts) {
		this.amounts = amounts;
	}
	
	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String[] toArray() {
		int amountsLength= amounts!=null?amounts.length:0;
		String[] arr= new String[3+amountsLength]; 
		arr[0]=agentName;
		arr[1]=businessType;
		arr[2]=salesRepName;
		if(amounts!=null){
			for(int i=0;i<amounts.length;i++){
				arr[3+i]=(amounts[i]!=null? LocaleNumberFormatter.format(amounts[i]):"");
			}
		}
		return arr;
	}
}
