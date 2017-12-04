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
package org.onebusaway.aws.monitoring.impl.alarms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.onebusaway.aws.cloudwatch.service.CloudwatchService;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;

@Component
public abstract class AlarmsTemplate implements ApplicationListener<ContextRefreshedEvent> {
	private final static String  ALARM = "Alarm";
	private final static String DEFAULT_ENV = "dev";
	
	private final static String CRITICAL_ACTION = "criticalAction";
	private final static String NON_CRITICAL_ACTION = "nonCriticalAction";
	
	private final static String AWS_RDS_DIMENSION = "DBInstanceIdentifier";
	private final static String AWS_RDS_NAMESPACE = "AWS/RDS";
	private final static String AWS_SQS_DIMENSION = "QueueName";
	private final static String AWS_SQS_NAMESPACE = "AWS/SQS";
	private final static String SQS_PREFIX = "Sqs";
	
	private String env = "";
	private String sqsQueue = "";
	
	private Map<String,String> actions = new HashMap<String,String>();
	
	
	@Autowired
	CloudwatchService _cloudWatchService;
	
	@Autowired
	ConfigurationService _configService;
	
	public String getEnv() {
		return env;
	}
	
	public String getSqsQueue() {
		return sqsQueue;
	}

	public Map<String,String> getActions() {
		return actions;
	}
	
	public List<String> getCriticalAction(){
		List<String> action = new ArrayList<String>(1);
		action.add(actions.get(CRITICAL_ACTION));
		return action;
	}
	
	public List<String> getNonCriticalAction(){
		List<String> action = new ArrayList<String>(1);
		action.add(actions.get(NON_CRITICAL_ACTION));
		return action;
	}

	protected PutMetricAlarmRequest getMetricAlarmRequest(MetricName metricName){
		return new PutMetricAlarmRequest()
		.withActionsEnabled(true)
		.withMetricName(metricName.toString())
		.withAlarmName(getAlarmName(metricName.toString()))
		.withPeriod(60)
		.withEvaluationPeriods(3)
		.withStatistic(Statistic.Average)
		.withComparisonOperator(ComparisonOperator.GreaterThanOrEqualToThreshold)
		.withNamespace(getEnv());
		
	}
	
	protected PutMetricAlarmRequest getRDSMetricAlarmRequest(MetricName metricName, String alarmName, String dbInstance){
		return new PutMetricAlarmRequest()
		.withActionsEnabled(true)
		.withMetricName(metricName.toString())
		.withAlarmName(getAlarmName(dbInstance + alarmName))
		.withPeriod(60)
		.withEvaluationPeriods(3)
		.withStatistic(Statistic.Average)
		.withComparisonOperator(ComparisonOperator.GreaterThanOrEqualToThreshold)
		.withNamespace(AWS_RDS_NAMESPACE)
		.withDimensions(new Dimension().withName(AWS_RDS_DIMENSION).withValue(dbInstance));
		
	}
	
	protected PutMetricAlarmRequest getSQSMetricAlarmRequest(MetricName metricName, String sqsQueue){
		return new PutMetricAlarmRequest()
		.withActionsEnabled(true)
		.withMetricName(metricName.toString())
		.withAlarmName(getAlarmName(SQS_PREFIX + metricName.toString()))
		.withPeriod(60)
		.withEvaluationPeriods(3)
		.withUnit(StandardUnit.Count)
		.withStatistic(Statistic.Average)
		.withComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold)
		.withNamespace(AWS_SQS_NAMESPACE)
		.withDimensions(new Dimension().withName(AWS_SQS_DIMENSION).withValue(sqsQueue));
		
	}
	
	protected String getAlarmName(String metricName){
		return metricName + ALARM + WordUtils.capitalize(getEnv());	
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(_configService == null){
			actions.put(CRITICAL_ACTION, "");
			actions.put(NON_CRITICAL_ACTION, "");
		}
		else {
			this.env = _configService.getConfigurationValueAsString("oba.env", DEFAULT_ENV);
			this.sqsQueue = _configService.getConfigurationValueAsString("alarm.sqsQueue", DEFAULT_ENV);
			actions.put(CRITICAL_ACTION, _configService.getConfigurationValueAsString("alarm.criticalSns", ""));
			actions.put(NON_CRITICAL_ACTION, _configService.getConfigurationValueAsString("alarm.nonCriticalSns", ""));
		}
	}
}
