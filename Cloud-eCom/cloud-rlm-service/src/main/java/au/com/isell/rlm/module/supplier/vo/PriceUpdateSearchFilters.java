package au.com.isell.rlm.module.supplier.vo;

import java.util.Date;

import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.rlm.common.utils.DatePicker;
import au.com.isell.rlm.module.schedule.domain.Status;

public enum PriceUpdateSearchFilters {
	DueNow("Due Now") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.linkWithAnd(filterMaker.makeDateRange("nextExecuteDate", DatePicker.pickDay(new Date(), 0), DatePicker.pickDay(new Date(), 1), true, false),
					filterMaker.makeNameFilter("enable", TextMatchOption.Is, "true"));
		}
	},
	CompletedToday("Completed Today") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.linkWithAnd(filterMaker.makeDateRange("recentExecuteDate", DatePicker.pickDay(new Date(), 0), DatePicker.pickDay(new Date(), 1), true, false),
					filterMaker.linkWithOr(filterMaker.makeNameFilter("status", TextMatchOption.Is,String.valueOf(Status.Completed.ordinal())),filterMaker.makeNameFilter("status", TextMatchOption.Is, String.valueOf(Status.Failed.ordinal()))),
					filterMaker.makeNameFilter("enable", TextMatchOption.Is, "true"));
		}
	},
	DueTomorrow("Due Tomorrow") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.linkWithAnd(filterMaker.makeDateRange("nextExecuteDate", DatePicker.pickDay(new Date(), 1), DatePicker.pickDay(new Date(), 2), true, false),
					filterMaker.makeNameFilter("enable", TextMatchOption.Is, "true"));
		}
	},
	Active("Active") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("enable", TextMatchOption.Is, "true");
		}
	},
	Disabled("Disabled") {
		@Override
		public FilterItem getFilterItem(FilterMaker filterMaker) {
			return filterMaker.makeNameFilter("enable", TextMatchOption.Is, "false");
		}

	};

	public abstract FilterItem getFilterItem(FilterMaker filterMaker);

	private String display;

	@Override
	public String toString() {// this will also affect Enum.valueOf(...)
		return display;
	}

	private PriceUpdateSearchFilters(String display) {
		this.display = display;
	}

}
