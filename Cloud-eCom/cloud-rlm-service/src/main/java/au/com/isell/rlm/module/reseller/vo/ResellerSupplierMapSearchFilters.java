package au.com.isell.rlm.module.reseller.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap;

public enum ResellerSupplierMapSearchFilters {
	All("All"){
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeAllQuery();
		}
	},  
	Pending("Pending") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerSupplierMap.ApprovalStatus.Pending.ordinal()));
		}
	},
	Approval("Approved") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerSupplierMap.ApprovalStatus.Approved.ordinal()));
		}
	},
	OnHold("OnHold") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerSupplierMap.ApprovalStatus.OnHold.ordinal()));
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

	private ResellerSupplierMapSearchFilters(String display) {
		this.display = display;
	}

}
