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
package org.onebusaway.transit_data_federation.impl.beans;

import java.util.*;

import org.onebusaway.collections.Counter;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.NearbyStopsBeanService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopWithArrivalsAndDeparturesBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class StopWithArrivalsAndDeparturesBeanServiceImpl implements
    StopWithArrivalsAndDeparturesBeanService {

  @Autowired
  private StopBeanService _stopBeanService;

  @Autowired
  private ArrivalsAndDeparturesBeanService _arrivalsAndDeparturesBeanService;

  @Autowired
  private NearbyStopsBeanService _nearbyStopsBeanService;

  @Autowired
  private AgencyService _agencyService;

  @Autowired
  private ServiceAlertsBeanService _serviceAlertsBeanService;

  public StopWithArrivalsAndDeparturesBean getArrivalsAndDeparturesByStopId(
      AgencyAndId id, ArrivalsAndDeparturesQueryBean query) {

    StopBean stop = _stopBeanService.getStopForId(id, new ServiceDate(new Date(query.getTime())));
    if (stop == null)
      return null;

    List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
        id, query);

    List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(
        stop, 100);
    List<StopBean> nearbyStops = new ArrayList<StopBean>();
    for (AgencyAndId nearbyStopId : nearbyStopIds)
      nearbyStops.add(_stopBeanService.getStopForId(nearbyStopId, null));

    List<ServiceAlertBean> situations = _serviceAlertsBeanService.getServiceAlertsForStopId(
        query.getTime(), id);

    return new StopWithArrivalsAndDeparturesBean(stop, arrivalsAndDepartures,
        nearbyStops, situations);
  }

  public StopsWithArrivalsAndDeparturesBean getArrivalsAndDeparturesForStopIds(
      Set<AgencyAndId> ids, ArrivalsAndDeparturesQueryBean query)
      throws NoSuchStopServiceException {

    List<StopBean> stops = new ArrayList<StopBean>();
    List<ArrivalAndDepartureBean> allArrivalsAndDepartures = new ArrayList<ArrivalAndDepartureBean>();
    Set<AgencyAndId> allNearbyStopIds = new HashSet<AgencyAndId>();
    Map<String, ServiceAlertBean> situationsById = new HashMap<String, ServiceAlertBean>();
    Counter<TimeZone> timeZones = new Counter<TimeZone>();
    boolean limitExceeded = false;

    for (AgencyAndId id : ids) {

      StopBean stopBean = _stopBeanService.getStopForId(id, null);
      stops.add(stopBean);

      List<ArrivalAndDepartureBean> arrivalsAndDepartures = _arrivalsAndDeparturesBeanService.getArrivalsAndDeparturesByStopId(
          id, query);
      if (!arrivalsAndDepartures.isEmpty()) {
        // we only add stopBean if it actually has results
        stops.add(stopBean);
        allArrivalsAndDepartures.addAll(arrivalsAndDepartures);
      }

      List<AgencyAndId> nearbyStopIds = _nearbyStopsBeanService.getNearbyStops(
          stopBean, 100, query.getInstanceFilterChain());
      // these stops need a distanceFromQuery as well -- its added below
      allNearbyStopIds.addAll(nearbyStopIds);

      TimeZone timeZone = _agencyService.getTimeZoneForAgencyId(id.getAgencyId());
      timeZones.increment(timeZone);

      List<ServiceAlertBean> situations = _serviceAlertsBeanService.getServiceAlertsForStopId(
          query.getTime(), id);
      for (ServiceAlertBean situation : situations)
        situationsById.put(situation.getId(), situation);
    }

    if (!query.getIncludeInputIdsInNearby()) {
      allNearbyStopIds.removeAll(ids);
    }
    List<StopBean> nearbyStops = new ArrayList<StopBean>();
    CoordinateBounds bounds = query.getBounds();
    CoordinatePoint center = null;
    if (bounds != null) {
      center = SphericalGeometryLibrary.getCenterOfBounds(bounds);
    }
    for (AgencyAndId id : allNearbyStopIds) {
      StopBean stop = _stopBeanService.getStopForId(id, null);
      if (center != null) {
        // if bounds are present calculate distance of this stop from center
        double distance = SphericalGeometryLibrary.distance(center.getLat(),
                center.getLon(), stop.getLat(), stop.getLon());
        stop.setDistanceAwayFromQuery(distance);
      }
      nearbyStops.add(stop);
    }
    // sort the collection so we can trim the furthest
    Collections.sort(nearbyStops, new StopDistanceComparator());
    while (nearbyStops.size() > query.getMaxCount()) {
      nearbyStops.remove(nearbyStops.size() - 1);
      limitExceeded = true;
    }

    TimeZone timeZone = timeZones.getMax();
    if (timeZone == null)
      timeZone = TimeZone.getDefault();

    StopsWithArrivalsAndDeparturesBean result = new StopsWithArrivalsAndDeparturesBean();
    // trim stops
    while (stops.size() > query.getMaxCount()) {
      stops.remove(stops.size() - 1);
      limitExceeded = true;
    }
    // trim arrivals as well
    while (allArrivalsAndDepartures.size() > query.getMaxCount()) {
      allArrivalsAndDepartures.remove(allArrivalsAndDepartures.size() - 1);
      limitExceeded = true;
    }
    result.setStops(stops);
    result.setArrivalsAndDepartures(allArrivalsAndDepartures);
    result.setNearbyStops(nearbyStops);
    result.setSituations(new ArrayList<ServiceAlertBean>(situationsById.values()));
    result.setTimeZone(timeZone.getID());
    result.setLimitExceeded(limitExceeded);
    return result;
  }

  private static class StopDistanceComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      StopBean s1 = (StopBean)o1;
      StopBean s2 = (StopBean)o2;
      if (s1.getDistanceAwayFromQuery() == s2.getDistanceAwayFromQuery())
        return 0;
      if (s1.getDistanceAwayFromQuery() == null)
        return -1;
      if (s2.getDistanceAwayFromQuery() == null)
        return 1;
      try {
        return Double.compare(s1.getDistanceAwayFromQuery(), s2.getDistanceAwayFromQuery());
      } catch (NullPointerException npe) {
        return 0;
      }
    }
  }

}
