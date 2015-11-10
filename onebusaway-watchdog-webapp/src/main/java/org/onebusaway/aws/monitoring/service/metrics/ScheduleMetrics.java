package org.onebusaway.aws.monitoring.service.metrics;

import java.util.List;

import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;

import com.amazonaws.services.cloudwatch.model.MetricDatum;


public interface ScheduleMetrics {
	void publishExpiryDateDeltaMetric();
	void publishTotalTripsMetric();
	void publishAgencyTotalMetric();
}
