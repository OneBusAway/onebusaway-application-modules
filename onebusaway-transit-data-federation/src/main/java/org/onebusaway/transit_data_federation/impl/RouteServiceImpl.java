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
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
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
      for( BlockTripEntry blockTrip : blockStopTimeIndex.getTrips() ) {
        TripEntry trip = blockTrip.getTrip();
        routeCollectionIds.add(trip.getRouteCollectionId());
      }
    }

    List<FrequencyBlockStopTimeIndex> frequencyIndices = _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry);

    for (FrequencyBlockStopTimeIndex blockStopTimeIndex : frequencyIndices) {
      for (BlockTripEntry blockTrip : blockStopTimeIndex.getTrips()) {
        TripEntry trip = blockTrip.getTrip();
        routeCollectionIds.add(trip.getRouteCollectionId());
      }
    }

    return routeCollectionIds;
  }
}
