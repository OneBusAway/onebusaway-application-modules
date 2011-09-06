/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

public enum ServiceCode {
	WEEKDAY_SCHOOL_OPEN(1),
	WEEKDAY_SCHOOL_CLOSED(11),
	SATURDAY(2),
	SUNDAY(3), 
	HOLIDAY(12);
	
	private int code;

	ServiceCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	static ServiceCode valueOf(int code) {
		switch(code) {
			case 1:
				return WEEKDAY_SCHOOL_OPEN;
			case 11:
				return WEEKDAY_SCHOOL_CLOSED;
			case 2:
				return SATURDAY;
			case 3: 
				return SUNDAY;
      case 12:
        return HOLIDAY;
		}
		return null;
	}

}
