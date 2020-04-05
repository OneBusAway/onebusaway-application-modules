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
package org.onebusaway.gtfs_realtime.archiver.listener;

import java.util.List;

import org.onebusaway.alerts.service.ServiceAlerts;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.alerts.impl.ServiceAlertLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GtfsRealtimeEntitySource {

  private static final Logger _log = LoggerFactory.getLogger(
      GtfsRealtimeEntitySource.class);

  private TransitGraphDao _transitGraphDao;

  private List<String> _agencyIds;

  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  public List<String> getAgencyIds() {
    return _agencyIds;
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds = agencyIds;
  }
  /**
   * This class was copied from org/onebusaway/transit_data_federation/impl
   * /realtime/gtfs_realtime/GtfsRealtimeEntitySource.java. It was modified
   * slightly, primarily the return values changed to AgencyAndId ids. The
   * documentation is from the original class.
   */

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
   * for any of the configured agencies, a {@link ServiceAlerts.Id} will be constructed with
   * the first agency id from the agency list.
   * 
   * @param routeId
   * @return an Id for {@link RouteCollectionEntry} with a matching
   *         {@link RouteEntry} id
   */
  public AgencyAndId getRouteId(String routeId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, routeId);
      RouteEntry route = _transitGraphDao.getRouteForId(id);
      if (route != null)
        return id;
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(routeId);
      RouteEntry route = _transitGraphDao.getRouteForId(id);
      if (route != null)
        return id;
    } catch (IllegalArgumentException ex) {

    }

    _log.debug("route not found with id \"{}\"", routeId);

    return null; // If not found, just return null.
  }

  public ServiceAlerts.Id getTripId(String tripId) {

    TripEntry trip = getTrip(tripId);
    if (trip != null)
      return ServiceAlertLibrary.id(trip.getId());

    _log.debug("trip not found with id \"{}\"", tripId);

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

  public AgencyAndId getStopId(String stopId) {

    for (String agencyId : _agencyIds) {
      AgencyAndId id = new AgencyAndId(agencyId, stopId);
      StopEntry stop = _transitGraphDao.getStopEntryForId(id);
      if (stop != null)
        return id;
    }

    try {
      AgencyAndId id = AgencyAndId.convertFromString(stopId);
      StopEntry stop = _transitGraphDao.getStopEntryForId(id);
      if (stop != null)
        return id;
    } catch (IllegalArgumentException ex) {

    }

    _log.debug("stop not found with id \"{}\"", stopId);

    return null; // If not found, just return null.
  }
}
