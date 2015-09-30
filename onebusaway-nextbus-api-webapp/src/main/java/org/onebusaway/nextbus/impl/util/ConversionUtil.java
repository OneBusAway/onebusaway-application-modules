package org.onebusaway.nextbus.impl.util;

import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ConversionUtil {
	
	private static DateTimeZone timeZone = DateTimeZone.forID("America/New_York");
	
	public static Date convertLocalDateToDateTimezone( Date localDate, String tzone ) {
		   TimeZone localTimeZone = TimeZone.getDefault();
		   TimeZone timezone = TimeZone.getTimeZone( tzone );
		   long gmtMillis = localDate.getTime();
		   long result = gmtMillis + timezone.getOffset( gmtMillis ) - localTimeZone.getOffset(gmtMillis);
		   return new Date( result );
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
