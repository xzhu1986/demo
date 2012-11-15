package au.com.isell.common.filter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.isell.common.filter.FilterItem.Type;
import au.com.isell.remote.common.model.Pair;

public interface FilterMaker {
	
	public static enum TextMatchOption {
		Contains(1), NotContains(2), Is(3), IsNot(4), StartWith(5), EndWith(6);
		private  TextMatchOption(int code) {
			this.code = code;
		}
		private int code;
		
		public int getCode() {
			return code;
		}
		
		public static TextMatchOption getTMOpionByCode(int code){
			for(TextMatchOption option2 : TextMatchOption.values()){
				if(code==option2.getCode()){
					return option2;
				}
			}
			return Contains;
		}
	}
	
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public <T> void setType(Class<T> type);
	
	void setFieldMapper(FieldMapper mapper);
	
	/**
	 * Make a filter for text match conditions such as name Contains "abc". 
	 * You can choose different match options defined in TextMatchOption enum such as Contains, StartWith, Is etc.
	 * @param fieldName the field to be filtered
	 * @param matchOption
	 * @param condition
	 * @return
	 */
	FilterItem makeNameFilter(String fieldName, TextMatchOption matchOption, String condition);
	
	/**
	 * Make a pick filters. on the screen it looks like a field with a set of tick boxes 
	 * which you can pick couple of possible values to search
	 * @param fieldName the field to be filtered
	 * @param pickedItems
	 * @param type
	 * @return
	 */
	FilterItem makePickFilter(String fieldName, String[] pickedItems, Type type);
	
	/**
	 * Make a date range filter. Start and End at least have one value.
	 * If start is null then equals date less than and vice versa.
	 * @param fieldName the field to be filtered
	 * @param start
	 * @param end
	 * @return
	 */
	FilterItem makeDateRange(String fieldName, Date start, Date end, boolean includeLower, boolean includeUpper);
	
	/**
	 * Make a number range filter. bottom and top at least have one value.
	 * If start is null then equals date less than and vice versa.
	 * @param fieldName the field to be filtered
	 * @param start
	 * @param end
	 * @return
	 */
	FilterItem makeIntRange(String fieldName, Integer bottom, Integer top, boolean includeLower, boolean includeUpper);
	
	/**
	 * Make a number range filter. bottom and top at least have one value.
	 * If start is null then equals date less than and vice versa.
	 * @param fieldName the field to be filtered
	 * @param start
	 * @param end
	 * @return
	 */
	FilterItem makeDecimalRange(String fieldName, BigDecimal bottom, BigDecimal top, boolean includeLower, boolean includeUpper);
	
	/**
	 * Link multiple filters by AND
	 * @param makers
	 * @return
	 */
	FilterItem linkWithAnd(FilterItem... makers);
	
	/**
	 * Link multiple filters by OR
	 * @param makers
	 * @return
	 */
	FilterItem linkWithOr(FilterItem... makers);
	
	FilterItem makeAllQuery();
	
	FilterItem makeNullQuery(String queryStr);
	Pair<String, Boolean> makeSortItem(String sortField, boolean asc);
	
	String getPhysicalField(String fieldName);
	String getPhysicalWildcardField(String fieldName);
}
