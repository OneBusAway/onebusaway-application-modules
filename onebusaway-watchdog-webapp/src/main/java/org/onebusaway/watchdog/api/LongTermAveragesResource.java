/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.watchdog.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.watchdog.model.MetricConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class LongTermAveragesResource extends MetricResource {
	  protected static final Logger _log = LoggerFactory.getLogger(LongTermAveragesResource.class);
	  private int _refreshInterval = 30;
	  private ScheduledExecutorService _scheduledExecutorService;
	  private ScheduledFuture<?> _refreshTask;
	  private List<String> agencyIds;
	  
	  static protected int matchedTripsAvg = 0;  // The current rolling average for matched trips
	  static protected int unmatchedTripsAvg = 0;
	  static protected int ROLLING_AVERAGE_COUNT = 10;// This is the number of recent updates included in the average. 
	                                                  // From a System property so it can be configured for fine tuning.
	                                                  // Default = 10, assuming an update every 30 seconds, this would cover 5 minutes.
	                                                  // This will be the same for both Matched Trips and Unmatched Trips.
	  static protected int[] recentMatchedTrips = new int[ROLLING_AVERAGE_COUNT];  // Array containing the most recent trip counts
	  static protected int[] recentUnmatchedTrips = new int[ROLLING_AVERAGE_COUNT];
	  static protected int currentTripCt = 0;         // Number of trip counts in the arrays.  Only relevant when the process is
	                                                  // restarting and the number of trip counts is less than ROLLING_AVERAGE_COUNT.
	  static protected int earliestTripIdx = 0;       // This indicates the earliest trip in the arrays and the one that will be replaced
	                                                  // by a new trip count.
	  static private Map<String, int[]> matchedTripAveragesByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> unmatchedTripAveragesByAgency = new HashMap<String, int[]>();
	  
	  public void setRefreshInterval(int refreshInterval) {
		 _refreshInterval = refreshInterval;
      }
 
	  protected int getMatchedTripsAvg() {
		    return matchedTripsAvg;
	  }

	  protected int getAvgByAgency(String metricType, String agencyId) {
		  int[] metricTotals = null;
		  if (metricType.equals("matched-trips-average")) {
			  metricTotals = matchedTripAveragesByAgency.get(agencyId);
			  _log.info("matched by agency, agency: " + agencyId + ", totals: " + metricTotals);
		  } else if (metricType.equals("unmatched-trips-average")) {
			  metricTotals = unmatchedTripAveragesByAgency.get(agencyId);
			  _log.info("unmatched by agency, agency: " + agencyId + ", totals: " + metricTotals);
		  } else {
			  return 0;
		  }
		  return calcAverage(metricTotals);
	  }
	  	  
	  protected int getUnmatchedTripsAvg() {
	    return unmatchedTripsAvg;
	  }
				  
	  // Create timer to update averages every 30 seconds.
	  @PostConstruct
	  public void start() {
	    agencyIds = getAgencyList();
	    // Initialize maps
	    for (String agencyId : agencyIds) {
	      matchedTripAveragesByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
	      unmatchedTripAveragesByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
	    }
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
		   
	  protected void updateLongTermMatchedTrips(int currentMatchedTrips, int idx) {
		    recentMatchedTrips[idx] = currentMatchedTrips;
		    int sum = 0;
		    for (int i : recentMatchedTrips) {
		      sum += i;
		    }  
		    matchedTripsAvg = currentTripCt != 0 ? sum / currentTripCt : 0;
		    return;
		  }	  

	  protected void updateLongTermUnmatchedTrips(int currentUnmatchedTrips, int idx) {
		    recentUnmatchedTrips[idx] = currentUnmatchedTrips;
		    int sum = 0;
		    for (int i : recentUnmatchedTrips) {
		      sum += i;
		    }  
		    unmatchedTripsAvg = currentTripCt != 0 ? sum / currentTripCt : 0;
		    return;
		  }

	  public void refresh() throws IOException {
	    	int matchedTripCt = 0;
	    	int unmatchedTripCt = 0;
	    	Map<String, Integer> matchedByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> unmatchedByAgency = new HashMap<String, Integer>();
		    for (MonitoredDataSource mds : getDataSources()) {
		        MonitoredResult result = mds.getMonitoredResult();
		        if (result == null) continue;
		        matchedTripCt += result.getMatchedTripIds().size();
		        unmatchedTripCt += result.getUnmatchedTripIds().size();
		        for (String agencyId : result.getAgencyIds()) {
		        	Integer matchedTrips = matchedByAgency.get(agencyId);
		        	if (matchedTrips == null) {
		        		matchedTrips = 0;
		        	}
			        matchedTrips += result.getMatchedTripIds().size();
			        matchedByAgency.put(agencyId, matchedTrips);
			        
		        	Integer unmatchedTrips = unmatchedByAgency.get(agencyId);
		        	if (unmatchedTrips == null) {
		        		unmatchedTrips = 0;
		        	}
			        unmatchedTrips += result.getUnmatchedTripIds().size();
			        unmatchedByAgency.put(agencyId, matchedTrips);
		        }
		    }
        int idx = currentTripCt < ROLLING_AVERAGE_COUNT ? currentTripCt++ : earliestTripIdx++;
		    updateTotals(matchedTripAveragesByAgency, matchedByAgency, idx);
		    updateTotals(unmatchedTripAveragesByAgency, unmatchedByAgency, idx);
		    updateLongTermMatchedTrips(matchedTripCt, idx);
		    updateLongTermUnmatchedTrips(unmatchedTripCt, idx);
        earliestTripIdx %= ROLLING_AVERAGE_COUNT;
		    _log.info("Averages updated: matched = " + matchedTripsAvg + ", unmatched = " + unmatchedTripsAvg );
		  }

		  /****
		   * Private Methods
		   ****/
	  
	  private void updateTotals(Map<String, int[]> totalsByAgency, Map<String, Integer> latestTotalByAgency, int idx) {
	    for (String agencyId : totalsByAgency.keySet()) {
	      int[] totalsForThisAgency = totalsByAgency.get(agencyId);
	      Integer latestTotal = latestTotalByAgency.get(agencyId);
	      if (latestTotal == null) {
	        latestTotal = 0;
	      }
	      totalsForThisAgency[idx] = latestTotal;
	      totalsByAgency.put(agencyId, totalsForThisAgency);
	    }
	  }
	  
	  private List<String> getAgencyList() {
	    try {	      
	      List<AgencyWithCoverageBean> agencyBeans = getTDS().getAgenciesWithCoverage();
	      agencyIds = new ArrayList<String>();
	      for (AgencyWithCoverageBean agency : agencyBeans) {
	        agencyIds.add(agency.getAgency().getId());
	      }
	    } catch (Exception e) {
	      _log.error("getAgencyList broke", e);
	    }	 
	    return agencyIds;
	  }
	  
	  private int calcAverage(int[] metricTotals) {
	    int sum = 0;
	    for (int i : metricTotals) {
	      sum += i;
	    }  
	    return currentTripCt != 0 ? sum / currentTripCt : 0;	    
	  }

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
