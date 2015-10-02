package org.onebusaway.nextbus.impl.util;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ConversionUtil {
	
	private static DateTimeZone timeZone = DateTimeZone.forID(TimeZone.getDefault().getID());
	
	public static Date convertLocalDateToDateTimezone( Date date, String tzone ) {
		   TimeZone timezone = TimeZone.getTimeZone( tzone );
		   long gmtMillis = date.getTime();
		   long result = gmtMillis + timezone.getOffset( gmtMillis );
		   return new Date( result );
	}
	
	public static Date convertLocalDateToDateTimezone( Date date) {
		   return convertLocalDateToDateTimezone(date, TimeZone.getDefault().getID());
	}
	
	public static double footToMeter(double foot){
		return 0.305*foot;
	}

	public static double meterToFoot(double meter){
		return meter/0.305;
	}
	
	public static long getStartofDayTime(Date date){
		DateTime dateTime = new DateTime(date, timeZone);
		dateTime = dateTime.withTimeAtStartOfDay();
		return dateTime.getMillis();	
	}

}
