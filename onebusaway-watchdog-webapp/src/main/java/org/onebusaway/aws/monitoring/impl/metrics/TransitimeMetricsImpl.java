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
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.metrics.TransitimeMetrics;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class TransitimeMetricsImpl extends MetricsTemplate implements TransitimeMetrics, ApplicationListener<ContextRefreshedEvent> {

	private String transitime_url = "";
	
	private void reloadConfig(){
		transitime_url = _configurationService.getConfigurationValueAsString("monitoring.transitimeUrl", "http://gtfsrt.dev.wmata.obaweb.org:8080/api/v1/key/4b248c1b/command/");
	}
	
	@Override
	public void publishGtfsRtMetric() {

		double metric = 1;
		try {
			JsonObject agencyList = getJsonObject(transitime_url + "agencies?format=json");
			JsonArray agencies = agencyList.get("agency").getAsJsonArray();
			for(JsonElement agencyElement : agencies){
				JsonObject agency = agencyElement.getAsJsonObject();
				if(agency != null){
					if (agency.get("name").getAsString().equalsIgnoreCase("WMATA")) {
						metric = 0;
						break;
					}
				}
			}
			publishMetric(MetricName.TransitimeApiErrorResponse, StandardUnit.Count, metric);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
		
	}

}
