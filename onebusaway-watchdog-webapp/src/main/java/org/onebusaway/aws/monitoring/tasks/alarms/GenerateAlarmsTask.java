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
package org.onebusaway.aws.monitoring.tasks.alarms;

import org.onebusaway.aws.monitoring.service.alarms.AdminServerAlarms;
import org.onebusaway.aws.monitoring.service.alarms.RealtimeAlarms;
import org.onebusaway.aws.monitoring.service.alarms.WebappAlarms;
import org.onebusaway.aws.monitoring.tasks.metrics.PublishMetricsTask;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class GenerateAlarmsTask implements ApplicationListener<ContextRefreshedEvent>{
	
	private static final Logger _log = LoggerFactory
			.getLogger(GenerateAlarmsTask.class);
	
	@Autowired
	ConfigurationService _configService;
	
	@Autowired
	WebappAlarms webappAlarms;
	
	@Autowired
	AdminServerAlarms adminServerAlarms;
	
	@Autowired
	RealtimeAlarms realtimeAlarms;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(_configService != null){
			if(Boolean.parseBoolean(_configService.getConfigurationValueAsString("alarm.createAlarms", "false"))){
				
				webappAlarms.createDesktopUiValidAlarm();
				webappAlarms.createNextBusApiAlarm();
				webappAlarms.createSmsApiAlarm();
				webappAlarms.createStopMonitoringAlarm();
				
				adminServerAlarms.createCurrentBundleCountAlarm();
				adminServerAlarms.createFirstValidBundleFilesCountAlarm();
				
				// Realtime Location
				realtimeAlarms.createRealtimeInvalidLatLonPctAlarm();
				realtimeAlarms.createRealtimeLocationsInvalidAlarm();
				realtimeAlarms.createRealtimeLocationsTotalAlarm();
				realtimeAlarms.createRealtimeLocationsTotalPctAlarm();
				
				// Realtime Stops
				realtimeAlarms.createRealtimeStopsMatchedAlarm();
				realtimeAlarms.createRealtimeStopsMatchedPctAlarm();
				realtimeAlarms.createRealtimeStopsUnmatchedAlarm();
				realtimeAlarms.createRealtimeStopsUnmatchedPctAlarm();
				
				// Realtime Trips
				realtimeAlarms.createRealtimeTripsMatchedAlarm();
				realtimeAlarms.createRealtimeTripsMatchedAvgAlarm();
				realtimeAlarms.createRealtimeTripsTotalAlarm();
				realtimeAlarms.createRealtimeTripsUnmatchedAlarm();		
				realtimeAlarms.createRealtimeTripTotalPctAlarm();
				realtimeAlarms.createScheduleRealtimeDeltaAlarm();
				realtimeAlarms.createRealtimeBusesInServiceAlarm();
			
				_log.info("Created alarms");
			}
		}
	}

}
