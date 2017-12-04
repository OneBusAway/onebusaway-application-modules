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
package org.onebusaway.admin.comparator;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.onebusaway.admin.model.ui.VehicleStatus;

/**
 * Compares vehicles on pullout time
 * @author abelsare
 *
 */
public class PullinTimeComparator implements Comparator<VehicleStatus>{
	
	private TimeComparator timeComparator;
	private DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
	
	public PullinTimeComparator(String order) {
		timeComparator = new TimeComparator(order);
	}
	
	@Override
	public int compare(VehicleStatus o1, VehicleStatus o2) {
		if(StringUtils.isBlank(o1.getPullinTime())) {
			return 1;
		}
		if(StringUtils.isBlank(o2.getPullinTime())) {
			return -1;
		}
		
		DateTime time1 = formatter.parseDateTime(o1.getPullinTime());
		DateTime time2 = formatter.parseDateTime(o2.getPullinTime());
		
		return timeComparator.compare(time1, time2);
	}

}
