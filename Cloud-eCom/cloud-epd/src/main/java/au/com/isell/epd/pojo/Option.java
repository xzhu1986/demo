package au.com.isell.epd.pojo;

public class Option {
	private String manufacturer;
	private String partNo;
	private String uuid;

	private String o_manufacturer;
	private String o_partNo;
	private String o_uuid;

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getPartNo() {
		return partNo;
	}

	public void setPartNo(String partNo) {
		this.partNo = partNo;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getO_manufacturer() {
		return o_manufacturer;
	}

	public void setO_manufacturer(String o_manufacturer) {
		this.o_manufacturer = o_manufacturer;
	}

	public String getO_partNo() {
		return o_partNo;
	}

	public void setO_partNo(String o_partNo) {
		this.o_partNo = o_partNo;
	}

	public String getO_uuid() {
		return o_uuid;
	}

	public void setO_uuid(String o_uuid) {
		this.o_uuid = o_uuid;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Option))
			return false;
		Option option = (Option) obj;
		boolean isSameManufacturer = false;
		boolean isSamePartNo = false;
		if(this.o_manufacturer!=null){
			isSameManufacturer = this.o_manufacturer.equals(option.getO_manufacturer());
		}else if(this.o_manufacturer ==null && option.getO_manufacturer()==null){
			isSameManufacturer = true;
		}
		if(this.o_partNo!=null){
			isSamePartNo = this.o_partNo.equals(option.getO_partNo());
		}else if(this.o_partNo ==null && option.getO_partNo()==null){
			isSamePartNo = true;
		}
		if(isSameManufacturer && isSamePartNo)
			return true;
		else
			return false;
		 
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + this.o_manufacturer.hashCode();
		result = 37 * result + this.o_partNo.hashCode();
		return result;
	}
}