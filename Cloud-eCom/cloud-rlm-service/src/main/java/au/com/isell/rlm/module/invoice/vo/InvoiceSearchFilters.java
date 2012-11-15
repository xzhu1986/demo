package au.com.isell.rlm.module.invoice.vo;

import java.util.Date;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.module.invoice.domain.Invoice.InvoiceStatus;

public enum InvoiceSearchFilters {
	FollowupDue("Followup Due") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem filterItem1 = filterMaker.makeIntRange("status", null, InvoiceStatus.PaymentSchedule.ordinal(), true, true);
			FilterItem filterItem2 = filterMaker.makeDateRange("followupDate", null, new Date(), true, true);
			FilterItem filterItem3 = filterMaker.makeNameFilter("balance", TextMatchOption.IsNot, "0");
			return filterMaker.linkWithAnd(filterItem1, filterItem2,filterItem3);
		}
	},
	WaitingPayment("Waiting Payment") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem filterItem1= filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.WaitingPayment.ordinal()));
			FilterItem filterItem3 = filterMaker.makeNameFilter("balance", TextMatchOption.IsNot, "0");
			return filterMaker.linkWithAnd(filterItem1, filterItem3);
		}
	},
	WaitingOnHold("Waiting/On Hold") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem filterItem1 = filterMaker
					.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.WaitingApproval.ordinal()));
			FilterItem filterItem2 = filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.OnHold.ordinal()));
			return filterMaker.linkWithOr(filterItem1, filterItem2);
		}
	},
	OnPaymentSchedule("On Payment Schedule") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem filterItem1=  filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.PaymentSchedule.ordinal()));
			FilterItem filterItem3 = filterMaker.makeNameFilter("balance", TextMatchOption.IsNot, "0");
			return filterMaker.linkWithAnd(filterItem1, filterItem3);
		}
	},
	FullyPaidLt30Days("Fully Paid&lt;30 days") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem filterItem1 = filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.FullPaid.ordinal()));
			FilterItem filterItem2 = filterMaker.makeDateRange("fullPaidDate", DatePicker.pickDay(new Date(), -30), null, true, true);
			return filterMaker.linkWithAnd(filterItem1, filterItem2);
		}
	},
	FullyPaid("Fully Paid") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.FullPaid.ordinal()));
		}
	},
	Cancelled("Cancelled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(InvoiceStatus.Cancelled.ordinal()));
		}
	},
	CreatedToday("Created Today") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeDateRange("createdDate", DatePicker.pickDay(new Date(), 0), null, true, false);
		}
	},
	CreatedThisMonth("Created this Month") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeDateRange("createdDate", DatePicker.pickFirstDayOfMonth(new Date(), 0), null, true, false);
		}
	},
	CreatedLastMonth("Created last Month") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeDateRange("createdDate", DatePicker.pickFirstDayOfMonth(new Date(), -1),
					DatePicker.pickFirstDayOfMonth(new Date(), 0), true, false);
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

	private InvoiceSearchFilters(String display) {
		this.display = display;
	}

}
