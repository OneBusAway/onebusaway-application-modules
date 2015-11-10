package org.onebusaway.aws.monitoring.tasks.metrics;

import org.onebusaway.aws.cloudwatch.service.CloudwatchService;
import org.onebusaway.aws.monitoring.impl.metrics.WebappMetricsImpl;
import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;
import org.onebusaway.aws.monitoring.service.metrics.AdminServerMetrics;
import org.onebusaway.aws.monitoring.service.metrics.RealtimeMetrics;
import org.onebusaway.aws.monitoring.service.metrics.ScheduleMetrics;
import org.onebusaway.aws.monitoring.service.metrics.TransitimeMetrics;
import org.onebusaway.aws.monitoring.service.metrics.WebappMetrics;
import org.onebusaway.container.refresh.Refreshable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

public class PublishMetricsTask implements ApplicationListener<ContextRefreshedEvent>{

	@Autowired
	WebappMetrics webappMetrics;
	
	@Autowired
	AdminServerMetrics adminServerMetrics;
	
	@Autowired
	TransitimeMetrics transitimeMetrics;
	
	@Autowired
	ScheduleMetrics scheduledMetrics;
	
	@Autowired
	RealtimeMetrics realtimeMetrics;
	
	
	private static final Logger _log = LoggerFactory
			.getLogger(PublishMetricsTask.class);
	
	private boolean initialized = false;

	@Scheduled(fixedDelay = 60000)
	public void publishEveryMinute() {
		
		if(initialized){		
			webappMetrics.publishVehicleMonitoringMetrics();
			webappMetrics.publishStopMonitoringMetrics();
			webappMetrics.publishSmsApiMetrics();
			webappMetrics.publishNextBusApiMetrics();
			webappMetrics.publishDesktopUiMetrics();

			adminServerMetrics.publishBundleCountMetrics();
			
			transitimeMetrics.publishGtfsRtMetric();
			
			// Watchdog Metrics	
			
			scheduledMetrics.publishAgencyTotalMetric();
			scheduledMetrics.publishExpiryDateDeltaMetric();
			scheduledMetrics.publishTotalTripsMetric();
			
			realtimeMetrics.publishLocationMetrics();
			realtimeMetrics.publishStopMetrics();
			realtimeMetrics.publishTripMetrics();
			realtimeMetrics.publishDeltaMetrics();
			
			_log.info("Published Metrics");
		}
		else
			_log.info("Waiting to initialize PublishMetricsTask");

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.initialized = true;
	}

	
}
