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

import cern.colt.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	  static private Map<String, int[]> busesInServicePctByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> stopsMatchedByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> stopsUnmatchedByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> tripScheduleRealtimeDiffByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> tripTotalsByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> locationInvalidLatLonByAgency = new HashMap<String, int[]>();
	  static private Map<String, int[]> locationTotalsByAgency = new HashMap<String, int[]>();
	  
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
			  _log.debug("matched by agency, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
		  } else if (metricType.equals("unmatched-trips-average")) {
			  metricTotals = unmatchedTripAveragesByAgency.get(agencyId);
			  _log.debug("unmatched by agency, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("buses-in-service-pct")) {
        metricTotals = busesInServicePctByAgency.get(agencyId);
        _log.debug("buses in service pct, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("matched-stops")) {
        metricTotals = stopsMatchedByAgency.get(agencyId);
        _log.debug("stops matched, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("unmatched-stops")) {
        metricTotals = stopsUnmatchedByAgency.get(agencyId);
        _log.debug("stops unmatched, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("trip-total")) {
        metricTotals = tripTotalsByAgency.get(agencyId);
        _log.debug("trip-total, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("trip-schedule-realtime-diff")) {
        metricTotals = tripScheduleRealtimeDiffByAgency.get(agencyId);
        _log.debug("trip-schedule-realtime-diff, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("location-total")) {
        metricTotals = locationTotalsByAgency.get(agencyId);
        _log.debug("location-total, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
      } else if (metricType.equals("location-invalid-lat-lon")) {
        metricTotals = locationInvalidLatLonByAgency.get(agencyId);
        _log.debug("location-invalid-lat-lon, agency: " + agencyId + ", totals: " + (metricTotals!=null?Arrays.toString(metricTotals):0));
		  } else {
			  return 0;
		  }
		  return metricTotals != null ? calcAverage(metricTotals) : 0;
	  }
	  	  
	  protected int getUnmatchedTripsAvg() {
	    return unmatchedTripsAvg;
	  }
				  
	  // Create timer to update averages every 30 seconds.
	  @PostConstruct
	  public void start() {
			try {
				agencyIds = getAgencyList();
				// Initialize maps
				for (String agencyId : agencyIds) {
					matchedTripAveragesByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					unmatchedTripAveragesByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					busesInServicePctByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					stopsMatchedByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					stopsUnmatchedByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					tripTotalsByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					tripScheduleRealtimeDiffByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					locationTotalsByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
					locationInvalidLatLonByAgency.put(agencyId, new int[ROLLING_AVERAGE_COUNT]);
				}
				if (_refreshInterval > 0) {
					_refreshTask = _scheduledExecutorService.scheduleAtFixedRate(new RefreshTask(), 0, _refreshInterval, TimeUnit.SECONDS);
				}
			} catch (Throwable t) {
				_log.error("exception initalizing: ", t, t);
			}
	  }

	  @PreDestroy
	  public void stop() {
	    if (_refreshTask != null) {
	      _refreshTask.cancel(true);
	      _refreshTask = null;
	    }
	    if (_scheduledExecutorService != null) {
	      _scheduledExecutorService.shutdown();
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
	    	int totalMatchedTripCt = 0;
	    	int totalUnmatchedTripCt = 0;
	    	int busesInServicePct = 0;
	    	Map<String, Integer> matchedByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> unmatchedByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> busesInServiceByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> currentTripTotalsByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> matchedStopsByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> unmatchedStopsByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> tripsDiffsByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> locsByAgency = new HashMap<String, Integer>();
	    	Map<String, Integer> locsInvalidByAgency = new HashMap<String, Integer>();
	    	// Get latest matched trip total by agency
	    	for (String agencyId : agencyIds) {
	    	  Integer matched = matchedByAgency.get(agencyId);
	    	  if (matched == null) matched = 0;
	    	  matched += getValidRealtimeTripIds(agencyId, null).size();
	    	  totalMatchedTripCt += matched;
	    	  matchedByAgency.put(agencyId, matched);
	    	}
	      // Get latest unmatched trip total by agency
        for (String agencyId : agencyIds) {
          Integer unmatched = unmatchedByAgency.get(agencyId);
          if (unmatched == null) unmatched = 0;
          unmatched += getUnmatchedTripIdCt(agencyId, null);
          totalUnmatchedTripCt += unmatched;
          unmatchedByAgency.put(agencyId, unmatched);
        }
        // Get latest buses in service percentage by agency
        for (String agencyId : agencyIds) {
          double scheduledTrips = getScheduledTrips(agencyId);
          double validRealtimeTrips = getValidRealtimeTripIds(agencyId, null).size();
          int percent = (int)(Math.round(scheduledTrips != 0 ? (validRealtimeTrips / scheduledTrips) * 100 : Integer.MAX_VALUE));
          busesInServiceByAgency.put(agencyId, percent);
        }
        // Get latest matched stops
        for (String agencyId : agencyIds) {
          int matched = getMatchedStopCt(agencyId, null);
          matchedStopsByAgency.put(agencyId, matched);
        }
        // Get latest unmatched stops
        for (String agencyId : agencyIds) {
          int unmatched = getUnmatchedStopCt(agencyId, null);
          unmatchedStopsByAgency.put(agencyId, unmatched);
        }
        // Get latest trip totals
        for (String agencyId : agencyIds) {
          int total = getTotalRecordCount(agencyId, null);
          currentTripTotalsByAgency.put(agencyId, total);
        }
        // Get latest trip schedule-realtime diffs
        for (String agencyId : agencyIds) {
          int scheduledTrips = getScheduledTrips(agencyId);
          int validRealtimeTrips = getValidRealtimeTripIds(agencyId, null).size();
          int diff = scheduledTrips - validRealtimeTrips;      
          tripsDiffsByAgency.put(agencyId, diff);
        }
        // Get latest location totals
        for (String agencyId : agencyIds) {
          int total = getLocationTotal(agencyId, null);
          locsByAgency.put(agencyId, total);
        }
        // Get latest location invalid lat-lon
        for (String agencyId : agencyIds) {
          int total = getInvalidLocation(agencyId, null);
          locsInvalidByAgency.put(agencyId, total);
        }
        
        int idx = currentTripCt < ROLLING_AVERAGE_COUNT ? currentTripCt++ : earliestTripIdx++;
		    updateTotals(matchedTripAveragesByAgency, matchedByAgency, idx);
		    updateTotals(unmatchedTripAveragesByAgency, unmatchedByAgency, idx);
		    updateTotals(busesInServicePctByAgency, busesInServiceByAgency, idx);
		    updateTotals(tripTotalsByAgency, currentTripTotalsByAgency, idx);
		    updateTotals(stopsMatchedByAgency, matchedStopsByAgency, idx);
		    updateTotals(stopsUnmatchedByAgency, unmatchedStopsByAgency, idx);
		    updateTotals(tripScheduleRealtimeDiffByAgency, tripsDiffsByAgency, idx);
		    updateTotals(locationTotalsByAgency, locsByAgency, idx);
		    updateTotals(locationInvalidLatLonByAgency, locsInvalidByAgency, idx);

		    
		    updateLongTermMatchedTrips(totalMatchedTripCt, idx);
		    updateLongTermUnmatchedTrips(totalUnmatchedTripCt, idx);
        earliestTripIdx %= ROLLING_AVERAGE_COUNT;
		    _log.debug("Averages updated: matched = " + matchedTripsAvg + ", unmatched = " + unmatchedTripsAvg );
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
