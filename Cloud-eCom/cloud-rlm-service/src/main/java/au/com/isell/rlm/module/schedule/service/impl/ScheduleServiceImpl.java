package au.com.isell.rlm.module.schedule.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.module.schedule.dao.ScheduleDao;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;
import au.com.isell.rlm.module.schedule.service.ScheduleService;
import au.com.isell.rlm.module.supplier.service.impl.SupplierServiceImpl;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	private static Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

	@Autowired
	private ScheduleDao scheduleDao;

	@Override
	public Schedule getSchedule(String scheduleId) {
		return scheduleDao.getSchedule(scheduleId);
	}

	@Override
	public void saveSchedule(Schedule schedule) {
		scheduleDao.saveSchedule(schedule);
	}
	@Override
	public boolean checkeScheduleTime(String time,String val){
		if(!StringUtils.isNumeric(time) || !StringUtils.isNumeric(val)) return false;
		if((Long.parseLong(time) & Long.parseLong(val))!=0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public Pair<Long, List<ScheduleHistory>> queryScheduleHistories(String scheduleId, Integer pageSize, Integer pageNo) {
		if(StringUtils.isBlank(scheduleId)){
			return new Pair<Long, List<ScheduleHistory>>(0l,new ArrayList<ScheduleHistory>());
		}
		return scheduleDao.queryScheduleHistories(scheduleId, pageSize, pageNo);
	}
}
