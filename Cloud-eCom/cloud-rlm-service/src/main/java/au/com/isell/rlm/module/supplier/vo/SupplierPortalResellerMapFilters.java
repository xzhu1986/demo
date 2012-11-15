package au.com.isell.rlm.module.supplier.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.reseller.domain.ResellerSupplierMap.ApprovalStatus;

public enum SupplierPortalResellerMapFilters {
	WaitingApproval("Waiting Approval") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ApprovalStatus.Pending.ordinal()));
		}
	},
	All("All") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeAllQuery();
		}
	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private SupplierPortalResellerMapFilters(String display) {
		this.display = display;
	}

}
