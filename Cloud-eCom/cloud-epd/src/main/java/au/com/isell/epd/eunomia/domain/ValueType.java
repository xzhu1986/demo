package au.com.isell.epd.eunomia.domain;

public enum ValueType {
	Text, Number, Range, Date;
	
	public static ValueType getValueTypeByName(String name){
		for(ValueType temp :ValueType.values()){
			if(temp.name().equals(name)){
				return temp;
			}
		}
		return null;
	}
}
