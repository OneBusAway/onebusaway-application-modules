/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.alerts.impl.ServiceAlertLibrary;
import org.onebusaway.transit_data_federation.services.ConsolidatedStopsService;
import org.onebusaway.alerts.service.ServiceAlerts.Id;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GtfsRealtimeEntitySource {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeEntitySource.class);

  private TransitGraphDao _transitGraphDao;

  private ConsolidatedStopsService _consolidatedStopsService;

  private List<String> _agencyIds;

  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  public void setConsolidatedStopService(ConsolidatedStopsService service) { _consolidatedStopsService = service; }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds = agencyIds;
  }

  /**
   * Given a route id without an agency prefix, we attempt to find a
   * {@link RouteEntry} with the specified id by cycling through the set of
   * agency ids specified in {@link #setAgencyIds(List)}. If a
   * {@link RouteEntry} is found, we return the id of the parent
   * {@link RouteCollectionEntry} as the matching id. This is to deal with the
   * fact that while GTFS deals with underlying routes, internally OneBusAway
   * mostly deals with RouteCollections.
   * 
   * If no route is found in the {@link TransitGraphDao} with the specified id
   * for any of the configured agencies, a {@link Id} will be constructed with
   * the first agency id from the agency list.
   * 
   * @param routeId
   * @return an Id for {@link RouteCollectionEntry} with a matching
   *         {@link RouteEntry} id
   */
  public Id getRouteId(String routeId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, routeId);
      RouteEntry route = _transitGraphDao.getRouteForId(id);
      if (route != null)
        return ServiceAlertLibrary.id(route.getParent().getId());
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(routeId);
      RouteEntry route = _transitGraphDao.getRouteForId(id);
      if (route != null)
        return ServiceAlertLibrary.id(route.getParent().getId());
    } catch (IllegalArgumentException ex) {

    }

    AgencyAndId id = new AgencyAndId(_agencyIds.get(0), routeId);
    _log.warn("route not found with id \"{}\", defaulting to {}", routeId, id);
    return ServiceAlertLibrary.id(id);
  }

  public Id getTripId(String tripId) {

    TripEntry trip = getTrip(tripId);
    if (trip != null)
      return ServiceAlertLibrary.id(trip.getId());

    _log.warn("trip not found with id \"{}\"", tripId);

    AgencyAndId id = new AgencyAndId(_agencyIds.get(0), tripId);
    return ServiceAlertLibrary.id(id);
  }

  public TripEntry getTrip(String tripId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, tripId);
      TripEntry trip = _transitGraphDao.getTripEntryForId(id);
      if (trip != null)
        return trip;
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(tripId);
      TripEntry trip = _transitGraphDao.getTripEntryForId(id);
      if (trip != null)
        return trip;
    } catch (IllegalArgumentException ex) {

    }
    return null;
  }

  public Id getStopId(String stopId) {

    for (String agencyId : _agencyIds) {
      /*
       * even though the entity may have give us an agency_id
       * routes, stops, and trips may each belong to separate
       * agencies, so we try all agencies
       */
      AgencyAndId id = new AgencyAndId(agencyId, stopId);
      StopEntry stop = _transitGraphDao.getStopEntryForId(id);
      if (stop != null)
        return ServiceAlertLibrary.id(id);
    }

    try {
      /*
       * here we see if we already have an agencyAndId encoded
       * as a string
       */
      AgencyAndId id = AgencyAndId.convertFromString(stopId);
      StopEntry stop = _transitGraphDao.getStopEntryForId(id);
      if (stop != null)
        return ServiceAlertLibrary.id(id);
    } catch (IllegalArgumentException ex) {

    }

    /*
     * if we made it here the stop was either consolidated or
     * doesn't exist in the GTFS due to a data mismatch
     */
    if (_consolidatedStopsService != null) {
      if (stopId.indexOf('_') > 0) {
        AgencyAndId consolidatedId = _consolidatedStopsService.getConsolidatedStopIdForHiddenStopId(AgencyAndId.convertFromString(stopId));
        if (consolidatedId != null) {
          return ServiceAlertLibrary.id(consolidatedId);
        }
      } else {
        for (String agencyId : _agencyIds) {
          AgencyAndId consolidatedId = _consolidatedStopsService.getConsolidatedStopIdForHiddenStopId(new AgencyAndId(agencyId, stopId));
          if (consolidatedId != null) {
            return ServiceAlertLibrary.id(consolidatedId);
          }
        }
      }
    }

    _log.warn("alert stop not found with id \"{}\"", stopId);

    AgencyAndId id = new AgencyAndId(_agencyIds.get(0), stopId);
    return ServiceAlertLibrary.id(id);
  }
}
