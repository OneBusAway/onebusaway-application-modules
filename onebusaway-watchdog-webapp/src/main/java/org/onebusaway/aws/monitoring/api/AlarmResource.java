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

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.onebusaway.aws.monitoring.service.alarms.AdminServerAlarms;
import org.onebusaway.aws.monitoring.service.alarms.RealtimeAlarms;
import org.onebusaway.aws.monitoring.service.alarms.WebappAlarms;
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

	@Path("create")
	@GET
	@Produces("application/json")
	public Response createAlarms() {
		try {
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
			
			_log.error("Alarms created successfully");
			return Response.ok("Alarms Created Successfully").build();
			
		} catch (Exception e) {
			_log.error("Error creating alarms");
			return Response.ok("Error Creating Alarms").build();
		}
	}
}
