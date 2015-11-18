package org.onebusaway.aws.monitoring.impl.alarms;

import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.alarms.DatabaseAlarms;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class DatabaseAlarmsImpl extends AlarmsTemplate implements
		DatabaseAlarms {

	@Override
	public void createRdsHighConnectionsAlarm(String dbInstance) {
		PutMetricAlarmRequest putMetricAlarmRequest = getRDSMetricAlarmRequest(
				MetricName.DatabaseConnections, "RdsHighConnections",
				dbInstance);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest
				.setThreshold(_configService.getConfigurationValueAsDouble(
						"alarm.dbHighConnections", 400d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}

	@Override
	public void createRdsLowStorageAlarm(String dbInstance) {
		PutMetricAlarmRequest putMetricAlarmRequest = getRDSMetricAlarmRequest(
				MetricName.FreeStorageSpace, "RdsLowStorageSpace", dbInstance);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.dbFreeStorageSpace",
						3000000000d));
		putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);

	}

	@Override
	public void createRdsReadLatencyAlarm(String dbInstance) {
		PutMetricAlarmRequest putMetricAlarmRequest = getRDSMetricAlarmRequest(
				MetricName.ReadLatency, "RdsReadLatency", dbInstance);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.dbReadLatency", 0.3d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);

	}

	@Override
	public void createRdsWriteLatencyAlarm(String dbInstance) {
		PutMetricAlarmRequest putMetricAlarmRequest = getRDSMetricAlarmRequest(
				MetricName.WriteLatency, "RdsWriteLatency", dbInstance);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.dbWriteLatency", 0.3d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);
	}

	@Override
	public void createRdsHighCPUAlarm(String dbInstance) {
		PutMetricAlarmRequest putMetricAlarmRequest = getRDSMetricAlarmRequest(
				MetricName.CPUUtilization, "RdsHighCPU", dbInstance);
		putMetricAlarmRequest.setAlarmActions(getCriticalAction());
		putMetricAlarmRequest.setUnit(StandardUnit.Count);
		putMetricAlarmRequest.setThreshold(_configService
				.getConfigurationValueAsDouble("alarm.dbCPUUtilization", 75d));
		_cloudWatchService.publishAlarm(putMetricAlarmRequest);

	}

}
