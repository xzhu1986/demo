package au.com.isell.rlm.module.invoice.domain.report;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Forcast12Month {
	private static final DecimalFormat DF = new DecimalFormat("###,###");
	
	private String agentId;
	private String agentName;
	private String type;
	private BigDecimal[] amounts;
	
	public Forcast12Month() {
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal[] getAmounts() {
		return amounts;
	}

	public Map[] getFormatedAmounts() {
		Map<String,Object>[] arrayMaps = new HashMap[this.amounts.length];
		for(int i=0;i<this.amounts.length;i++){
			arrayMaps[i] = new HashMap<String, Object>(); 
			arrayMaps[i].put("v", this.amounts[i]==null?"0":DF.format(this.amounts[i].setScale(0, BigDecimal.ROUND_HALF_UP)));
		}
		return arrayMaps;
	}
	
	public void setAmounts(BigDecimal[] amounts) {
		this.amounts = amounts;
	}

	public String[] toArray(){
		String[] array = new String[this.amounts.length+2];
		array[0] = this.agentName;
		array[1] = this.type;
		for(int i=0;i<this.amounts.length;i++){
			array[i+2] = this.amounts[i]==null?"0":DF.format(this.amounts[i].setScale(0, BigDecimal.ROUND_HALF_UP));
		}
		return array;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	
}
