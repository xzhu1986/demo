package au.com.isell.remote.ws.parasearch.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Frank Wu 29/09/2011
 */
@XStreamAlias("epdProduct")
public class EpdVendorProduct implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1939606536963190663L;
	private Integer supplierProductId;
	private Integer vendorId;
	private String vendorName;
	private String vendorCode;
	private String name;
	private String supplier;
	private Integer supplierId;
	private String partNumber;
	private Integer stock;
	private Double buy;
	private Double rrp;
	private Double sell;
	private Integer imageFlag;

	public Integer getVendorId() {
		return vendorId;
	}

	public void setVendorId(Integer vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorCode() {
		return vendorCode;
	}

	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Double getBuy() {
		return buy;
	}

	public void setBuy(Double buy) {
		this.buy = buy;
	}

	public Double getRrp() {
		return rrp;
	}

	public void setRrp(Double rrp) {
		this.rrp = rrp;
	}

	public Double getSell() {
		return sell;
	}

	public void setSell(Double sell) {
		this.sell = sell;
	}

	public Integer getImageFlag() {
		return imageFlag;
	}

	public void setImageFlag(Integer imageFlag) {
		this.imageFlag = imageFlag;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public Integer getSupplierId() {
		return supplierId;
	}

	public void setSupplierProductId(Integer supplierProductId) {
		this.supplierProductId = supplierProductId;
	}

	public Integer getSupplierProductId() {
		return supplierProductId;
	}

}
