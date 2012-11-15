package au.com.isell.rlm.module.invoice.domain.report;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import au.com.isell.rlm.common.utils.LocaleNumberFormatter;

public class AisRowModel {
	public static int arrySize = 12;

	private String rowName;
	private String[] monthAmounts;
	private String rowAmountsSum;
	private BigDecimal sumDec=BigDecimal.ZERO;

	public AisRowModel(String rowName, BigDecimal[] monthAmounts) {
		this.rowName = rowName;
		BigDecimal total = BigDecimal.ZERO;
		this.monthAmounts = new String[arrySize];
		if (monthAmounts != null) {
			for (int i = 0; i < monthAmounts.length; i++) {
				BigDecimal v = monthAmounts[i];
				v = v == null ? BigDecimal.ZERO : v;
				this.monthAmounts[i] = LocaleNumberFormatter.format(v);
				total = total.add(v);
			}
		}
		this.sumDec=total;
		this.rowAmountsSum = LocaleNumberFormatter.format(total);
	}

	public AisRowModel(String rowName, String[] monthAmounts, String rowAmountsSum) {
		this.rowName = rowName;
		this.monthAmounts = monthAmounts;
		this.rowAmountsSum = rowAmountsSum;
	}

	public String getRowName() {
		return NullToBlankConvertor.convert(rowName);
	}

	public void setRowName(String rowName) {
		this.rowName = rowName;
	}

	@JsonSerialize(using = CustomArraySerializer.class)
	public String[] getMthAmt() {
		if(monthAmounts==null) return null;
		for(int i=0;i<monthAmounts.length;i++){
			monthAmounts[i]=monthAmounts[i]==null?"":monthAmounts[i];
		}
		return monthAmounts;
	}

	public void setMonthAmounts(String[] monthAmounts) {
		this.monthAmounts = monthAmounts;
	}

	public String getSum() {
		return NullToBlankConvertor.convert(rowAmountsSum);
	}
	
	@JsonIgnore
	public BigDecimal getSumDec(){
		return sumDec;
	}

	public void setRowSum(String rowAmountsSum) {
		this.rowAmountsSum = rowAmountsSum;
	}

}