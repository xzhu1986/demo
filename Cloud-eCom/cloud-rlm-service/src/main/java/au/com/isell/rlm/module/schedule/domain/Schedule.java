package au.com.isell.rlm.module.schedule.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import au.com.isell.common.aws.annotation.s3.IsellPath;
import au.com.isell.common.aws.annotation.s3.IsellPathField;
import au.com.isell.common.index.annotation.ISellIndexKey;
import au.com.isell.common.index.annotation.ISellIndexValue;
import au.com.isell.common.model.AbstractModel;
import au.com.isell.rlm.common.freemarker.EnumMsgCode;

@IsellPath("data/schedules/${scheduleId}/basic.info.json")
@XStreamAlias("schedule")
public class Schedule extends AbstractModel {
	public static enum Frequency {
		@EnumMsgCode("schedule.weekdays")
		WeekDays, 
		@EnumMsgCode("schedule.weeksofmonth")
		WeeksOfMonth,
		@EnumMsgCode("schedule.dayofmonth")
		DayOfMonth
	}
	public static enum DayOfWeeks {
		Mon(MONDAY),Tue(TUESDAY),Wed(WEDNESDAY),Thu(THURSDAY),Fri(FRIDAY),Sat(SATURDAY),Sun(SUNDAY);
		private int display;
    	@Override
    	public String toString() {
    		return String.valueOf(display);
    	}
        DayOfWeeks(int display) {
            this.display = display;
        }
	}
	
	public static int MONDAY=(int)Math.pow(2, 0);
	public static int TUESDAY=(int)Math.pow(2, 1);
	public static int WEDNESDAY=(int)Math.pow(2, 2);
	public static int THURSDAY=(int)Math.pow(2, 3);
	public static int FRIDAY=(int)Math.pow(2, 4);
	public static int SATURDAY=(int)Math.pow(2, 5);
	public static int SUNDAY=(int)Math.pow(2, 6);

	public static enum WeeksOfMonths {
		Week1(FIRST_WEEK),Week2(SECOND_WEEK),Week3(THRID_WEEK),Week4(FORTH_WEEK);
		private int display;
    	@Override
    	public String toString() {
    		return String.valueOf(display);
    	}
    	WeeksOfMonths(int display) {
            this.display = display;
        }
	}
	
	public static int FIRST_WEEK=(int)Math.pow(2, 0);
	public static int SECOND_WEEK=(int)Math.pow(2, 1);
	public static int THRID_WEEK=(int)Math.pow(2, 2);
	public static int FORTH_WEEK=(int)Math.pow(2, 3);

	public static long LAST_DAY_OF_MONTH = (long)Math.pow(2, 31);

	private UUID scheduleId;
	private String type;
	private Frequency frequency;
	/**
	 * seconds of a day from 0 to 60*60*24
	 */
	private int time;
	private int weekDays;
	private int weeksOfMonth;
	/**
	 * From 2^0 - 2^30, 2^31 means last day of month
	 */
	private long daysOfMonth;
	private long fixedSeconde;
	private boolean enabled = true;

	@IsellPathField
	@ISellIndexKey
	public UUID getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(UUID scheduleId) {
		this.scheduleId = scheduleId;
	}
	public Frequency getFrequency() {
		return frequency;
	}
	public void setFrequency(Frequency updateFrequency) {
		this.frequency = updateFrequency;
	}
	public int getWeekDays() {
		return weekDays;
	}
	public void setWeekDays(int weekDays) {
		this.weekDays = weekDays;
	}
	public int getWeeksOfMonth() {
		return weeksOfMonth;
	}
	public void setWeeksOfMonth(int weeksOfMonth) {
		this.weeksOfMonth = weeksOfMonth;
	}
	public long getDaysOfMonth() {
		return daysOfMonth;
	}
	public void setDaysOfMonth(long daysOfMonth) {
		this.daysOfMonth = daysOfMonth;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public int getTime() {
		return time;
	}

	public Date calcNextExecuteDate(Date recentExecuteDate) {
		// calculate nextExecuteDate based on recentExecuteDate 
		if(recentExecuteDate==null) return null;
		Date nextExecuteDate = null;
		List<Date> allScheduleExecuteDates = null;
		if(this.frequency==Frequency.WeekDays){
			allScheduleExecuteDates = getAllScheduleExecuteDates(this.frequency,this.weekDays,FIRST_WEEK+SECOND_WEEK+THRID_WEEK+FORTH_WEEK,recentExecuteDate);
		}else if(this.frequency==Frequency.WeeksOfMonth){
			allScheduleExecuteDates = getAllScheduleExecuteDates(this.frequency,this.weekDays,this.weeksOfMonth,recentExecuteDate);
		}else if(this.frequency==Frequency.DayOfMonth){
			allScheduleExecuteDates = getAllScheduleExecuteDates(this.frequency,this.weekDays,this.weeksOfMonth,recentExecuteDate);
		}
		Collections.sort(allScheduleExecuteDates);
		for(Date executeDate: allScheduleExecuteDates){
			if(executeDate.after(recentExecuteDate)){
				nextExecuteDate = executeDate;
				break;
			}
		}
		return nextExecuteDate;
	}
	public void setType(String type) {
		this.type = type;
	}
	@ISellIndexValue(wildcard=true)
	public String getType() {
		return type;
	}
	public void setFixedSeconde(long fixedSeconde) {
		this.fixedSeconde = fixedSeconde;
	}
	public long getFixedSeconde() {
		return fixedSeconde;
	}
	 /**
     * 将 long 类型数据转成二进制的字符串，不足位数时在前面添“0”以凑足位数
     * @param num,len
     * @return
     */
    public static String toFullBinaryString(long num, int len) {
        char[] chs = new char[len];
        for(int i = 0; i < len; i++) {
            chs[len - 1 - i] = (char)(((num >> i) & 1) + '0');
        }
        return new String(chs);        
    }

	private List<Date> getAllScheduleExecuteDates(Frequency frequency,int weekDays,int weeksOfMonth,Date recentExecuteDate) {
		List<Date> dates = new ArrayList<Date>();
		if(frequency==Frequency.WeekDays || frequency==Frequency.WeeksOfMonth){
			char[] weekChars = new StringBuffer(toFullBinaryString(weeksOfMonth,4 )).reverse().toString().toCharArray();
			char[] weekdayChars = new StringBuffer(toFullBinaryString(weekDays,7)).reverse().toString().toCharArray();
			Calendar calendar1 = null,calendar2 = null;
			for(int i=1;i<=weekChars.length;i++){
				for(int j=1;j<=weekdayChars.length;j++){
					if(weekChars[i-1] == '1' && weekdayChars[j-1]=='1'){
						calendar1 = Calendar.getInstance();
						calendar1.setTime(recentExecuteDate);
						calendar1.set(Calendar.WEEK_OF_MONTH, i);
						calendar1.set(Calendar.DAY_OF_WEEK, j+1);
						dates.add(calendar1.getTime());
						
						calendar2 = Calendar.getInstance();
						calendar2.setTime(recentExecuteDate);
						int m = calendar2.get(Calendar.MONTH);
						calendar2.add(Calendar.MONTH, 1);
						calendar2.set(Calendar.WEEK_OF_MONTH, i);
						calendar2.set(Calendar.DAY_OF_WEEK, j+1);
						if(m<calendar2.get(Calendar.MONTH)){
							dates.add(calendar2.getTime());
						}
					}
				}
			}
		}else if(frequency==Frequency.DayOfMonth ){
			char[] dayChars =  new StringBuffer(toFullBinaryString(daysOfMonth,32)).reverse().toString().toCharArray();
			Calendar calendar = null;
			for(int i=1;i<=dayChars.length;i++){
				if(dayChars[i-1] == '1'){
					calendar = Calendar.getInstance();
					calendar.setTime(recentExecuteDate);
					if(i-1==31){
						calendar.set(Calendar.DATE, 1);
						calendar.add(Calendar.MONTH,1);
						calendar.add(Calendar.DATE, -1);
					}else{
						calendar.set(Calendar.DATE, i);
					}
					dates.add(calendar.getTime());
					calendar = Calendar.getInstance();
					calendar.setTime(recentExecuteDate);
					calendar.add(Calendar.MONTH,1);
					if(i-1==31){
						calendar.set(Calendar.DATE, 1);
						calendar.add(Calendar.MONTH,1);
						calendar.add(Calendar.DATE, -1);
					}else{
						calendar.set(Calendar.DATE, i);
					}
					dates.add(calendar.getTime());
				}
			}
		}
		return dates;
	}
}
