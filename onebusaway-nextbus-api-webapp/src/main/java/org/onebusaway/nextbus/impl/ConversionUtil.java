package org.onebusaway.nextbus.impl;

import java.util.Date;
import java.util.TimeZone;

public class ConversionUtil {
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

}
