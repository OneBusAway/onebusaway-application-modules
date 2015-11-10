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
