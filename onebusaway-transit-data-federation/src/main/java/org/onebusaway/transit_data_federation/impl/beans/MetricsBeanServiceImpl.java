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
   * Fills in all stop-related fields in the MetricsBean.
   * @param bean The MetricsBean object that is populated.
   */
  private void populateStopFields(MetricsBean bean) {
    // add unmatched stops
    bean.setStopIDsUnmatched(getUnmatchedStops());
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
}
