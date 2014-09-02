package com.yuanwei.android.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateConverter {

	public static String getDateDifference(Date thenDate){
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        now.setTime(new Date());
        then.setTime(thenDate);

        
        // Get the represented date in milliseconds
        long nowMs = now.getTimeInMillis();
        long thenMs = then.getTimeInMillis();
        
        // Calculate difference in milliseconds
        long diff = nowMs - thenMs;
        
        // Calculate difference in seconds
        long diffMinutes = diff / (60 * 1000);
        long diffHours = diff / (60 * 60 * 1000);
        long diffDays = diff / (24 * 60 * 60 * 1000);

		if (diffMinutes<60){
			if (diffMinutes==0) return "Just updated";
		else if (diffMinutes==1)
				return diffMinutes + " minute ago";
			else
				return diffMinutes + " minutes ago";
		} else if (diffHours<24){
			if (diffHours==1)
				return diffHours + " hour ago";
			else
				return diffHours + " hours ago";
		}else if (diffDays<15){
			if (diffDays==1)
				return diffDays + " day ago";
			else
				return diffDays + " days ago";
		}else {
			return "a long time ago..";
		}
	}
	public static int getDateGap(String thenDate,SimpleDateFormat format){
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        now.setTime(new Date());
        try{
        	Date date=format.parse(thenDate);
        	then.setTime(date);
        }catch(Exception e){
        	e.printStackTrace();
        	return 100;
        }
        long nowMs = now.getTimeInMillis();
        long thenMs = then.getTimeInMillis();
        
        // Calculate difference in milliseconds
        long diff = nowMs - thenMs;
        return (int) (diff/(24*60*60*1000));
	}
	
}
