package au.com.isell.rlm.module.supplier.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.supplier.domain.PriceBreak;

public enum PriceBreakSearchFilters {
	All("All"){
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeAllQuery();
		}
	},  
	Active("Active") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(PriceBreak.Status.Active.ordinal()));
		}
	},
	Setup("Setup") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(PriceBreak.Status.Setup.ordinal()));
		}
	},
	Disabled("Disabled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(PriceBreak.Status.Disabled.ordinal()));
		}
	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private PriceBreakSearchFilters(String display) {
		this.display = display;
	}

}
