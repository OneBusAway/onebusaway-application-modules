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
package org.onebusaway.admin.service.exceptions;

import org.apache.commons.lang.StringUtils;

/**
 * Thrown when date validation fails
 * @author abelsare
 *
 */
public class DateValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String startDate;
	private String endDate;
	
	public DateValidationException(String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public String getMessage() {
		if(StringUtils.isBlank(startDate)) {
			return "Start date cannot be empty";
		}
		
		if(StringUtils.isBlank(endDate)) {
			return "End date cannot be empty";
		}
		
		return "Start date: " +startDate + " should be before End date: " +endDate;
	}
}
