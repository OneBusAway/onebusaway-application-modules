package org.onebusaway.aws.monitoring.impl.metrics;

import java.util.List;

import org.onebusaway.aws.monitoring.service.metrics.RSSMetrics;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

@Component
public class RSSMetricsImpl implements RSSMetrics {

	@Override
	public void publishServiceAlertFeedStatusMetric() {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishServiceAlertFeedSizeMetric() {
		// TODO Auto-generated method stub

	}

}
