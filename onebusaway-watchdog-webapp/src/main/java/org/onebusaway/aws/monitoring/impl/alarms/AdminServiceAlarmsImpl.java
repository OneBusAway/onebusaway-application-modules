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

import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.alarms.AdminServerAlarms;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class AdminServiceAlarmsImpl extends AlarmsTemplate implements AdminServerAlarms {

	@Override
	public void createCurrentBundleCountAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.CurrentBundleCount);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.bundleCount", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}

	@Override
	public void createFirstValidBundleFilesCountAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.FirstValidBundleFilesCount);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.bundleFilesCount", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}
}
