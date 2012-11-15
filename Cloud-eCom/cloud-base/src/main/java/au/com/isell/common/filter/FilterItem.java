package au.com.isell.common.filter;


public interface FilterItem {

	public static enum Type {
		FieldName(1), Text(2), Int(3), Decimal(4), Date(5), Function(6);
		private  Type(int code) {
			this.code = code;
		}
		private int code;
		
		public int getCode() {
			return code;
		}
		
		public static Type getTypeByCode(int code){
			for(Type option2 : Type.values()){
				if(code==option2.getCode()){
					return option2;
				}
			}
			return null;
		}
	};

}
