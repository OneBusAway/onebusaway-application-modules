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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;
import org.onebusaway.aws.monitoring.service.metrics.WebappMetrics;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.JsonObject;

@Component
public class WebappMetricsImpl extends MetricsTemplate implements
		WebappMetrics, ApplicationListener<ContextRefreshedEvent> {

	protected static final Logger _log = LoggerFactory
			.getLogger(WebappMetricsImpl.class);

	private boolean initialized;

	private static String vehicle_monitoring_url = "";

	private String stop_monitoring_url = "";

	private String sms_webapp_url = "";

	private String nextbus_api_url = "";

	private String webapp_url = "";

	
	public void reloadConfig() {
		vehicle_monitoring_url = _configurationService.getConfigurationValueAsString("monitoring.vehicleMonitoringUrl", "http://localhost:8080/onebusaway-api-webapp/siri/vehicle-monitoring?key=OBAKEY&type=xml&LineRef=70");
		stop_monitoring_url = _configurationService.getConfigurationValueAsString("monitoring.stopMonitoringUrl","http://localhost:8080/onebusaway-api-webapp/siri/stop-monitoring?key=OBAKEY&MonitoringRef=9653&type=json");
		sms_webapp_url = _configurationService.getConfigurationValueAsString("monitoring.smsWebappUrl","http://localhost:8080/onebusaway-sms-webapp/");
		nextbus_api_url = _configurationService.getConfigurationValueAsString("monitoring.nextbusApiUrl","http://localhost:8080/onebusaway-nextbus-api-webapp/service/publicJSONFeed?command=agencyList");
		webapp_url = _configurationService.getConfigurationValueAsString("monitoring.webappUrl","http://localhost:8080/");
	}

	@Override
	public void publishVehicleMonitoringMetrics() {
		MetricResponse metricResponse = getUrlWithResponseTime(vehicle_monitoring_url);
		publishMetric(
				MetricName.VehicleMonitoringResponseTime,
				StandardUnit.Milliseconds, metricResponse.getResponseTime());
		publishMetric(
				MetricName.VehicleMonitoringErrorResponse, StandardUnit.Count,
				metricResponse.getMetric());
	}

	@Override
	public void publishStopMonitoringMetrics() {
		MetricResponse metricResponse = getUrlWithResponseTime(stop_monitoring_url);
		publishMetric(MetricName.StopMonitoringResponseTime,
				StandardUnit.Milliseconds, metricResponse.getResponseTime());
		publishMetric(
				MetricName.StopMonitoringErrorResponse, StandardUnit.Count,
				metricResponse.getMetric());
	}

	@Override
	public void publishNextBusApiMetrics() {
		double metric = 1;
		try {
			JsonObject agencyList = getJsonObject(nextbus_api_url);
			JsonObject agency = (JsonObject) agencyList.get("agency");
			if (agency.get("title").getAsString().equalsIgnoreCase("WMATA")) {
				metric = 0;
			}
			publishMetric(
					MetricName.NextBusApiErrorResponse, StandardUnit.Count,
					metric);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
	}


	@Override
	public void publishSmsApiMetrics() {
		MetricResponse metricResponse = getUrlWithResponseTime(sms_webapp_url);
		publishMetric(MetricName.SMSWebappResponseTime,
				StandardUnit.Milliseconds, metricResponse.getResponseTime());
		publishMetric(MetricName.SMSWebappErrorResponse,
				StandardUnit.Count, metricResponse.getMetric());
	}

	@Override
	public void publishDesktopUiMetrics() {
		try {
			Document doc = Jsoup.connect(webapp_url).get();
			Elements searchBar = doc
					.select("form[action='search.action'] > input[name='q']");
			publishMetric(MetricName.DesktopUiValid,
					StandardUnit.Count, (double) searchBar.size());
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
	}
}
