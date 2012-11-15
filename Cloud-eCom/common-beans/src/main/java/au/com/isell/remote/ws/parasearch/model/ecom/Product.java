package au.com.isell.remote.ws.parasearch.model.ecom;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author frankw 16/12/2011
 */
@XStreamAlias("product")
public class Product implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5387364533891997636L;
	private	Integer id;
	private	Integer supplierID;
	private	String productCode;
	private	String productName;
	private	String description;
	private	String supplier;
	private	String vendorPart;
	private	Integer vendorID;
	private	String vendor;
	private	Integer accountID;
	private	Double weight;
	private	Integer supplierProductID;
	private	String productID;
	private	Integer imageFlag;
	
	private Integer imageCount;
	private List<String> imageNames;
		
	private	Double priceInc;
	private	Double priceEx;
	private	Double rrpInc;
	private	Double rrpEx;
	private	Double buyInc;
	private	Double buyEx;
		
	private	Double weeklyEx;
	private	Double weeklyInc;
	private	Double monthlyEx;
	private	Double monthlyInc;
	
	private	String stock;
	private	Integer stockQty;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getSupplierID() {
		return supplierID;
	}
	public void setSupplierID(Integer supplierID) {
		this.supplierID = supplierID;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getSupplier() {
		return supplier;
	}
	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}
	public String getVendorPart() {
		return vendorPart;
	}
	public void setVendorPart(String vendorPart) {
		this.vendorPart = vendorPart;
	}
	public Integer getVendorID() {
		return vendorID;
	}
	public void setVendorID(Integer vendorID) {
		this.vendorID = vendorID;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public Integer getAccountID() {
		return accountID;
	}
	public void setAccountID(Integer accountID) {
		this.accountID = accountID;
	}
	public Double getWeight() {
		return weight;
	}
	public void setWeight(Double weight) {
		this.weight = weight;
	}
	public Integer getSupplierProductID() {
		return supplierProductID;
	}
	public void setSupplierProductID(Integer supplierProductID) {
		this.supplierProductID = supplierProductID;
	}
	public String getProductID() {
		return productID;
	}
	public void setProductID(String productID) {
		this.productID = productID;
	}
	public Integer getImageFlag() {
		return imageFlag;
	}
	public void setImageFlag(Integer imageFlag) {
		this.imageFlag = imageFlag;
	}
	public Double getPriceInc() {
		return priceInc;
	}
	public void setPriceInc(Double priceInc) {
		this.priceInc = priceInc;
	}
	public Double getPriceEx() {
		return priceEx;
	}
	public void setPriceEx(Double priceEx) {
		this.priceEx = priceEx;
	}
	public Double getRrpInc() {
		return rrpInc;
	}
	public void setRrpInc(Double rrpInc) {
		this.rrpInc = rrpInc;
	}
	public Double getRrpEx() {
		return rrpEx;
	}
	public void setRrpEx(Double rrpEx) {
		this.rrpEx = rrpEx;
	}
	public Double getBuyInc() {
		return buyInc;
	}
	public void setBuyInc(Double buyInc) {
		this.buyInc = buyInc;
	}
	public Double getBuyEx() {
		return buyEx;
	}
	public void setBuyEx(Double buyEx) {
		this.buyEx = buyEx;
	}
	public Double getWeeklyEx() {
		return weeklyEx;
	}
	public void setWeeklyEx(Double weeklyEx) {
		this.weeklyEx = weeklyEx;
	}
	public Double getWeeklyInc() {
		return weeklyInc;
	}
	public void setWeeklyInc(Double weeklyInc) {
		this.weeklyInc = weeklyInc;
	}
	public Double getMonthlyEx() {
		return monthlyEx;
	}
	public void setMonthlyEx(Double monthlyEx) {
		this.monthlyEx = monthlyEx;
	}
	public Double getMonthlyInc() {
		return monthlyInc;
	}
	public void setMonthlyInc(Double monthlyInc) {
		this.monthlyInc = monthlyInc;
	}
	
	public String getStock() {
		return stock;
	}
	public void setStock(String stock) {
		this.stock = stock;
	}
	public Integer getStockQty() {
		return stockQty;
	}
	public void setStockQty(Integer stockQty) {
		this.stockQty = stockQty;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getImageCount() {
		return imageCount;
	}
	public void setImageCount(Integer imageCount) {
		this.imageCount = imageCount;
	}
	public List<String> getImageNames() {
		return imageNames;
	}
	public void setImageNames(List<String> imageNames) {
		this.imageNames = imageNames;
	}
	
	
}
