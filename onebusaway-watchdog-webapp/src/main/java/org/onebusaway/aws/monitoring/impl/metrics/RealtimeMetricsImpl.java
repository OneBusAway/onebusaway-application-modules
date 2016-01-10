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
import org.onebusaway.aws.monitoring.service.metrics.RealtimeMetrics;
import org.onebusaway.aws.monitoring.service.metrics.ScheduleMetrics;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class RealtimeMetricsImpl extends MetricsTemplate implements RealtimeMetrics, ApplicationListener<ContextRefreshedEvent> {

	private String watchdog_rt_location_url = "";
	
	private String watchdog_rt_stop_url = "";
	
	private String watchdog_rt_trip_url = "";
	
	private String watchdog_rt_delta_url = "";
	
	public void reloadConfig() {
		watchdog_rt_location_url = _configurationService.getConfigurationValueAsString("monitoring.watchdogRealtimeLocationUrl", "http://localhost:8080/onebusaway-watchdog-webapp/api/metric/realtime/location/1/");
		watchdog_rt_stop_url = _configurationService.getConfigurationValueAsString("monitoring.watchdogRealtimeStopUrl", "http://localhost:8080/onebusaway-watchdog-webapp/api/metric/realtime/stop/1/");
		watchdog_rt_trip_url = _configurationService.getConfigurationValueAsString("monitoring.watchdogRealtimeTripUrl", "http://localhost:8080/onebusaway-watchdog-webapp/api/metric/realtime/trip/1/");
		watchdog_rt_delta_url = _configurationService.getConfigurationValueAsString("monitoring.watchdogRealtimeDeltaUrl", "http://localhost:8080/onebusaway-watchdog-webapp/api/metric/realtime/delta/1/");
	}

	@Override
	public void publishLocationMetrics() {
		publishWatchdogMetric(watchdog_rt_location_url + "total", MetricName.RealtimeLocationsTotal, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_location_url + "invalid", MetricName.RealtimeLocationsInvalid, StandardUnit.Count);	
	}

	@Override
	public void publishStopMetrics() {
		publishWatchdogMetric(watchdog_rt_stop_url + "matched", MetricName.RealtimeStopsMatched, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_stop_url + "unmatched", MetricName.RealtimeStopsUnmatched, StandardUnit.Count);
	}

	@Override
	public void publishTripMetrics() {
		
		publishWatchdogMetric(watchdog_rt_trip_url + "total", MetricName.RealtimeTripsTotal, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_trip_url + "matched", MetricName.RealtimeTripsMatched, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_trip_url + "unmatched", MetricName.RealtimeTripsUnmatched, StandardUnit.Count);
		
		publishWatchdogMetric(watchdog_rt_trip_url + "schedule-realtime-delta", MetricName.ScheduleRealtimeDelta, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_trip_url + "buses-in-service-percent", MetricName.RealtimeBusesInServicePct, StandardUnit.Count);
	}

	@Override
	public void publishDeltaMetrics() {
		
		publishWatchdogMetric(watchdog_rt_delta_url + "location-total-pct", MetricName.RealtimeLocationsTotalPct, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_delta_url + "location-invalid-lat-lon-pct", MetricName.RealtimeInvalidLatLonPct, StandardUnit.Count);
		
		publishWatchdogMetric(watchdog_rt_delta_url + "matched-stops-pct", MetricName.RealtimeStopsMatchedPct, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_delta_url + "unmatched-stops-pct", MetricName.RealtimeStopsUnmatchedPct, StandardUnit.Count);
		
		publishWatchdogMetric(watchdog_rt_delta_url + "trip-total-pct", MetricName.RealtimeTripTotalPct, StandardUnit.Count);
		publishWatchdogMetric(watchdog_rt_delta_url + "average-matched-trips", MetricName.RealtimeTripsMatchedAvg, StandardUnit.Count);
		
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
	}

}
