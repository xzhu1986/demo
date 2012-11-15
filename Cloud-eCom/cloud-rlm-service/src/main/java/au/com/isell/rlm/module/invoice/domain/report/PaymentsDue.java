package au.com.isell.rlm.module.invoice.domain.report;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class PaymentsDue {
	private static final DecimalFormat DF = new DecimalFormat("###,###");
	
	private String name;
	private BigDecimal overdue;
	private Map<Date, BigDecimal> dueAmount;
	private BigDecimal later;
	private String probability;
	
	public PaymentsDue() {
		this.overdue = BigDecimal.ZERO;
		this.later =  BigDecimal.ZERO;
		this.dueAmount = new TreeMap<Date, BigDecimal>();
	}
	
	public PaymentsDue(String name) {
		this();
		setName(name);
	}
	
	public PaymentsDue(String name,String probability) {
		this();
		setName(name);
		setProbability(probability);
	}
	
	public BigDecimal getOverdue() {
		return overdue;
	}
	public void setOverdue(BigDecimal overdue) {
		this.overdue = overdue;
	}
	public Map<Date, BigDecimal> getDueAmount() {
		return dueAmount;
	}
	public void setDueAmount(Map<Date, BigDecimal> dueAmount) {
		this.dueAmount = dueAmount;
	}
	public BigDecimal getLater() {
		return later;
	}
	public void setLater(BigDecimal later) {
		this.later = later;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public PaymentsDue add(PaymentsDue... others) {
		PaymentsDue due = new PaymentsDue(this.name);
		BigDecimal overdue = this.overdue;
		BigDecimal later = this.later;
		Map<Date, BigDecimal> dueAmounts = new TreeMap<Date, BigDecimal>();
		dueAmounts.putAll(this.dueAmount);
		for (PaymentsDue other : others) {
			overdue = overdue.add(other.overdue);
			later = later.add(other.later);
			for (Map.Entry<Date, BigDecimal> otherAmounts:other.getDueAmount().entrySet()) {
				BigDecimal value = dueAmounts.get(otherAmounts.getKey());
				if (value == null) dueAmounts.put(otherAmounts.getKey(), otherAmounts.getValue());
				else dueAmounts.put(otherAmounts.getKey(), value.add(otherAmounts.getValue()));
			}
		}
		due.setProbability(this.probability);
		due.setOverdue(overdue);
		due.setDueAmount(dueAmounts);
		due.setLater(later);
		return due;
	}

	public String getProbability() {
		return probability;
	}

	public void setProbability(String probability) {
		this.probability = probability;
	}

	@Override
	public String toString() {
		String sixDueAmount = "";
		for (Map.Entry<Date, BigDecimal> amount:this.getDueAmount().entrySet()) {
			sixDueAmount+=amount.getValue()+"   ";
		}
		return "PaymentsDue [name=" + name + ", probability=" + probability + ", overdue=" + overdue + ", later=" + later
				+ ", sixDueAmount=" + sixDueAmount + "]";
	}
	
	public String[] toArray(){
		String[] array = new String[]{this.name,this.probability,DF.format(overdue.setScale(0, BigDecimal.ROUND_HALF_UP)),"0","0","0","0","0","0",DF.format(this.later.setScale(0, BigDecimal.ROUND_HALF_UP))};
		int i=0;
		for (BigDecimal value : this.dueAmount.values()) {
			array[i+3] = DF.format(value.setScale(0, BigDecimal.ROUND_HALF_UP));
			i++;
		}
		return array;
	}
	
}
