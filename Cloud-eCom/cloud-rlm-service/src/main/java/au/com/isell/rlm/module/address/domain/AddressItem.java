package au.com.isell.rlm.module.address.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.bean.AfterSaving;
import au.com.isell.common.index.annotation.ISellIndex;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexRouting;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.module.user.service.PermissionPreloadingService;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@IsellPath("data/location/country/${s3code}/basic.info.json")
@XStreamAlias("address-def")
@ISellIndex(name = "location", type = "area")
public class AddressItem extends AbstractModel implements AfterSaving {
	public static final String TYPE_COUNTRY = "Country";
	public static final String DEFAULT_PARENT_CODE = "_";

	private String code;
	private String type;
	private String name;
	private String shortName;
	private String parent = DEFAULT_PARENT_CODE;
	private boolean hasPostcode;
	private String postcodeName;
	private String[] postcodeBind;
	private int phoneAreaCodeBind;
	private String currency;
	private UUID defaultAgency;
	private String regionName;
	private BigDecimal taxRate;
	private String taxName;

	public AddressItem() {
		parent = DEFAULT_PARENT_CODE;
	}

	public AddressItem(String code, String type, String name, String shortName, String parent) {
		setCode(code);
		setType(type);
		setName(name);
		setShortName(shortName);
		if (parent == null)
			parent = DEFAULT_PARENT_CODE;
		setParent(parent);
	}

	@Override
	public void operate(ApplicationContext context) {
		if (parent == DEFAULT_PARENT_CODE) {// country
			context.getBean(PermissionPreloadingService.class).updatePermissionCache(
					new ClassPathResource("permissions/agent/show-supplier-by-country.xml"));
		}
	}

	@IsellPathField
	public String getS3code() {
		return code != null ? code.replace('_', '/') : null;
	}

	public void setType(String type) {
		this.type = type;
	}

	@ISellIndexValue
	public String getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ISellIndexValue(wildcard = true)
	public String getName() {
		return name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@ISellIndexKey
	public String getCode() {
		return code;
	}

	@ISellIndexValue
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	@ISellIndexValue
	public String[] getPostcodeBind() {
		return postcodeBind;
	}

	public void setPostcodeBind(String[] postcodeBind) {
		this.postcodeBind = postcodeBind;
	}

	@ISellIndexValue
	public int getPhoneAreaCodeBind() {
		return phoneAreaCodeBind;
	}

	public void setPhoneAreaCodeBind(int phoneAreaCodeBind) {
		this.phoneAreaCodeBind = phoneAreaCodeBind;
	}

	@ISellIndexRouting
	public String getRouting() {
		return "isell";
	}

	@Override
	public String toString() {
		return "AddressItem [code=" + code + ", type=" + type + ", name=" + name + ", shortName=" + shortName + ", parent=" + parent
				+ ", hasPostcode=" + hasPostcode + ", postcodeName=" + postcodeName + ", postcodeBind=" + Arrays.toString(postcodeBind)
				+ ", phoneAreaCodeBind=" + phoneAreaCodeBind + ", currency=" + currency + ", defaultAgency=" + defaultAgency + "]";
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	@ISellIndexValue
	public String getCurrency() {
		return currency;
	}

	public boolean isHasPostcode() {
		return hasPostcode;
	}

	public void setHasPostcode(boolean hasPostcode) {
		this.hasPostcode = hasPostcode;
	}

	public String getPostcodeName() {
		return postcodeName;
	}

	public void setPostcodeName(String postcodeName) {
		this.postcodeName = postcodeName;
	}

	public UUID getDefaultAgency() {
		return defaultAgency;
	}

	public void setDefaultAgency(UUID defaultAgency) {
		this.defaultAgency = defaultAgency;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	@ISellIndexValue
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public BigDecimal getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(BigDecimal taxRate) {
		this.taxRate = taxRate;
	}

	public String getTaxName() {
		return taxName;
	}

	public void setTaxName(String taxName) {
		this.taxName = taxName;
	}

}