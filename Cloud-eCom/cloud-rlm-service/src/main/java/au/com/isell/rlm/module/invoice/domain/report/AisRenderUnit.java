package au.com.isell.rlm.module.invoice.domain.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class AisRenderUnit {
	private String agentName;
	private String[] monthColumnsDef;
	private List<AgentBusinessTypeDataGroup> dataGroups;
	private AisRowModel total4Agent;
	private AisRowModel lastYear4AgentSum;
	private AisRowModel diffAmount;
	private AisRowModel diffPercentage;

	public String getAgentName() {
		return NullToBlankConvertor.convert(agentName);
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	@JsonSerialize(using = CustomArraySerializer.class)
	public String[] getMthColsDef() {
		return monthColumnsDef;
	}

	public void setMonthColumnsDef(String[] monthColumnsDef) {
		this.monthColumnsDef = monthColumnsDef;
	}

	public List<AgentBusinessTypeDataGroup> getDataGroups() {
		return dataGroups;
	}

	public void setDataGroups(List<AgentBusinessTypeDataGroup> dataGroups) {
		this.dataGroups = dataGroups;
	}

	public AisRowModel getAgtSum() {
		return total4Agent;
	}

	public void setTotal4Agent(AisRowModel total4Agent) {
		this.total4Agent = total4Agent;
	}

	public AisRowModel getPrevAgtSum() {
		return lastYear4AgentSum;
	}

	public void setLastYear4AgentSum(AisRowModel lastYear4AgentSum) {
		this.lastYear4AgentSum = lastYear4AgentSum;
	}

	public AisRowModel getDiffAmt() {
		return diffAmount;
	}

	public void setDiffAmount(AisRowModel diffAmount) {
		this.diffAmount = diffAmount;
	}

	public AisRowModel getDiffPct() {
		return diffPercentage;
	}

	public void setDiffPercentage(AisRowModel diffPercentage) {
		this.diffPercentage = diffPercentage;
	}
}

class CustomArraySerializer extends JsonSerializer<String[]> {
	@Override
	public void serialize(String[] value, JsonGenerator gen, SerializerProvider provider) throws IOException, JsonProcessingException {
		List<Map> r = new ArrayList();
		if (value != null) {
			for (String v : value) {
				Map m = new HashMap();
				m.put("v", v);
				r.add(m);
			}
		}
		gen.writeObject(r);
	}
}
