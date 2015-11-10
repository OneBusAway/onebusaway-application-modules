package org.onebusaway.aws.monitoring.impl.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.onebusaway.aws.cloudwatch.service.CloudwatchService;
import org.onebusaway.aws.monitoring.model.metrics.Metric;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;
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

public class MetricsTemplate {
	
	protected static final Logger _log = LoggerFactory
			.getLogger(MetricsTemplate.class);
	
	@Autowired
	CloudwatchService _cloudWatchService;
	
	@Autowired
	ConfigurationService _configurationService;
	
	public JsonObject getJsonObject(String uri) throws MalformedURLException,
			IOException {
		URL url = new URL(uri);
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.connect();

		// Convert to a JSON object to print data
		JsonParser jp = new JsonParser(); // from gson
		JsonElement root = jp.parse(new InputStreamReader((InputStream) request
				.getContent())); 
		
		// Convert the input stream to a
		// json element
		JsonObject rootobj = root.getAsJsonObject(); 
		// May be an array, may be an object.
		return rootobj;
	}

	public MetricDatum getMetricDatum(String metricName, Double value,
			StandardUnit unit) {
		return new MetricDatum().withMetricName(metricName)
				.withTimestamp(new Date()).withValue(value).withUnit(unit);
	}
	
	public MetricResponse getUrlWithResponseTime(String url) {
		long startTime = System.currentTimeMillis();
		double metric;
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				while(in.readLine() != null) {}
				in.close();
				metric = 0d;
			} else {
				metric = 1d;
			}
		} catch (IOException ioe) {
			_log.warn("Error communicating with specified url : " + url);
			metric = 1d;
		}
		
		return new MetricResponse(metric,
				(double) (System.currentTimeMillis() - startTime));
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
