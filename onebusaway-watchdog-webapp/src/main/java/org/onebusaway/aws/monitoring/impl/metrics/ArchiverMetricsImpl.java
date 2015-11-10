package org.onebusaway.aws.monitoring.impl.metrics;

import java.util.List;

import org.onebusaway.aws.monitoring.service.metrics.ArchiverMetrics;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

@Component
public class ArchiverMetricsImpl extends MetricsTemplate implements ArchiverMetrics {

	@Override
	public void publishArchiverMetric() {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishArchiverDBMetric() {
		// TODO Auto-generated method stub

	}

}
