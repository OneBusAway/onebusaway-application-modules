package org.onebusaway.aws.monitoring.service.alarms;

import java.util.List;

import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;

import com.amazonaws.services.cloudwatch.model.MetricDatum;


public interface WebappAlarms {
	void createDesktopUiValidAlarm();
	void createVehicleMonitoringAlarm();
	void createStopMonitoringAlarm();
	void createNextBusApiAlarm();
	void createSmsApiAlarm();
}
