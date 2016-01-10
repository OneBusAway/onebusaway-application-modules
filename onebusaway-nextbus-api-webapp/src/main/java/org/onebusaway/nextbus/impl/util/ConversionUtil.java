/**
 * Copyright (C) 2015 Cambridge Systematics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
