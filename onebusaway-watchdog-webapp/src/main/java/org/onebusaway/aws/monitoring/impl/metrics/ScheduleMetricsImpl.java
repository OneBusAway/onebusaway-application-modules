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
package org.onebusaway.aws.monitoring.impl.metrics;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.metrics.ScheduleMetrics;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class ScheduleMetricsImpl extends MetricsTemplate implements ScheduleMetrics, ApplicationListener<ContextRefreshedEvent> {

	private String watchdog_schedule_url = "";
	
	
	public void reloadConfig() {
		watchdog_schedule_url = _configurationService.getConfigurationValueAsString("monitoring.watchdogScheduleUrl", 
				"http://localhost:8080/onebusaway-watchdog-webapp/api/metric/schedule/");
		
	}
	
	@Override
	public void publishExpiryDateDeltaMetric() {
		String url = watchdog_schedule_url + "agency/1/expiry-date-delta";
		publishWatchdogMetric(url, MetricName.ScheduleExpiryDateDelta, StandardUnit.Count);
	}

	@Override
	public void publishTotalTripsMetric() {
		String url = watchdog_schedule_url + "trip/1/total";
		publishWatchdogMetric(url,  MetricName.ScheduleTotalTrips, StandardUnit.Count);
	}

	@Override
	public void publishAgencyTotalMetric() {
		String url = watchdog_schedule_url + "agency/total";
		publishWatchdogMetric(url,  MetricName.ScheduleAgencyTotal, StandardUnit.Count);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
	}

}
