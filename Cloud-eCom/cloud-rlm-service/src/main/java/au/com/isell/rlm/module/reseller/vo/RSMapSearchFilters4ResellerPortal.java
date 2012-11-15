package au.com.isell.rlm.module.reseller.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;

public enum RSMapSearchFilters4ResellerPortal {
	MySuppliers("My Suppliers"){
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerSupplierMap.ApprovalStatus.Disabled.ordinal()));
		}
	},  
	Active("Active") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerSupplierMap.ApprovalStatus.Approved.ordinal()));
		}
	},
	Pending("Pending") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerSupplierMap.ApprovalStatus.Pending.ordinal()));
		}
	},
	Disabled("Disabled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerSupplierMap.ApprovalStatus.Disabled.ordinal()));
		}
	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private RSMapSearchFilters4ResellerPortal(String display) {
		this.display = display;
	}

}
