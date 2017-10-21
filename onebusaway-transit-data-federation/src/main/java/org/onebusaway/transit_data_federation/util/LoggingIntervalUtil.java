/**
 * Copyright (C) 2017 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.util;

public class LoggingIntervalUtil {
	
	public static int getAppropriateLoggingInterval(int s){
				
		int interval = s/10;
		// rounded to lowest power of 10. 
		double exponent = Math.floor(Math.log10((double) interval));
		int roundedInterval = (int) Math.pow(10, exponent); 
		
		if (roundedInterval >= 1) {
			return roundedInterval;
		}
		else {
			return 1;
		}
		
	}

}
