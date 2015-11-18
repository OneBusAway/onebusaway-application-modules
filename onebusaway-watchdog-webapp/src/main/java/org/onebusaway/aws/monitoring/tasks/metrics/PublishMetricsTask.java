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
package org.onebusaway.aws.monitoring.tasks.metrics;

import org.onebusaway.aws.monitoring.service.metrics.AdminServerMetrics;
import org.onebusaway.aws.monitoring.service.metrics.RealtimeMetrics;
import org.onebusaway.aws.monitoring.service.metrics.ScheduleMetrics;
import org.onebusaway.aws.monitoring.service.metrics.TransitimeMetrics;
import org.onebusaway.aws.monitoring.service.metrics.WebappMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;


public class PublishMetricsTask implements ApplicationListener<ContextRefreshedEvent>{

	@Autowired
	WebappMetrics webappMetrics;
	
	@Autowired
	AdminServerMetrics adminServerMetrics;
	
	@Autowired
	TransitimeMetrics transitimeMetrics;
	
	@Autowired
	ScheduleMetrics scheduledMetrics;
	
	@Autowired
	RealtimeMetrics realtimeMetrics;
	
	
	private static final Logger _log = LoggerFactory
			.getLogger(PublishMetricsTask.class);
	
	private boolean initialized = false;

	@Scheduled(fixedRate=60000)
	public void publishEveryMinute() {
		
		if(initialized){		
			webappMetrics.publishVehicleMonitoringMetrics();
			webappMetrics.publishStopMonitoringMetrics();
			webappMetrics.publishSmsApiMetrics();
			webappMetrics.publishNextBusApiMetrics();
			webappMetrics.publishDesktopUiMetrics();

			adminServerMetrics.publishBundleCountMetrics();
			
			transitimeMetrics.publishGtfsRtMetric();
			
			// Watchdog Metrics	
			
			scheduledMetrics.publishAgencyTotalMetric();
			scheduledMetrics.publishExpiryDateDeltaMetric();
			scheduledMetrics.publishTotalTripsMetric();
			
			realtimeMetrics.publishLocationMetrics();
			realtimeMetrics.publishStopMetrics();
			realtimeMetrics.publishTripMetrics();
			realtimeMetrics.publishDeltaMetrics();
			
			_log.info("Published Metrics");
		}
		else
			_log.info("Waiting to initialize PublishMetricsTask");

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.initialized = true;
	}

	
}
