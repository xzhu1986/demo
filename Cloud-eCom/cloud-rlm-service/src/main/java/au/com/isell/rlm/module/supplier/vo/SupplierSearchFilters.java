package au.com.isell.rlm.module.supplier.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.supplier.domain.Supplier.Status;

public enum SupplierSearchFilters {
	Active("Active") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(Status.Active.ordinal()));
		}
	},
	Setup("Setup") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(Status.Setup.ordinal()));
		}
	},	
	Disabled("Disabled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(Status.Disabled.ordinal()));
		}
	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private SupplierSearchFilters(String display) {
		this.display = display;
	}

}
