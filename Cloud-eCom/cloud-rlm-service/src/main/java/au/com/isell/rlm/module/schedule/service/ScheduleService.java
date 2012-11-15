package au.com.isell.rlm.module.schedule.service;

import java.util.List;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;

public interface ScheduleService {
	Schedule getSchedule(String scheduleId);
	
	void saveSchedule(Schedule schedule);
	
	boolean checkeScheduleTime(String time,String val);
	
	Pair<Long, List<ScheduleHistory>> queryScheduleHistories(String scheduleId,Integer pageSize, Integer pageNo);
	
}