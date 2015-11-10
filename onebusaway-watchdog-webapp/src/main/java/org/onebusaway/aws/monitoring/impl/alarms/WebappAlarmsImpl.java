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
import org.onebusaway.aws.monitoring.service.alarms.WebappAlarms;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class WebappAlarmsImpl extends AlarmsTemplate implements WebappAlarms {

	@Override
	public void createDesktopUiValidAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.DesktopUiValid);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.webDesktopUiValid", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}


	@Override
	public void createVehicleMonitoringAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.VehicleMonitoringErrorResponse);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.webVehicleMonitoringError", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
		
	}

	@Override
	public void createStopMonitoringAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.StopMonitoringErrorResponse);
		putMetricAlarmRequest.setThreshold(1d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.webStopMonitoringError", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}
	

	@Override
	public void createNextBusApiAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.NextBusApiErrorResponse);
		putMetricAlarmRequest.setThreshold(1d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.webNextBusApiError", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createSmsApiAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.SMSWebappErrorResponse);
		putMetricAlarmRequest.setThreshold(1d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService.getConfigurationValueAsDouble("alarm.webSMSWebappError", 1d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}
}
