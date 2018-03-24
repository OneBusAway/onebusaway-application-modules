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
package org.onebusaway.aws.cloudwatch.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.aws.cloudwatch.service.CloudwatchService;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class CloudwatchServiceImpl implements CloudwatchService, ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	ConfigurationService _configurationService;
	
	AmazonCloudWatchClient cloudWatch;
	String endPoint = "monitoring.us-east-1.amazonaws.com";
	String environmentName = "dev";
	
	protected static final Logger _log = LoggerFactory.getLogger(CloudwatchServiceImpl.class);

	public void setup(){
		String accessKey = _configurationService.getConfigurationValueAsString("aws.accessKey", "");
		String secretKey = _configurationService.getConfigurationValueAsString("aws.secretKey", "");
		
		endPoint = _configurationService.getConfigurationValueAsString("aws.endPoint", "monitoring.us-east-1.amazonaws.com");
		environmentName = _configurationService.getConfigurationValueAsString("oba.env", "dev");
		
		AmazonCloudWatchClient cloudWatch = new AmazonCloudWatchClient(new BasicAWSCredentials(accessKey, secretKey));
		cloudWatch.setEndpoint(endPoint);
		this.cloudWatch = cloudWatch;
	}
	
	@Override
	public void publishMetric(String metricName, StandardUnit unit, Double metricValue){

        if(cloudWatch == null)
            return;

        MetricDatum datum = new MetricDatum().
                withMetricName(metricName).
                withTimestamp(new Date()).
                withValue(metricValue).
                withUnit(unit);
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().
                withNamespace(environmentName).
                withMetricData(datum);
        cloudWatch.putMetricData(putMetricDataRequest);
        
        _log.debug("published metric : " + putMetricDataRequest.toString());

    }
	
	
	public void publishMetrics(List<MetricDatum> data){

        if(cloudWatch == null)
            return;

        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest().
                withNamespace(environmentName).
                withMetricData(data);
        cloudWatch.putMetricData(putMetricDataRequest);
        
        _log.debug("published metrics : " + putMetricDataRequest.toString());

    }
	
	public void publishAlarm(PutMetricAlarmRequest putMetricAlarmRequest){
		cloudWatch.putMetricAlarm(putMetricAlarmRequest);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		setup();	
	}


}
