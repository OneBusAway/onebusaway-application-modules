/**
 * Copyright (C) 2025 Aaron Brethorst <aaron@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.MetricsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.bundle.RealtimeSourceServiceImpl;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.beans.MetricsBeanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MetricsBeanServiceImpl implements MetricsBeanService {
  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private RealtimeSourceServiceImpl _sourceService;

  protected List<MonitoredDataSource> getDataSources() {
    List<MonitoredDataSource> dataSources = new ArrayList<>();
    for (GtfsRealtimeSource source : _sourceService.getSources()) {
      if (source instanceof MonitoredDataSource) {
        dataSources.add((MonitoredDataSource) source);
      }
    }
    return dataSources;
  }
  protected static Logger _log = LoggerFactory.getLogger(MetricsBeanServiceImpl.class);

  @Override
  public MetricsBean getMetrics() {
    MetricsBean bean = new MetricsBean();

    populateAgencyFields(bean);

    populateStopFields(bean);

    populateRealtimeTripFields(bean);

    populateTotalRecordsFields(bean);

    bean.setScheduledTripsCount(getScheduledTrips());

    return bean;
  }

  /**
   * Fills in all agency-related fields in the MetricsBean.
   * @param bean The MetricsBean object that is populated.
   */
  private void populateAgencyFields(MetricsBean bean) {
    List<AgencyWithCoverageBean> agencies = _transitDataService.getAgenciesWithCoverage();
    bean.setAgenciesWithCoverageCount(agencies.size());

    ArrayList<String> agencyIDs = new ArrayList<String>();
    for (AgencyWithCoverageBean a : agencies) {
      agencyIDs.add(a.getAgency().getId());
    }
    bean.setAgencyIDs(agencyIDs.toArray(String[]::new));
  }

  /**
   * Fills in all realtime trip-related fields in the MetricsBean.
   * @param bean The MetricsBean object that is populated.
   */
  private void populateRealtimeTripFields(MetricsBean bean) {
    bean.setRealtimeTripIDsUnmatched(getRealtimeTripIDsUnmatched());
    bean.setRealtimeTripCountsUnmatched(getUnmatchedTripCounts());
    bean.setRealtimeTripCountsMatched(getRealtimeTripCountsMatched());
  }

  /**
   * Retrieves a dictionary of agency IDs mapped to scheduled trips count.
   * @return The per-agency trip count.
   */
  private HashMap<String,Integer> getScheduledTrips() {
    HashMap<String,Integer> tripCountMap = new HashMap<String, Integer>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      tripCountMap.put(id, getScheduledTrips(id, null));
    }
    return tripCountMap;
  }

  /**
   * Retrieves the list of scheduled trips for the specified agencyId and optional routeId.
   *
   * Note: This code was ported over from onebusaway-watchdog-webapp.
   *
   * @param agencyId The ID of the agency. Required.
   * @param routeId The ID of the route. Optional.
   * @return The number of scheduled trips that match.
   */
  private int getScheduledTrips(String agencyId, String routeId) {
    Set<TripDetailsBean> agencyTrips = new HashSet<TripDetailsBean>();
    TripsForAgencyQueryBean query = new TripsForAgencyQueryBean();
    query.setAgencyId(agencyId);
    query.setMaxCount(Integer.MAX_VALUE);
    ListBean<TripDetailsBean> tripsForAgency = _transitDataService.getTripsForAgency(query);
    if (tripsForAgency == null) {
      return 0;
    }

    AgencyAndId routeAndId = new AgencyAndId(agencyId, routeId);
    for (TripDetailsBean trip : tripsForAgency.getList()) {
      // trip and tripId can be null for cancelled trips!
      if (trip != null && trip.getTripId() != null && trip.getTripId().startsWith(agencyId + "_")) {
        if (routeId == null || routeAndId.toString().equals(trip.getTrip().getRoute().getId())) {
          agencyTrips.add(trip);
        }
      }
    }
    _log.debug("scheduledTrips for (" + agencyId + ", " + routeId + "): " + agencyTrips.size() + " matched trips");
    return agencyTrips.size();
  }

  /**
   * Retrieves a dictionary of agency IDs mapped to the list of unmatched trip IDs.
   * @return The per-agency unmatched trip IDs list.
   */
  private HashMap<String, ArrayList<String>> getRealtimeTripIDsUnmatched() {
    HashMap<String, ArrayList<String>> unmatchedTrips = new HashMap<>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      unmatchedTrips.put(id, getUnmatchedTripIds(id, null));
    }
    return unmatchedTrips;
  }

  /**
   * Retrieves the list of unmatched trip IDs for the specified agencyId.
   *
   * Note: This code was ported over from onebusaway-watchdog-webapp.
   *
   * @param agencyId The ID of the agency. Required.
   * @return The list of unmatched trip IDs.
   */
  private ArrayList<String> getUnmatchedTripIds(String agencyId,String feedId) {
    try {
      ArrayList<String> unmatchedTripIds = new ArrayList<String>();
      List<MonitoredDataSource> dataSources = getDataSources();
      if (dataSources == null || dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return new ArrayList<>();
      }

      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null)
          continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              unmatchedTripIds.addAll(result.getUnmatchedTripIds());
            }
          }
        }
      }
      return unmatchedTripIds;
    } catch (Exception e) {
      _log.error("getUnmatchedTripIds broke", e);
      return new ArrayList<>();
    }
  }

  /**
   * Retrieves a dictionary of agency IDs mapped to the count of unmatched trip IDs.
   * @return The per-agency unmatched trip IDs count.
   */
  private HashMap<String, Integer> getUnmatchedTripCounts() {
    HashMap<String, Integer> unmatchedTripCounts = new HashMap<>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      unmatchedTripCounts.put(id, getUnmatchedTripIds(id, null).size());
    }
    return unmatchedTripCounts;
  }

  /**
   * Fills in all stop-related fields in the MetricsBean.
   * @param bean The MetricsBean object that is populated.
   */
  private void populateStopFields(MetricsBean bean) {
    // add unmatched stops
    bean.setStopIDsUnmatched(getUnmatchedStops());
    bean.setStopIDsUnmatchedCount(getUnmatchedStopIdsCount());
    // add matched stops
    bean.setStopIDsMatchedCount(getMatchedStopIdsCount());
  }
  /**
   * Fills in all total records-related fields in the MetricsBean.
   * @param bean The MetricsBean object that is populated.
   */
  private void populateTotalRecordsFields(MetricsBean bean) {
    bean.setRealtimeRecordsTotal(getTotalRecordsCounts());
  }

  /**
   * Retrieves a dictionary of agency IDs mapped to the total records count.
   * @return The per-agency total records count.
   */
  private HashMap<String, Integer> getTotalRecordsCounts() {
    HashMap<String, Integer> totalRecordsCounts = new HashMap<>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
        String agencyId = agency.getAgency().getId();
        int totalRecordsCount = getTotalRecordCount(agencyId, null);
        totalRecordsCounts.put(agencyId, totalRecordsCount);
    }
    return totalRecordsCounts;
  }

  /**
   * Retrieves the total record count for the specified agencyId and optional feedId.
   *
   * Note: This code was ported over from onebusaway-watchdog-webapp.
   *
   * @param agencyId The ID of the agency. Required.
   * @param feedId The ID of the feed. Optional.
   * @return The total record count.
   */
  private int getTotalRecordCount(String agencyId, String feedId) {
    int totalRecords = 0;

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      if (feedId == null || feedId.equals(mds.getFeedId())) {
        for (String mAgencyId : result.getAgencyIds()) {
          if (agencyId.equals(mAgencyId)) {
            totalRecords += result.getRecordsTotal();
          }
        }
      }
    }
    return totalRecords;
  }

  /**
   * Retrieves a dictionary of agency IDs mapped to unmatched stop IDs.
   * @return The per-agency unmatched stop IDs.
   */
  private HashMap<String, ArrayList<String>> getUnmatchedStops() {
    HashMap<String, ArrayList<String>> unmatchedStops = new HashMap<String, ArrayList<String>>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      unmatchedStops.put(id, getUnmatchedStopIds(id, null));
    }
    return unmatchedStops;
  }

  /**
   * Retrieves the list of unmatched stop IDs for the specified agencyId.
   *
   * Note: This code was ported over from onebusaway-watchdog-webapp.
   *
   * @param agencyId The ID of the agency. Required.
   * @return The list of unmatched stop IDs.
   */
  private ArrayList<String> getUnmatchedStopIds(String agencyId,String feedId) {
    try {
      ArrayList<String> unmatchedStopIds = new ArrayList<String>();
      List<MonitoredDataSource> dataSources = getDataSources();
      if (dataSources == null  || dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return new ArrayList<String>();
      }
      for (MonitoredDataSource mds : dataSources) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              unmatchedStopIds.addAll(result.getUnmatchedStopIds());
            }
          }
        }
      }
      return unmatchedStopIds;
    } catch (Exception e) {
      _log.error("getUnmatchedStopIds broke", e);
      return new ArrayList<String>();
    }
  }
  
  /**
   * Retrieves a dictionary of agency IDs mapped to the count of unmatched stop IDs.
   * @return The per-agency unmatched stop IDs count.
   */
  private HashMap<String, Integer> getUnmatchedStopIdsCount() {
    HashMap<String, Integer> unmatchedStopIdsCountMap = new HashMap<String, Integer>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      unmatchedStopIdsCountMap.put(id, getUnmatchedStopsCount(id, null));
    }
    return unmatchedStopIdsCountMap;
  }

  /**
   * Retrieves the number of unmatched stops for the specified agencyId.
   *
   * Note: This code was ported over from onebusaway-watchdog-webapp.
   *
   * @param agencyId The ID of the agency. Required.
   * @param feedId The ID of the feed. Optional.
   * @return The number of unmatched stops.
   */
  private int getUnmatchedStopsCount(String agencyId, String feedId) {
    try {
      int unmatchedStops = 0;
      List<MonitoredDataSource> dataSources = getDataSources();
      if (dataSources == null || dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return 0;
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            _log.debug("examining agency=" + mAgencyId + " with unmatched stops=" + result.getUnmatchedStopIds().size());
            if (agencyId.equals(mAgencyId)) {
              unmatchedStops += result.getUnmatchedStopIds().size();
            }
          }
        }
      }
      return unmatchedStops;
    } catch (Exception e) {
      _log.error("getUnmatchedStops broke", e);
      return 0;
    }
  }

  /**
   * Retrieves the count of matched trips on a per-agency basis.
   * @return A map of agency IDs to matched trip counts.
   */
  private HashMap<String, Integer> getRealtimeTripCountsMatched() {
    HashMap<String, Integer> matchedTripCounts = new HashMap<>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String agencyId = agency.getAgency().getId();
      int matchedTripCount = getValidRealtimeTripIds(agencyId, null).size();
      matchedTripCounts.put(agencyId, matchedTripCount);
    }
    return matchedTripCounts;
  }

  /**
   * Retrieves the list of valid real-time trip IDs for the specified agencyId and optional feedId.
   *
   * Note: This code was ported over from onebusaway-watchdog-webapp.
   * 
   * @param agencyId The ID of the agency. Required.
   * @param feedId The ID of the feed. Optional.
   * @return The list of valid real-time trip IDs.
   */
  private List<String> getValidRealtimeTripIds(String agencyId, String feedId) {
    Set<String> tripIds = new HashSet<>();

    for (MonitoredDataSource mds : getDataSources()) {
      MonitoredResult result = mds.getMonitoredResult();
      if (result == null) continue;
      if ((feedId == null || feedId.equals(mds.getFeedId())) && agencyId != null) {
        for (String tripId : result.getMatchedTripIds()) {
          if (tripId != null) {
            AgencyAndId matchedTripId = AgencyAndIdLibrary.convertFromString(tripId);
            if (matchedTripId != null && agencyId.equals(matchedTripId.getAgencyId())) {
              tripIds.add(tripId);
            }
          }
        }
        for (String tripId : result.getAddedTripIds()) {
          if (tripId != null) {
            AgencyAndId addedTripId = AgencyAndIdLibrary.convertFromString(tripId);
            if (addedTripId != null && agencyId.equals(addedTripId.getAgencyId())) {
              tripIds.add(tripId);
            }
          }
        }
        for (String tripId : result.getDuplicatedTripIds()) {
          if (tripId != null) {
            AgencyAndId duplicatedTripId = AgencyAndIdLibrary.convertFromString(tripId);
            if (duplicatedTripId != null && agencyId.equals(duplicatedTripId.getAgencyId())) {
              tripIds.add(tripId);
            }
          }
        }
      }
    }
    return new ArrayList<>(tripIds);
  }
  /**
   * Retrieves a dictionary of agency IDs mapped to the count of matched stop IDs.
   * @return The per-agency matched stop IDs count.
   */
  private HashMap<String, Integer> getMatchedStopIdsCount() {
    HashMap<String, Integer> matchedStopIdsCountMap = new HashMap<String, Integer>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      matchedStopIdsCountMap.put(id, getMatchedStopIdsList(id, null).size());
    }
    return matchedStopIdsCountMap;
  }

  /**
   * Retrieves the List of matched stops for the specified agencyId.
   *
   * @param agencyId The ID of the agency. Required.
   * @param feedId The ID of the feed. Optional.
   * @return The list of matched stops.
   */
  private ArrayList<String> getMatchedStopIdsList(String agencyId, String feedId) {
    ArrayList<String> matchedStopIds = new ArrayList<String>();
    List<MonitoredDataSource> dataSources = getDataSources();
    try {
      if (dataSources == null || dataSources.isEmpty()) {
        _log.error("no configured data sources");
        return new ArrayList<String>();
      }
      
      for (MonitoredDataSource mds : getDataSources()) {
        MonitoredResult result = mds.getMonitoredResult();
        if (result == null) continue;
        if (feedId == null || feedId.equals(mds.getFeedId())) {
          for (String mAgencyId : result.getAgencyIds()) {
            if (agencyId.equals(mAgencyId)) {
              matchedStopIds.addAll(result.getMatchedStopIds());
            }
          }
        }                
      }
      return matchedStopIds;
    } catch (Exception e) {
      _log.error("getMatchedStopCount broke", e);
      return new ArrayList<String>();
    }
  }
}
