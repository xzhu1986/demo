package au.com.isell.remote.ws.parasearch.model;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("epdProductGroupTier1")
public class EpdProductGroupTier1 implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6139491699452928433L;
	private String tier1;
	public String getTier1() {
		return tier1;
	}
	public void setTier1(String tier1) {
		this.tier1 = tier1;
	}
}
