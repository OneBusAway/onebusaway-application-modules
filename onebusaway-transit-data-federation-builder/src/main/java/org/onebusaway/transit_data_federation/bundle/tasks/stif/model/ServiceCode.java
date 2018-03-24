/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

import java.util.HashMap;

public enum ServiceCode {
	WEEKDAY_SCHOOL_OPEN, 
	WEEKDAY_SCHOOL_CLOSED, 
	SATURDAY, 
	SUNDAY, 
	MLK, 
	PRESIDENTS_DAY, 
	MEMORIAL_DAY, 
	GOOD_FRIDAY, 
	LABOR_DAY, 
	JULY_FOURTH, 
	COLUMBUS_DAY, 
	THANKSGIVING, 
	DAY_AFTER_THANKSGIVING, 
	CHRISTMAS_EVE, 
	CHRISTMAS_DAY, 
	CHRISTMAS_DAY_OBSERVED, 
	CHRISTMAS_WEEK, 
	NEW_YEARS_EVE, 
	NEW_YEARS_DAY, 
	NEW_YEARS_DAY_OBSERVED;

	static HashMap<String, ServiceCode> serviceCodeForGtfsId = new HashMap<String, ServiceCode>();
	static HashMap<ServiceCode, String> letterCodeForServiceCode = new HashMap<ServiceCode, String>();

	static {
		mapServiceCode("1", WEEKDAY_SCHOOL_OPEN);
		mapServiceCode("11", WEEKDAY_SCHOOL_CLOSED);
		mapServiceCode("2", SATURDAY);
		mapServiceCode("3", SUNDAY);
		mapServiceCode("E", WEEKDAY_SCHOOL_OPEN);
		mapServiceCode("C", WEEKDAY_SCHOOL_CLOSED);
		mapServiceCode("A", SATURDAY);
		mapServiceCode("D", SUNDAY);
		mapServiceCode("H", MLK);
		mapServiceCode("I", PRESIDENTS_DAY);
		mapServiceCode("J", GOOD_FRIDAY);
		mapServiceCode("K", MEMORIAL_DAY);
		mapServiceCode("M", JULY_FOURTH);
		mapServiceCode("N", LABOR_DAY);
		mapServiceCode("O", COLUMBUS_DAY);
		mapServiceCode("R", THANKSGIVING);
		mapServiceCode("S", DAY_AFTER_THANKSGIVING);
		mapServiceCode("T", CHRISTMAS_EVE);
		mapServiceCode("U", CHRISTMAS_DAY);
		mapServiceCode("V", CHRISTMAS_DAY_OBSERVED);
		mapServiceCode("W", CHRISTMAS_WEEK);
		mapServiceCode("X", NEW_YEARS_EVE);
		mapServiceCode("Y", NEW_YEARS_DAY);
		mapServiceCode("Z", NEW_YEARS_DAY_OBSERVED);
	}

	public static ServiceCode getServiceCodeForId(String id) {
		return serviceCodeForGtfsId.get(id);
	}

	private static void mapServiceCode(String string, ServiceCode serviceCode) {
		serviceCodeForGtfsId.put(string, serviceCode);
		if (Character.isLetter(string.charAt(0))) {
		  letterCodeForServiceCode.put(serviceCode, string);
		}
	}

	public static ServiceCode getServiceCodeForMTAHastusGTFS(String id) {
		String[] serviceIdParts = id.split("_");
		if(serviceIdParts.length != 2)
			return null;
		String[] serviceIdSubparts = serviceIdParts[1].split("-");
		if(serviceIdParts.length < 2)
			return null;
		
		String pickCode = serviceIdSubparts[0].toUpperCase();

		char pickCodeWithoutYear = pickCode.toCharArray()[0];
		if(pickCodeWithoutYear <= 'G') {
			if (id.contains("Weekday")) {
			    if (id.contains("SDon")) {
			      return WEEKDAY_SCHOOL_OPEN;
			    } else {
			      return WEEKDAY_SCHOOL_CLOSED;
			    }
			} else if (id.contains("Saturday")) {
			    return SATURDAY;
			} else if (id.contains("Sunday")) {
			    return SUNDAY;
			} else
			    return null;
		} else {
			// holiday code
			return serviceCodeForGtfsId.get(Character.toString(pickCodeWithoutYear));
		}
	}
	
	public String getLetterCode() {
	  return letterCodeForServiceCode.get(this);
	}

	public boolean isHoliday() {
    return !(this == WEEKDAY_SCHOOL_OPEN || this == WEEKDAY_SCHOOL_CLOSED
        || this == SATURDAY || this == SUNDAY);
	}

}
