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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.metrics.AdminServerMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class AdminServerMetricsImpl extends MetricsTemplate implements AdminServerMetrics, ApplicationListener<ContextRefreshedEvent>  {
	
	protected static final Logger _log = LoggerFactory
			.getLogger(WebappMetricsImpl.class);
	
	private static String admin_api_url = "";
	
	public void reloadConfig() {
		admin_api_url = _configurationService.getConfigurationValueAsString("monitoring.adminApiUrl", "");
	}
	
	@Override
	public void publishBundleCountMetrics() {
		double metric = 0;
		String bundleListUrl = admin_api_url + "bundle/list";
		try{
			JsonObject bundleList = getJsonObject(bundleListUrl);
			JsonArray bundles = bundleList.getAsJsonArray("bundles");
			for(JsonElement bundleElement : bundles){
				if(metric == 0){
					try{		
						JsonObject bundle = bundleElement.getAsJsonObject();
						
						Date today = new Date();
						Date dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse(bundle.get("service-date-from").getAsString());
						Date dateTo = new SimpleDateFormat("yyyy-MM-dd").parse(bundle.get("service-date-to").getAsString());
						
						if(dateFrom.compareTo(today) <= 0 && dateTo.compareTo(today) >= 0){
							JsonArray files = bundle.getAsJsonArray("files");
							double numberOfFiles = files == null ? 0 : files.size(); 	
							publishMetric(MetricName.FirstValidBundleFilesCount, StandardUnit.Count, numberOfFiles);
						}
							
					}
					catch (NullPointerException npe){
						_log.error("Unable to retreive the bundle start and end dates for bundle " + metric + 1);
					}
					catch (ParseException pe) {
						_log.error("Unable to parse the bundle start and end dates for bundle " + metric + 1);
					}
				}
				
				metric++;
			}
			publishMetric(MetricName.CurrentBundleCount, StandardUnit.Count, metric);	
		}
		catch(MalformedURLException mue){
			_log.error(mue.getMessage());
			return;
		}
		catch(IOException ioe){
			_log.warn("Error communicating with specified url : " + bundleListUrl);
			//publishMetric(MetricName.StopMonitoringErrorResponse, StandardUnit.Count, metric);
		}

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
	}

}
