package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
class RouteServiceImpl implements RouteService {

  @Autowired
  private GtfsRelationalDao _gtfsDao;

  @Autowired
  private TransitDataFederationDao _whereDao;

  @Cacheable
  @Transactional
  public Collection<AgencyAndId> getStopsForRouteCollection(AgencyAndId id) {
    Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();
    RouteCollection routeCollection = _whereDao.getRouteCollectionForId(id);
    for (Route route : routeCollection.getRoutes()) {
      List<Trip> trips = _gtfsDao.getTripsForRoute(route);
      for (Trip trip : trips) {
        List<StopTime> stopTimes = _gtfsDao.getStopTimesForTrip(trip);
        for (StopTime stopTime : stopTimes)
          stopIds.add(stopTime.getStop().getId());
      }
    }
    return new ArrayList<AgencyAndId>(stopIds);
  }

  @Cacheable
  public AgencyAndId getRouteCollectionIdForRoute(Route route) {
    RouteCollection routeCollection = _whereDao.getRouteCollectionsForRoute(route);
    if (routeCollection == null)
      return null;
    return routeCollection.getId();
  }
}
