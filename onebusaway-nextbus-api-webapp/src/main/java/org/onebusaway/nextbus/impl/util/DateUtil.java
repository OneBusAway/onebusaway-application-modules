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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

public class DateUtil {
	
	public static List<Long> getWeekdayDateTimes(String timeZoneId){
		
		List<Long> weekdayTimes = new ArrayList<Long>();
		
		DateTimeZone timeZone = DateTimeZone.forID(timeZoneId);
		DateTime today = new DateTime(timeZone).withTimeAtStartOfDay();
		
		weekdayTimes.add(today.withDayOfWeek(DateTimeConstants.MONDAY).getMillis() + 60000);
		weekdayTimes.add(today.withDayOfWeek(DateTimeConstants.TUESDAY).getMillis()+ 60000);
		weekdayTimes.add(today.withDayOfWeek(DateTimeConstants.WEDNESDAY).getMillis()+ 60000);
		weekdayTimes.add(today.withDayOfWeek(DateTimeConstants.THURSDAY).getMillis()+ 60000);
		weekdayTimes.add(today.withDayOfWeek(DateTimeConstants.FRIDAY).getMillis()+ 60000);
		
		return weekdayTimes;
	}
	
	public static List<Long> getWeekdayDateTimes(){
		return getWeekdayDateTimes(DateTimeZone.getDefault().getID());
	}
	
	public static List<Long> getWeekendDateTimes(String timeZoneId){

		List<Long> weekendTimes = new ArrayList<Long>();
		
		DateTimeZone timeZone = DateTimeZone.forID(timeZoneId);
		DateTime today = new DateTime(timeZone).withTimeAtStartOfDay();
		
		weekendTimes.add(today.withDayOfWeek(DateTimeConstants.SATURDAY).getMillis()+ 60000);
		weekendTimes.add(today.withDayOfWeek(DateTimeConstants.SUNDAY).getMillis()+ 60000);
		
		return weekendTimes;
		
	}
	
	public static List<Long> getWeekendDateTimes(){
		return getWeekendDateTimes(DateTimeZone.getDefault().getID());
	}
}
