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
package org.onebusaway.aws.monitoring.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.aws.monitoring.service.alarms.AdminServerAlarms;
import org.onebusaway.aws.monitoring.service.alarms.DatabaseAlarms;
import org.onebusaway.aws.monitoring.service.alarms.GtfsRtAlarms;
import org.onebusaway.aws.monitoring.service.alarms.RealtimeAlarms;
import org.onebusaway.aws.monitoring.service.alarms.WebappAlarms;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.watchdog.api.MetricResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@Path("/monitoring/alarms")
public class AlarmResource extends MetricResource {

	private static final Logger _log = LoggerFactory
			.getLogger(AlarmResource.class);
	
	@Autowired
	WebappAlarms webappAlarms;

	@Autowired
	AdminServerAlarms adminServerAlarms;

	@Autowired
	RealtimeAlarms realtimeAlarms;
	
	@Autowired
	DatabaseAlarms databaseAlarms;
	
	@Autowired
	GtfsRtAlarms gtfsRtAlarms;
	
	@Autowired
	ConfigurationService _configurationService;

	@Path("create")
	@GET
	@Produces("application/json")
	public Response createAlarms() {
	  StringBuffer logMsg = new StringBuffer();
	  logMsg.append("starting\n");
		try {
			webappAlarms.createDesktopUiValidAlarm();
			webappAlarms.createNextBusApiAlarm();
			webappAlarms.createSmsApiAlarm();
			webappAlarms.createStopMonitoringAlarm();
			logMsg.append("webappAlarms created\n");

			adminServerAlarms.createCurrentBundleCountAlarm();
			adminServerAlarms.createFirstValidBundleFilesCountAlarm();
			logMsg.append("adminServerAlarms created\n");
			

			// Realtime Location
//			realtimeAlarms.createRealtimeInvalidLatLonPctAlarm();
//			realtimeAlarms.createRealtimeLocationsInvalidAlarm();
//			realtimeAlarms.createRealtimeLocationsTotalAlarm();
//			realtimeAlarms.createRealtimeLocationsTotalPctAlarm();

			// Realtime Stops
//			realtimeAlarms.createRealtimeStopsMatchedAlarm();
//			realtimeAlarms.createRealtimeStopsMatchedPctAlarm();
//			realtimeAlarms.createRealtimeStopsUnmatchedAlarm();
//			realtimeAlarms.createRealtimeStopsUnmatchedPctAlarm();

			// Realtime Trips
//			realtimeAlarms.createRealtimeTripsMatchedAlarm();
//			realtimeAlarms.createRealtimeTripsMatchedAvgAlarm();
//			realtimeAlarms.createRealtimeTripsTotalAlarm();
//			realtimeAlarms.createRealtimeTripsUnmatchedAlarm();
//			realtimeAlarms.createRealtimeTripTotalPctAlarm();
//			realtimeAlarms.createScheduleRealtimeDeltaAlarm();
//			realtimeAlarms.createRealtimeBusesInServiceAlarm();
			
			// SQS Alarms
			gtfsRtAlarms.createMessagesDelayedAlarm();
			gtfsRtAlarms.createMessagesDeletedAlarm();
			gtfsRtAlarms.createMessagesReceivedAlarm();
			gtfsRtAlarms.createMessagesSentAlarm();
			gtfsRtAlarms.createMessagesSizeAlarm();		
			logMsg.append("gtfsRtAlarms created\n");
			
			
			// RDS Alarms
			if (getDbInstances() != null) {
  			for(String dbInstance : getDbInstances()){
  				databaseAlarms.createRdsHighConnectionsAlarm(dbInstance);
  				databaseAlarms.createRdsHighCPUAlarm(dbInstance);
  				databaseAlarms.createRdsLowStorageAlarm(dbInstance);
  				databaseAlarms.createRdsReadLatencyAlarm(dbInstance);
  				databaseAlarms.createRdsWriteLatencyAlarm(dbInstance);
  				logMsg.append("Db " + dbInstance + " alarms created\n");
  			}
			} else {
			  logMsg.append("no db isntances defined!\n");
			}
			logMsg.append("Alarms created successfully\n");
			_log.info(logMsg.toString());
			return Response.ok(logMsg.toString()).build();
			
		} catch (Exception e) {
			_log.error("Error creating alarms");
			return Response.ok("Error Creating Alarms").build();
		}
	}
	
	private List<String> getDbInstances(){
		String dbInstances = _configurationService.getConfigurationValueAsString("alarm.dbInstances", "");
		if(StringUtils.isNotBlank(dbInstances)){
			return Arrays.asList(dbInstances.split("\\s*,\\s*"));
		}
		return new ArrayList<String>(0);
		
	}
}
