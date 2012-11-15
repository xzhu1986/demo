package au.com.isell.rlm.module.invoice;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.Assert;

/**
 * @author frankw 10/05/2012
 */
public class DateRangeLocater {
	private Date[] arr =null;
	
	public DateRangeLocater(Calendar start,int incField,int incAmount,int circulateTimes){
		arr = new Date[circulateTimes+1];
		if(incAmount>0){
			arr[0]=start.getTime();
			for(int i=1;i<=circulateTimes;i++){
				start.add(incField, incAmount);
				arr[i]=start.getTime();
			}
		}else{
			arr[arr.length-1]=start.getTime();
			for(int i=arr.length-2;i>=0;i--){
				start.add(incField, incAmount);
				arr[i]=start.getTime();
			}
		}
	}
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public int locateAt(Date target){
		Assert.notNull(target);
		long current=target.getTime();
		Date previous = arr[0];
		Assert.isTrue(current>=previous.getTime() && current<arr[arr.length-1].getTime(),"target data not in range");
		for(int i=1;i<arr.length;i++){
			if(current>=previous.getTime() && current<arr[i].getTime()){
				return i-1;
			}
			previous=arr[i];
		}
		return -1;
	}
	
	public Date[] getRangeSpliters(){
		return Arrays.copyOf(arr, arr.length-1);
	}

}
