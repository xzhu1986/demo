package au.com.isell.rlm.module.schedule.dao.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import au.com.isell.common.filter.FieldMapper;
import au.com.isell.common.filter.FilterItem;
import au.com.isell.common.filter.FilterMaker;
import au.com.isell.common.filter.FilterMaker.TextMatchOption;
import au.com.isell.common.filter.elasticsearch.ESFilterItem;
import au.com.isell.common.filter.elasticsearch.ESFilterMaker;
import au.com.isell.common.index.elasticsearch.IndexHelper;
import au.com.isell.common.index.elasticsearch.QueryParams;
import au.com.isell.remote.common.model.Pair;
import au.com.isell.rlm.common.dao.DAOSupport;
import au.com.isell.rlm.module.schedule.dao.ScheduleDao;
import au.com.isell.rlm.module.schedule.domain.Schedule;
import au.com.isell.rlm.module.schedule.domain.ScheduleHistory;

@Repository
public class AwsScheduleDao extends DAOSupport implements ScheduleDao {
	private IndexHelper indexHelper = IndexHelper.getInstance();
	private static FieldMapper scheduleHistoryMapper = new FieldMapper();
	
	@Override
	public Schedule getSchedule(String scheduleId) {
		Schedule schedule = new Schedule();
		schedule.setScheduleId(UUID.fromString(scheduleId));
		return get(schedule);
	}

	@Override
	public void saveSchedule(Schedule schedule) {
		super.save(schedule);
	}

	@Override
	public Pair<Long, List<ScheduleHistory>> queryScheduleHistories(String scheduleId, Integer pageSize, Integer pageNo) {
		FilterMaker maker = getScheduleHistoryMaker();
		FilterItem item = maker.makeNameFilter("scheduleId", TextMatchOption.Is, scheduleId);
		@SuppressWarnings("unchecked")
		Pair<String, Boolean>[] sorts = new Pair[1];
		sorts[0]= maker.makeSortItem("operationDate", true);
		Pair<Long, List<ScheduleHistory>> r = indexHelper.queryBeans(ScheduleHistory.class, new QueryParams(((ESFilterItem) item).generateQueryBuilder(),sorts));
		if(r!=null && r.getValue()!=null){
			Pair<Long, List<ScheduleHistory>> nr = new Pair<Long, List<ScheduleHistory>>();
			nr.setKey(r.getKey());
			List<ScheduleHistory> nrList = new ArrayList<ScheduleHistory>();
			ScheduleHistory o = null;
			for(ScheduleHistory obj :r.getValue()){
				o = super.get(obj);
				if(o!=null){
					nrList.add(o);
				}
			}
			nr.setValue(nrList);
			return nr;
		}
		return null;
	}

	@Override
	public FilterMaker getScheduleHistoryMaker() {
		ESFilterMaker maker = new ESFilterMaker();
		maker.setType(ScheduleHistory.class);
		maker.setFieldMapper(scheduleHistoryMapper);
	return maker;}

	@Override
	public void saveScheduleHistory(ScheduleHistory scheduleHistory) {
		super.save(scheduleHistory);
		
		FilterMaker maker = getScheduleHistoryMaker();
		FilterItem item = maker.makeNameFilter("scheduleId", TextMatchOption.Is, scheduleHistory.getScheduleId().toString());
		int count = (int)indexHelper.count(ScheduleHistory.class, ((ESFilterItem) item).generateQueryBuilder());
		if(count>20){
			@SuppressWarnings("unchecked")
			Pair<String, Boolean>[] sorts = new Pair[1];
			sorts[0]= maker.makeSortItem("operationDate", true);
			Pair<Long, List<ScheduleHistory>> r = indexHelper.queryBeans(ScheduleHistory.class,new QueryParams(((ESFilterItem) item).generateQueryBuilder(), sorts).withPaging(1, count-20));
			indexHelper.deleteByObj(r.getValue().toArray(new ScheduleHistory[r.getValue().size()]));
		}
	}
}
