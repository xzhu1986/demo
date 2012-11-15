package au.com.isell.rlm.module.schedule.dao;

import java.util.List;

import au.com.isell.common.filter.FilterMaker;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;

public interface ScheduleDao {
	Schedule getSchedule(String scheduleId);
	void saveSchedule(Schedule schedule);
	Pair<Long, List<ScheduleHistory>> queryScheduleHistories(String scheduleId,Integer pageSize, Integer pageNo);
	
	FilterMaker getScheduleHistoryMaker();
	
	void saveScheduleHistory(ScheduleHistory scheduleHistory);
}
