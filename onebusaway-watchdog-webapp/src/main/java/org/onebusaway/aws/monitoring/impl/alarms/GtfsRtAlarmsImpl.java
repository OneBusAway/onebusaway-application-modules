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
import org.onebusaway.aws.monitoring.service.alarms.GtfsRtAlarms;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class GtfsRtAlarmsImpl extends AlarmsTemplate implements GtfsRtAlarms {

	@Override
	public void createMessagesSentAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getSQSMetricAlarmRequest(MetricName.NumberOfMessagesSent, getSqsQueue());
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setInsufficientDataActions(getCriticalAction());
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.sqsNumMessagesSent", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}

	@Override
	public void createMessagesReceivedAlarm() {
		String sqsQueue = _configService
				.getConfigurationValueAsString("alarm.sqsQueue", getEnv());
		PutMetricAlarmRequest putMetricAlarmRequest = getSQSMetricAlarmRequest(MetricName.NumberOfMessagesReceived, getSqsQueue());
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setInsufficientDataActions(getCriticalAction());
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.sqsNumMessagesReceived", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}

	@Override
	public void createMessagesDeletedAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getSQSMetricAlarmRequest(MetricName.NumberOfMessagesDeleted, getSqsQueue());
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setInsufficientDataActions(getCriticalAction());
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.sqsNumMessagesDeleted", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}

	@Override
	public void createMessagesSizeAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getSQSMetricAlarmRequest(MetricName.SentMessageSize, getSqsQueue());
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setInsufficientDataActions(getCriticalAction());
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.sqsSentMessageSize", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}

	@Override
	public void createMessagesDelayedAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getSQSMetricAlarmRequest(MetricName.NumberOfMessagesDelayed, getSqsQueue());
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setInsufficientDataActions(getCriticalAction());
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.sqsNumMessagesDelayed", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}
}
