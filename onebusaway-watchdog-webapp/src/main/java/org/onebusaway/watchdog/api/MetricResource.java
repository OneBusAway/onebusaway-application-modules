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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.watchdog.model.Metric;
import org.onebusaway.watchdog.model.MetricConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class MetricResource {

  protected static Logger _log = LoggerFactory.getLogger(MetricResource.class);
  private ScheduledExecutorService _scheduledExecutorService;
  private ScheduledFuture<?> _refreshTask;
  private LongTermAverages _longTermAverages;
  private int _refreshInterval = 30;    // Interval in seconds for updating long term averages

  
  protected MetricConfiguration _configuration;
  protected ObjectMapper _mapper = new ObjectMapper();
  
  static protected int longTermMatchedTrips = 0;  // The current rolling average for matched trips
  static protected int longTermUnmatchedTrips = 0;
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

  @Autowired
  public void setLongTermAverages(
		  LongTermAverages longTermAverages) {
    _longTermAverages = longTermAverages;
  }

  @Autowired
  public void setScheduledExecutorService(
      ScheduledExecutorService scheduledExecutorService) {
    _scheduledExecutorService = scheduledExecutorService;
  }

  @Autowired
  public void setMetricConfiguration(MetricConfiguration mc) {
    _configuration = mc;
  }
  
  protected List<MonitoredDataSource> getDataSources() {
    return _configuration.getDataSources();
  }
  
  protected TransitDataService getTDS() {
    return _configuration.getTDS();
  }
  
  protected int getLongTermMatchedTrips() {
    return longTermMatchedTrips;
  }
	  
  protected int getLongTermUnmatchedTrips() {
    return longTermUnmatchedTrips;
  }
		  
  protected int getTotalRecordCount(String agencyId) throws Exception {
    int totalRecords = 0;

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      for (String mAgencyId : result.getAgencyIds()) {
        if (agencyId.equals(mAgencyId)) {
          totalRecords += result.getRecordsTotal();
        }
      }
    }
    return totalRecords;
  }
  
  protected int getScheduledTrips(String agencyId) {
    Set<TripDetailsBean> agencyTrips = new HashSet<TripDetailsBean>();
    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    List<CoordinateBounds> allBounds = getTDS().getAgencyIdsWithCoverageArea().get(agencyId);
    
    for (CoordinateBounds bounds : allBounds) {
      query.setBounds(bounds);
      query.setTime(System.currentTimeMillis());
      query.setMaxCount(Integer.MAX_VALUE);
      
      TripDetailsInclusionBean inclusion = query.getInclusion();
      inclusion.setIncludeTripBean(true);
      ListBean<TripDetailsBean> allTrips =  getTDS().getTripsForBounds(query);
      if (allTrips == null) {
        continue;
      }
  
      _log.debug("allTrips size=" + allTrips.getList().size());
  
      for (TripDetailsBean trip : allTrips.getList()) {
        if (trip.getTripId().startsWith(agencyId + "_")) {
          agencyTrips.add(trip);
        }
      }
    }
    return agencyTrips.size();
  }
  
  protected List<String> getValidRealtimeTripIds(String agencyId) {
    Set<String> tripIds = new HashSet<String>();

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      for (String tripId : result.getMatchedTripIds()) {
        if (agencyId.equals(AgencyAndIdLibrary.convertFromString(tripId).getAgencyId())) {
          tripIds.add(tripId);
        }
      }
    }
    List<String> prunedTripIds = new ArrayList<String>(tripIds.size());
    prunedTripIds.addAll(tripIds);
    return prunedTripIds;
  }

  protected int getLongTermDeltaMatchedTrips() {
	int totalMatchedTripIds = 0;
	for (MonitoredDataSource mds : getDataSources()) {
	  MonitoredResult result = mds.getMonitoredResult();
	  totalMatchedTripIds += result.getMatchedTripIds().size();
	}
	_log.info("long term matched delta, current = " + totalMatchedTripIds + ", average = " + _longTermAverages.getMatchedTripsAvg());;
	return totalMatchedTripIds - _longTermAverages.getMatchedTripsAvg();
  }
  
  protected int getLongTermDeltaUnmatchedTrips() {
	int totalUnmatchedTripIds = 0;
	for (MonitoredDataSource mds : getDataSources()) {
	  MonitoredResult result = mds.getMonitoredResult();
	  totalUnmatchedTripIds += result.getUnmatchedTripIds().size();
	}
	_log.info("long term unmatched delta, current = " + totalUnmatchedTripIds + ", average = " + _longTermAverages.getUnmatchedTripsAvg());;	  
	return totalUnmatchedTripIds - _longTermAverages.getUnmatchedTripsAvg();
  }
  
  protected String ok(String metricName, Object value) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setCurrentTimestamp(System.currentTimeMillis());
    metric.setMetricValue(value);
    metric.setResponse("SUCCESS");
    
    try {
      return _mapper.writeValueAsString(metric);
    } catch (IOException e) {
      _log.error("metric serialization failed:" + e);
      return "{response=\"ERROR\"}";
    }
  }
  
  protected String error(String metricName, Exception e) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setErrorMessage(e.toString());
    metric.setResponse("ERROR");
    try {
      return _mapper.writeValueAsString(metric);
    } catch (IOException ioe) {
      _log.error("metric serialization failed:" + ioe);
      return "{response=\"ERROR\"}";
    }
  }
 
  protected String error(String metricName, String errorMessage) {
    Metric metric = new Metric();
    metric.setMetricName(metricName);
    metric.setErrorMessage(errorMessage);
    metric.setResponse("ERROR");
    try {
      return _mapper.writeValueAsString(metric);
    } catch (IOException e) {
      _log.error("metric serialization failed:" + e);
      return "{response=\"ERROR\"}";
    }

  }
  
  protected void updateLongTermMatchedTrips(int currentMatchedTrips) {
    int idx = currentMatchedTripCt < ROLLING_AVERAGE_COUNT ? currentMatchedTripCt++ : earliestMatchedTripIdx++;
    recentMatchedTrips[idx] = currentMatchedTrips;
    earliestMatchedTripIdx %= ROLLING_AVERAGE_COUNT;

    int sum = 0;
    for (int i : recentMatchedTrips) {
      sum += i;
    }  
    longTermMatchedTrips = sum / currentMatchedTripCt;
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
    longTermUnmatchedTrips = sum / currentUnmatchedTripCt;
    return;
  }

}