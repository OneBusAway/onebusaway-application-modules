/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.exceptions.InternalErrorServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class RouteServiceImpl implements RouteService {

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

  @Override
  @Cacheable
  public Collection<AgencyAndId> getStopsForRouteCollection(AgencyAndId id) {

    Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();
    RouteCollectionEntry routeCollectionEntry = _transitGraphDao.getRouteCollectionForId(id);

    for (RouteEntry route : routeCollectionEntry.getChildren()) {
      List<TripEntry> trips = route.getTrips();
      for (TripEntry trip : trips) {
        List<StopTimeEntry> stopTimes = trip.getStopTimes();
        for (StopTimeEntry stopTime : stopTimes)
          stopIds.add(stopTime.getStop().getId());
      }
    }

    return new ArrayList<AgencyAndId>(stopIds);
  }

  @Override
  @Cacheable
  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId) {

    StopEntry stopEntry = _transitGraphDao.getStopEntryForId(stopId);
    if (stopEntry == null)
      throw new InternalErrorServiceException("no such stop: id=" + stopId);

    Set<AgencyAndId> routeCollectionIds = new HashSet<AgencyAndId>();

    List<BlockStopTimeIndex> indices = _blockIndexService.getStopTimeIndicesForStop(stopEntry);

    for (BlockStopTimeIndex blockStopTimeIndex : indices) {
      for (BlockTripEntry blockTrip : blockStopTimeIndex.getTrips()) {
        TripEntry trip = blockTrip.getTrip();
        routeCollectionIds.add(trip.getRouteCollection().getId());
      }
    }

    List<FrequencyBlockStopTimeIndex> frequencyIndices = _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry);

    for (FrequencyBlockStopTimeIndex blockStopTimeIndex : frequencyIndices) {
      for (BlockTripEntry blockTrip : blockStopTimeIndex.getTrips()) {
        TripEntry trip = blockTrip.getTrip();
        routeCollectionIds.add(trip.getRouteCollection().getId());
      }
    }

    return routeCollectionIds;
  }
}
