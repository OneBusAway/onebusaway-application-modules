package org.onebusaway.aws.cloudwatch.service;

import java.util.List;

import org.onebusaway.aws.monitoring.model.metrics.MetricName;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public interface CloudwatchService {
	void publishMetric(String metricName, StandardUnit unit, Double metricValue);
	void publishMetrics(List<MetricDatum> data);
	void publishAlarm(PutMetricAlarmRequest putMetricAlarmRequest);
}
