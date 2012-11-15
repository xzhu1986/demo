package au.com.isell.rlm.module.invoice.vo;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.rlm.module.invoice.domain.Invoice.InvoiceStatus;

public enum InvoiceSearchFilters4Reseller {
	All("All") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeAllQuery();
		}
	},
	UnPaid("Un-Paid") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeIntRange("status", null, InvoiceStatus.WaitingPayment.ordinal(), false, true);
		}
	}
	;

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private InvoiceSearchFilters4Reseller(String display) {
		this.display = display;
	}

}
