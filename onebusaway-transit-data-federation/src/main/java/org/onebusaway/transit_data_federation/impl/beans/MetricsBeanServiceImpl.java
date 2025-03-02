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
import org.onebusaway.transit_data_federation.services.beans.MetricsBeanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class MetricsBeanServiceImpl implements MetricsBeanService {

  @Autowired
  private TransitDataService _transitDataService;

  protected static Logger _log = LoggerFactory.getLogger(MetricsBeanServiceImpl.class);

  @Override
  public MetricsBean getMetrics() {
    MetricsBean bean = new MetricsBean();
    bean.setAgenciesWithCoverageCount(_transitDataService.getAgenciesWithCoverage().size());
    bean.setScheduledTripsCount(getScheduledTrips());
    return bean;
  }

  private HashMap<String,Integer> getScheduledTrips() {
    HashMap<String,Integer> tripCountMap = new HashMap<String, Integer>();
    for (AgencyWithCoverageBean agency : _transitDataService.getAgenciesWithCoverage()) {
      String id = agency.getAgency().getId();
      tripCountMap.put(id, getScheduledTrips(id, null));
    }
    return tripCountMap;
  }

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
}
