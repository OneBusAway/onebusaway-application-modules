package org.onebusaway.transit_data_federation.util;

public class LoggingIntervalUtil {
	
	public int getAppropriateLoggingInterval(int s){
				
		int interval = s/20;
		
		if (interval > 0){
			return interval;
		} else {
			return 1;
		}
	}

}
