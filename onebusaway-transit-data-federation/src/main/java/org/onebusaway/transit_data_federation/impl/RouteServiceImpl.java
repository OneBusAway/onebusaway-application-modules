package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.exceptions.InternalErrorServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class RouteServiceImpl implements RouteService {

  @Autowired
  private GtfsRelationalDao _gtfsDao;

  @Autowired
  private TransitDataFederationDao _whereDao;

  private TransitGraphDao _transitGraphDao;

  private BlockIndexService _blockIndexService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

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
  @Override
  public AgencyAndId getRouteCollectionIdForRoute(
      @CacheableArgument(keyProperty = "id") Route route) {
    RouteCollection routeCollection = _whereDao.getRouteCollectionForRoute(route);
    if (routeCollection == null)
      return null;
    return routeCollection.getId();
  }

  @Override
  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId) {

    StopEntry stopEntry = _transitGraphDao.getStopEntryForId(stopId);
    if (stopEntry == null)
      throw new InternalErrorServiceException("no such stop: id=" + stopId);

    Set<AgencyAndId> routeCollectionIds = new HashSet<AgencyAndId>();

    List<BlockStopTimeIndex> indices = _blockIndexService.getStopTimeIndicesForStop(stopEntry);

    for (BlockStopTimeIndex blockStopTimeIndex : indices) {
      BlockIndex blockIndex = blockStopTimeIndex.getBlockIndex();
      for (BlockConfigurationEntry blockConfig : blockIndex.getBlocks()) {
        for (BlockTripEntry blockTrip : blockConfig.getTrips()) {
          TripEntry trip = blockTrip.getTrip();
          routeCollectionIds.add(trip.getRouteCollectionId());
        }
      }
    }

    return routeCollectionIds;
  }
}
