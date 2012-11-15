package au.com.isell.remote.ws.parasearch.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("epdProductGroup")
public class EpdProductGroup implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4831562872122925029L;
	private String tier1;
	private String tier2;//FRIENDLYNAME
	private Integer unspsc;
	
	public Integer getUnspsc() {
		return unspsc;
	}
	public void setUnspsc(Integer unspsc) {
		this.unspsc = unspsc;
	}
	public String getTier1() {
		return tier1;
	}
	public void setTier1(String tier1) {
		this.tier1 = tier1;
	}
	public String getTier2() {
		return tier2;
	}
	public void setTier2(String tier2) {
		this.tier2 = tier2;
	}

}
