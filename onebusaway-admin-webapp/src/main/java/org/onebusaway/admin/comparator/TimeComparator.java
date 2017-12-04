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

import org.joda.time.DateTime;

/**
 * Compares vehicle records based on given times
 * @author abelsare
 *
 */
public class TimeComparator implements Comparator<DateTime>{

	private String order;
	
	public TimeComparator(String order) {
		this.order = order;
	}
	
	@Override
	public int compare(DateTime time1, DateTime time2) {
		if(order.equalsIgnoreCase("desc")) {
			return time2.compareTo(time1);
		}
		return time1.compareTo(time2);
	}
}
