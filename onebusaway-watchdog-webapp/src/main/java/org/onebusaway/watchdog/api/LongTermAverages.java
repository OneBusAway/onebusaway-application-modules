package org.onebusaway.watchdog.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.watchdog.model.MetricConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LongTermAverages {
	  private static final Logger _log = LoggerFactory.getLogger(LongTermAverages.class);
	  private int _refreshInterval = 30;
	  private ScheduledExecutorService _scheduledExecutorService;
	  private ScheduledFuture<?> _refreshTask;
	  protected MetricConfiguration _configuration;	  
	  
	  static protected int matchedTripsAvg = 0;  // The current rolling average for matched trips
	  static protected int unmatchedTripsAvg = 0;
	  static protected int ROLLING_AVERAGE_COUNT = 10;// This is the number of recent updates included in the average. 
	                                                  // From a System property so it can be configured for fine tuning.
	                                                  // Default = 10, assuming an update every 30 seconds, this would cover 5 minutes.
	                                                  // This will be the same for both Matched Trips and Unmatched Trips.
	  static protected int[] recentMatchedTrips = new int[ROLLING_AVERAGE_COUNT];  // Array containing the most recent trip counts
	  static protected int[] recentUnmatchedTrips = new int[ROLLING_AVERAGE_COUNT];
	  static protected int currentMatchedTripCt = 0;  // Number of trip counts in the array.  Only relevant when the process is
	                                                  // restarting and the number of trip counts is less than ROLLING_AVERAGE_COUNT.
	  static protected int currentUnmatchedTripCt = 0;
	  static protected int earliestMatchedTripIdx = 0;// This indicates the earliest trip in the array and the one that will be replaced
	                                                  // by a new trip count.
	  static protected int earliestUnmatchedTripIdx = 0;
	  
	  public void setRefreshInterval(int refreshInterval) {
		    _refreshInterval = refreshInterval;
		  }

	  protected int getMatchedTripsAvg() {
		    return matchedTripsAvg;
		  }
			  
	  protected int getUnmatchedTripsAvg() {
	    return unmatchedTripsAvg;
	  }
				  
	  // Create timer to update averages every 30 seconds.
	  @PostConstruct
	  public void start() {
	    if (_refreshInterval > 0) {
	      _refreshTask = _scheduledExecutorService.scheduleAtFixedRate(new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
	    }
	  }

	  @PreDestroy
	  public void stop() {
	    if (_refreshTask != null) {
	      _refreshTask.cancel(true);
	      _refreshTask = null;
	    }
	  }

	  @Autowired
	  public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
	    _scheduledExecutorService = scheduledExecutorService;
	  }

	  @Autowired
	  public void setMetricConfiguration(MetricConfiguration mc) {
	    _configuration = mc;
	  }
	  
	  protected List<MonitoredDataSource> getDataSources() {
		    return _configuration.getDataSources();
		  }
		  
	  protected void updateLongTermMatchedTrips(int currentMatchedTrips) {
		    int idx = currentMatchedTripCt < ROLLING_AVERAGE_COUNT ? currentMatchedTripCt++ : earliestMatchedTripIdx++;
		    recentMatchedTrips[idx] = currentMatchedTrips;
		    earliestMatchedTripIdx %= ROLLING_AVERAGE_COUNT;
		    int sum = 0;
		    for (int i : recentMatchedTrips) {
		      sum += i;
		    }  
		    matchedTripsAvg = sum / currentMatchedTripCt;
		    return;
		  }	  

	  protected void updateLongTermUnmatchedTrips(int currentUnmatchedTrips) {
		    int idx = currentUnmatchedTripCt < ROLLING_AVERAGE_COUNT ? currentUnmatchedTripCt++ : earliestUnmatchedTripIdx++;
		    recentUnmatchedTrips[idx] = currentUnmatchedTrips;
		    earliestUnmatchedTripIdx %= ROLLING_AVERAGE_COUNT;

		    int sum = 0;
		    for (int i : recentUnmatchedTrips) {
		      sum += i;
		    }  
		    unmatchedTripsAvg = sum / currentUnmatchedTripCt;
		    return;
		  }

	  public void refresh() throws IOException {
	    	int matchedTripCt = 0;
	    	int unmatchedTripCt = 0;
		    for (MonitoredDataSource mds : getDataSources()) {
		        MonitoredResult result = mds.getMonitoredResult();
		        if (result == null) continue;
		        matchedTripCt += result.getMatchedTripIds().size();
		        unmatchedTripCt += result.getUnmatchedTripIds().size();
		    }	
		    updateLongTermMatchedTrips(matchedTripCt);
		    updateLongTermUnmatchedTrips(unmatchedTripCt);
		    _log.info("Averages updated: matched = " + matchedTripsAvg + ", unmatched = " + unmatchedTripsAvg );
		  }

		  /****
		   * Private Methods
		   ****/

	  private class RefreshTask implements Runnable {
	    @Override
	    public void run() {
	      try {
	        refresh();
	      } catch (Throwable ex) {
	        _log.warn("Error updating from GTFS-realtime data sources", ex);
	      }
	    }
	  }
}
