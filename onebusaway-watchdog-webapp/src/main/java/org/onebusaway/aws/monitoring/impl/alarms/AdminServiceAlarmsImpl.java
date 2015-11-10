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
		putMetricAlarmRequest.setThreshold(1d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
		
	}

	@Override
	public void createFirstValidBundleFilesCountAlarm() {
		PutMetricAlarmRequest putMetricAlarmRequest = getMetricAlarmRequest(MetricName.FirstValidBundleFilesCount);
		putMetricAlarmRequest.setThreshold(1d);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);	
	}
}
