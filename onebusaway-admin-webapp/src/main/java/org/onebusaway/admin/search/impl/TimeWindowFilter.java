/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.search.impl;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.onebusaway.admin.model.ui.VehicleStatus;
import org.onebusaway.admin.search.Filter;

/**
 * Filters vehicles whose last received time falls within the given time window (in seconds)
 * @author abelsare
 *
 */
public class TimeWindowFilter implements Filter<VehicleStatus>{

	private int timeWindow;
	
	public TimeWindowFilter(int timeWindow) {
		this.timeWindow = timeWindow;
	}
	
	@Override
	public boolean apply(VehicleStatus type) {
		if(StringUtils.isNotBlank(type.getTimeReported())) {
			BigDecimal timeDifference = getTimeDifference(type.getTimeReported());
			return timeDifference.compareTo(new BigDecimal(timeWindow * 60)) <= 0;
		}
		return false;
	}
	
	private BigDecimal getTimeDifference(String timeReported) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		DateTime lastReportedTime = formatter.parseDateTime(timeReported);
		DateTime now = new DateTime();
		int seconds = Seconds.secondsBetween(lastReportedTime, now).getSeconds();
		BigDecimal difference = new BigDecimal(seconds);
		return difference;
	}

}
