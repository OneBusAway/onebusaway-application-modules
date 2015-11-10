package org.onebusaway.aws.monitoring.service.alarms;

import java.util.List;

import org.onebusaway.aws.monitoring.model.metrics.MetricName;
import org.onebusaway.aws.monitoring.model.metrics.MetricResponse;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;


public interface RealtimeAlarms {
	void createRealtimeLocationsTotalAlarm();
	void createRealtimeLocationsInvalidAlarm();
	void createRealtimeStopsMatchedAlarm();
	void createRealtimeStopsUnmatchedAlarm();
	
	void createRealtimeTripsTotalAlarm();
	void createRealtimeTripsMatchedAlarm();
	void createScheduleRealtimeDeltaAlarm();
	
	void createRealtimeLocationsTotalPctAlarm();
	void createRealtimeInvalidLatLonPctAlarm();
	void createRealtimeStopsMatchedPctAlarm();
	void createRealtimeStopsUnmatchedPctAlarm();
	void createRealtimeTripTotalPctAlarm();
	void createRealtimeTripsMatchedAvgAlarm();
	void createRealtimeTripsUnmatchedAlarm();
	
	void createRealtimeBusesInServiceAlarm();
}
