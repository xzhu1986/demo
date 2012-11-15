package au.com.isell.common.filter;

public class OrderItem {

	private String name;
	private boolean asc;
	
	public OrderItem() {
		
	}

	public OrderItem(String name, boolean asc) {
		setName(name);
		setAsc(asc);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isAsc() {
		return asc;
	}
	public void setAsc(boolean asc) {
		this.asc = asc;
	}
}
