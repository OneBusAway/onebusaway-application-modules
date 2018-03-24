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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.onebusaway.aws.cloudwatch.service.CloudwatchService;
import org.onebusaway.aws.monitoring.model.metrics.Metric;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class MetricsTemplate {
	
	protected static final Logger _log = LoggerFactory
			.getLogger(MetricsTemplate.class);
	
	protected static final int CONNECTION_TIMEOUT = 3000; 
	
	@Autowired
	CloudwatchService _cloudWatchService;
	
	@Autowired
	ConfigurationService _configurationService;
	
	public JsonObject getJsonObject(String uri) throws MalformedURLException,
			IOException, SocketTimeoutException {
		HttpURLConnection con = null;
		try{
			URL url = new URL(uri);
			con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(CONNECTION_TIMEOUT);
			con.setReadTimeout(CONNECTION_TIMEOUT);
			con.connect();
	
			// Convert to a JSON object to print data
			JsonParser jp = new JsonParser(); // from gson
			JsonElement root = jp.parse(new InputStreamReader((InputStream) con
					.getContent())); 
			
			// Convert the input stream to a
			// json element
			JsonObject rootobj = root.getAsJsonObject(); 
			// May be an array, may be an object.
			return rootobj;
		}catch (MalformedURLException mue){
			_log.warn(mue.getMessage());
			throw mue;	
		}catch (SocketTimeoutException ste){
			_log.warn("Connection to url : " + uri + " timed out after " + (CONNECTION_TIMEOUT / 1000) + " sec");
			throw ste;
		}catch (IOException ioe){
			_log.warn("Error communicating with specified url : " + uri);
			throw ioe;
		}finally{
			if(con != null) con.disconnect();
		}
	}

	public MetricDatum getMetricDatum(String metricName, Double value,
			StandardUnit unit) {
		return new MetricDatum().withMetricName(metricName)
				.withTimestamp(new Date()).withValue(value).withUnit(unit);
	}
	
	public MetricResponse getUrlWithResponseTime(String url) {
		long startTime = SystemTime.currentTimeMillis();
		double metric;
		HttpURLConnection con = null;
		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(CONNECTION_TIMEOUT);
			con.setReadTimeout(CONNECTION_TIMEOUT);
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				while(in.readLine() != null) {}
				in.close();
				metric = 0d;
			} else {
				metric = 1d;
			}
		} catch (SocketTimeoutException e){
			_log.warn("Connection to url : " + url + " timed out after " + (CONNECTION_TIMEOUT / 1000) + " sec");
			metric = 1d;
		} catch (IOException ioe) {
			_log.warn("Error communicating with specified url : " + url);
			metric = 1d;
		} 
		finally{
			if(con != null) con.disconnect();
		}
		
		return new MetricResponse(metric,
				(double) (SystemTime.currentTimeMillis() - startTime));
	}
	
	public String getUrlResponse(String url){
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				StringBuffer response = new StringBuffer();
				String inputLine;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				while((inputLine = in.readLine()) != null) {
					 response.append(inputLine);
				}
				in.close();
				return response.toString();
			}
		} catch (IOException ioe) {
			_log.warn("Error communicating with specified url : " + url);
		}
		
		return null;
	}
	
	public void publishWatchdogMetric(String url, MetricName metricName, StandardUnit unit){
		try{
			JsonObject json = getJsonObject(url);
			if(json != null){
				Gson gson = new Gson();
				Metric metric = gson.fromJson(json, Metric.class);
				publishMetric(metricName, StandardUnit.Count, Double.valueOf(metric.getMetricValue().toString()));
			}	
		}
		catch(MalformedURLException mue){
			_log.error(mue.getMessage());
			return;
		}
		catch(IOException ioe){
			_log.warn("Error communicating with specified url : " + url);
		}
		catch(NumberFormatException nfe){
			_log.error("Unable to convert metric " + metricName + " to a Double value");
		}
		catch(Exception e){
			_log.error("Error retreiving Watchdog Metric");
		}
	}
	
	public void publishMetric(MetricName metricName, StandardUnit unit, Double metric){
		_cloudWatchService.publishMetric(metricName.toString(), unit, metric);
	}
	
}
