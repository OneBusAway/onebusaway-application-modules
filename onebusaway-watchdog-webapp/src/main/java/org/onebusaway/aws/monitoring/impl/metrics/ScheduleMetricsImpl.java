package org.onebusaway.aws.monitoring.impl.metrics;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.service.metrics.ScheduleMetrics;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

@Component
public class ScheduleMetricsImpl extends MetricsTemplate implements ScheduleMetrics, ApplicationListener<ContextRefreshedEvent> {

	private String watchdog_schedule_url = "";
	
	
	public void reloadConfig() {
		watchdog_schedule_url = _configurationService.getConfigurationValueAsString("monitoring.watchdogScheduleUrl", 
				"http://localhost:8080/onebusaway-watchdog-webapp/api/metric/schedule/");
		
	}
	
	@Override
	public void publishExpiryDateDeltaMetric() {
		String url = watchdog_schedule_url + "agency/1/expiry-date-delta";
		publishWatchdogMetric(url, MetricName.ScheduleExpiryDateDelta, StandardUnit.Count);
	}

	@Override
	public void publishTotalTripsMetric() {
		String url = watchdog_schedule_url + "trip/1/total";
		publishWatchdogMetric(url,  MetricName.ScheduleTotalTrips, StandardUnit.Count);
	}

	@Override
	public void publishAgencyTotalMetric() {
		String url = watchdog_schedule_url + "agency/total";
		publishWatchdogMetric(url,  MetricName.ScheduleAgencyTotal, StandardUnit.Count);
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		reloadConfig();
	}

}
