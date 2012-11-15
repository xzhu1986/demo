package au.com.isell.rlm.module.reseller.domain;

import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;


public class ResellerUsage {

	private int quotingUsers;
	private int orderingUsers;
	private int opportunityUsers;
	private int jobUsers;
	private int freeUsers;
	private int totalUsers;
	private int quoteCount;
	private int orderCount;
	private int jobCount;
	private int ecomLogins;
	private int localProductCount;
	private int localActiveProductCount;
	private int localVendorCount;
	private int localActiveVendorCount;
	private long epdProductCount;
	private long epdImageCount;
	private long epdMatchCount;
	private Date epdImportStartdate;
	private Date epdImportFinishdate;
	private String epdImportStatus;
	private String systemInfo;

	public int getQuotingUsers() {
		return quotingUsers;
	}
	public void setQuotingUsers(int quotingUsers) {
		this.quotingUsers = quotingUsers;
	}
	public int getOrderingUsers() {
		return orderingUsers;
	}
	public void setOrderingUsers(int orderingUsers) {
		this.orderingUsers = orderingUsers;
	}
	public int getOpportunityUsers() {
		return opportunityUsers;
	}
	public void setOpportunityUsers(int opportunityUsers) {
		this.opportunityUsers = opportunityUsers;
	}
	public int getJobUsers() {
		return jobUsers;
	}
	public void setJobUsers(int jobUsers) {
		this.jobUsers = jobUsers;
	}
	public int getFreeUsers() {
		return freeUsers;
	}
	public void setFreeUsers(int freeUsers) {
		this.freeUsers = freeUsers;
	}
	public int getTotalUsers() {
		return totalUsers;
	}
	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}
	public int getQuoteCount() {
		return quoteCount;
	}
	public void setQuoteCount(int quoteCount) {
		this.quoteCount = quoteCount;
	}
	public int getOrderCount() {
		return orderCount;
	}
	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}
	public int getJobCount() {
		return jobCount;
	}
	public void setJobCount(int jobCount) {
		this.jobCount = jobCount;
	}
	public int getEcomLogins() {
		return ecomLogins;
	}
	public void setEcomLogins(int ecomLogins) {
		this.ecomLogins = ecomLogins;
	}

	public void setSystemInfo(String systemInfo) {
		this.systemInfo = systemInfo;
	}
	public String getSystemInfo() {
		return systemInfo;
	}
	public int getLocalProductCount() {
		return localProductCount;
	}
	public void setLocalProductCount(int localProductCount) {
		this.localProductCount = localProductCount;
	}
	public int getLocalActiveProductCount() {
		return localActiveProductCount;
	}
	public void setLocalActiveProductCount(int localActiveProductCount) {
		this.localActiveProductCount = localActiveProductCount;
	}
	public int getLocalVendorCount() {
		return localVendorCount;
	}
	public void setLocalVendorCount(int localVendorCount) {
		this.localVendorCount = localVendorCount;
	}
	public int getLocalActiveVendorCount() {
		return localActiveVendorCount;
	}
	public void setLocalActiveVendorCount(int localActiveVendorCount) {
		this.localActiveVendorCount = localActiveVendorCount;
	}
	public long getEpdProductCount() {
		return epdProductCount;
	}
	public void setEpdProductCount(long epdProductCount) {
		this.epdProductCount = epdProductCount;
	}
	public long getEpdImageCount() {
		return epdImageCount;
	}
	public void setEpdImageCount(long epdImageCount) {
		this.epdImageCount = epdImageCount;
	}
	public long getEpdMatchCount() {
		return epdMatchCount;
	}
	public void setEpdMatchCount(long epdMatchCount) {
		this.epdMatchCount = epdMatchCount;
	}
	public Date getEpdImportStartdate() {
		return epdImportStartdate;
	}
	public void setEpdImportStartdate(Date epdImportStartdate) {
		this.epdImportStartdate = epdImportStartdate;
	}
	public Date getEpdImportFinishdate() {
		return epdImportFinishdate;
	}
	public void setEpdImportFinishdate(Date epdImportFinishdate) {
		this.epdImportFinishdate = epdImportFinishdate;
	}
	public String getEpdImportStatus() {
		return epdImportStatus;
	}
	public void setEpdImportStatus(String epdImportStatus) {
		this.epdImportStatus = epdImportStatus;
	}
	@Override
	public String toString() {
		return "ResellerUsage [quotingUsers=" + quotingUsers
				+ ", orderingUsers=" + orderingUsers + ", opportunityUsers="
				+ opportunityUsers + ", jobUsers=" + jobUsers + ", freeUsers="
				+ freeUsers + ", totalUsers=" + totalUsers + ", quoteCount="
				+ quoteCount + ", orderCount=" + orderCount + ", jobCount="
				+ jobCount + ", ecomLogins=" + ecomLogins
				+ ", localProductCount=" + localProductCount
				+ ", localActiveProductCount=" + localActiveProductCount
				+ ", localVendorCount=" + localVendorCount
				+ ", localActiveVendorCount=" + localActiveVendorCount
				+ ", epdProductCount=" + epdProductCount + ", epdImageCount="
				+ epdImageCount + ", epdMatchCount=" + epdMatchCount
				+ ", epdImportStartdate=" + epdImportStartdate
				+ ", epdImportFinishdate=" + epdImportFinishdate
				+ ", epdImportStatus=" + epdImportStatus + ", systemInfo="
				+ systemInfo + "]";
	}
}
