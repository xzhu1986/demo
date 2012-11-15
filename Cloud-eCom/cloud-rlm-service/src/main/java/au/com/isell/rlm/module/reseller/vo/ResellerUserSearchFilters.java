package au.com.isell.rlm.module.reseller.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.user.constant.UserStatus;

public enum ResellerUserSearchFilters {
	All("All"){
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeAllQuery();
		}
	},  
	Active("Active") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(UserStatus.ACTIVE.ordinal()));
		}
	},
	Disabled("Disabled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(UserStatus.DISABLED.ordinal()));
		}
	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private ResellerUserSearchFilters(String display) {
		this.display = display;
	}


}
