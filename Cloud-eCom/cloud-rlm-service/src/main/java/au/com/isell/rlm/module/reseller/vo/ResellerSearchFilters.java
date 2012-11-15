package au.com.isell.rlm.module.reseller.vo;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerStatus;
import au.com.isell.rlm.module.reseller.domain.Reseller.ResellerType;

public enum ResellerSearchFilters {
	Active("Active") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.Active.ordinal()));
			return linkExludeFilter(filterMaker, item1);
		}
	},
	AccountReviewDue("Account Review Due") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			Date todayBegin = DateUtils.addDays(new Date(), 0);
			Date todayEnd = DateUtils.addDays(new Date(), 1);
			FilterItem item1 = filterMaker.makeDateRange("nextReviewDate", todayBegin, todayEnd, true, false);
			return item1;
		}
	},
	Renewing60Days("Renewing 60 Days") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			Date _60DaysLater = DateUtils.addDays(new Date(), 60);
			FilterItem item1 = filterMaker.makeDateRange("renewDate", null, _60DaysLater, false, false);
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item1, item2);
		}
	},
	Created30Days("Created 30 Days") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeDateRange("createdDatetime", DateUtils.addDays(new Date(), -30), null, false, false);
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item1, item2);
		}
	},
	StepState("Setup Stage") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.Setup.ordinal()));
			return linkExludeFilter(filterMaker, item1);
		}
	},
	WaitingSuppliers("Waiting Suppliers") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeIntRange("supplierApprovalRemain", 0, null, false, false);
			FilterItem item3 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item1, item3);
		}
	},
	LastSyncGt1Day("Last Sync > 1 day") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeDateRange("lastSyncDate", null, DateUtils.addDays(new Date(), -1), false, false);
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item1, item2);
		}
	},
	IsellInternal("iSell Internal") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1= filterMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.Internal.ordinal()));
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return filterMaker.linkWithAnd(item1,item2);
		}
	},
	TestLicense("Test License") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1= filterMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.TestLicense.ordinal()));
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return filterMaker.linkWithAnd(item1,item2);
		}
	},
	DataOnly("Data Only Customers") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.DataOnly.ordinal()));
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item1, item2);
		}
	},
	NoManagedData("No Data Customers") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.NoData.ordinal()));
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item1, item2);
		}
	},
	Suppliers("Supplier") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item1 = filterMaker.makeNameFilter("type", TextMatchOption.Is, String.valueOf(ResellerType.Supplier.ordinal()));
			FilterItem item2 = filterMaker.makeNameFilter("status", TextMatchOption.IsNot, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return filterMaker.linkWithAnd(item1, item2);
		}
	},
	OnHold("On Hold") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item = filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.OnHold.ordinal()));
			return linkExludeFilter(filterMaker, item);
		}
	},
	Disabled("Disabled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			FilterItem item = filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(ResellerStatus.Disabled.ordinal()));
			return linkExludeFilter(filterMaker, item);
		}

	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private ResellerSearchFilters(String display) {
		this.display = display;
	}

	private static FilterItem linkExludeFilter(FilterMaker filterMaker, FilterItem... original) {
		FilterItem exlude1 = filterMaker.makeNameFilter("type", TextMatchOption.IsNot, String.valueOf(ResellerType.TestLicense.ordinal()));
		FilterItem exlude2 = filterMaker.makeNameFilter("type", TextMatchOption.IsNot, String.valueOf(ResellerType.Supplier.ordinal()));
		FilterItem exlude3 = filterMaker.makeNameFilter("type", TextMatchOption.IsNot, String.valueOf(ResellerType.Internal.ordinal()));
		FilterItem[] arr = new FilterItem[original.length + 3];
		arr[0] = exlude1;
		arr[1] = exlude2;
		arr[2] = exlude3;
		for (int i = 0; i < original.length; i++) {
			arr[i + 3] = original[i];
		}
		return filterMaker.linkWithAnd(arr);
	}

}
