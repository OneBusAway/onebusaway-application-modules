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
