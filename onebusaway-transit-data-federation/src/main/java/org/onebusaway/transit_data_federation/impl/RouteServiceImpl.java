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

import java.util.*;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.exceptions.InternalErrorServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.AgencyServiceInterval;
import org.onebusaway.gtfs.model.calendar.ServiceInterval;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.blocks.FrequencyBlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class RouteServiceImpl implements RouteService {

  private TransitGraphDao _transitGraphDao;

  private BlockIndexService _blockIndexService;

  private CalendarService _calendarService;

  private ServiceIntervalHelper _helper = new ServiceIntervalHelper();

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) { _calendarService = calendarService; }

  @Override
  @Cacheable
  public Collection<AgencyAndId> getStopsForRouteCollection(AgencyAndId id) {
    return getStopsForRouteCollectionForServiceInterval(id, null);
  }

  @Override
  @Cacheable
  public Collection<AgencyAndId> getStopsForRouteCollectionForServiceInterval(AgencyAndId id, AgencyServiceInterval serviceInterval) {
    Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();
    RouteCollectionEntry routeCollectionEntry = _transitGraphDao.getRouteCollectionForId(id);

    for (RouteEntry route : routeCollectionEntry.getChildren()) {
      List<TripEntry> trips = route.getTrips();
      for (TripEntry trip : trips) {
        ServiceInterval tripServiceInterval = _helper.getServiceIntervalForTrip(trip);
        if (serviceInterval != null) {
          boolean isActiveTrip;
          if (_blockIndexService.isDynamicTrip(trip)) {
            isActiveTrip = _helper.isServiceIntervalActiveInRange(trip.getServiceId(), tripServiceInterval, serviceInterval);
          } else {
            isActiveTrip = _calendarService.isLocalizedServiceIdActiveInRange(trip.getServiceId(),
                    tripServiceInterval,
                    serviceInterval);
          }
          if (!isActiveTrip) continue;//skip this trip if not active
        }

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
    // this API is independent of service interval
    return getRouteCollectionIdsForStopForServiceDate(stopId, null);
  }

  @Override
  @Cacheable
  /*
  * serviceInterval can be null here.
   */
  public Set<AgencyAndId> getRouteCollectionIdsForStopForServiceDate(AgencyAndId stopId, AgencyServiceInterval serviceInterval) {
    StopEntry stopEntry = _transitGraphDao.getStopEntryForId(stopId);
    if (stopEntry == null)
      throw new InternalErrorServiceException("no such stop: id=" + stopId);

    Set<AgencyAndId> routeCollectionIds = new HashSet<AgencyAndId>();

    List<BlockStopTimeIndex> indices = _blockIndexService.getStopTimeIndicesForStop(stopEntry);

    for (BlockStopTimeIndex blockStopTimeIndex : indices) {
      for (BlockTripEntry blockTrip : blockStopTimeIndex.getTrips()) {
        TripEntry trip = blockTrip.getTrip();
        AgencyAndId routeCollectionAgencyAndId = trip.getRouteCollection().getId();
        // don't bother evaluating if this route is already in list
        if (!routeCollectionIds.contains(routeCollectionAgencyAndId)) {
            boolean isActiveTrip = false;
            if (serviceInterval != null) {
              ServiceInterval stopServiceInterval = _helper.getServiceIntervalForTrip(trip, stopEntry);
              if (_blockIndexService.isDynamicTrip(trip)) {
                isActiveTrip = _helper.isServiceIntervalActiveInRange(trip.getServiceId(), stopServiceInterval, serviceInterval);
              } else {
                isActiveTrip = _calendarService.isLocalizedServiceIdActiveInRange(trip.getServiceId(),
                        stopServiceInterval,
                        serviceInterval);
              }
              if (!isActiveTrip) continue; //skip this trip if not active
            }
          routeCollectionIds.add(routeCollectionAgencyAndId);
        }
      }
    }

    List<FrequencyBlockStopTimeIndex> frequencyIndices = _blockIndexService.getFrequencyStopTimeIndicesForStop(stopEntry);

    for (FrequencyBlockStopTimeIndex blockStopTimeIndex : frequencyIndices) {
      for (BlockTripEntry blockTrip : blockStopTimeIndex.getTrips()) {
        TripEntry trip = blockTrip.getTrip();
        if (serviceInterval != null) {
          ServiceInterval stopServiceInterval = _helper.getServiceIntervalForTrip(trip, stopEntry);
          boolean isActiveTrip;
          if (_blockIndexService.isDynamicTrip(trip)) {
            isActiveTrip = _helper.isServiceIntervalActiveInRange(trip.getServiceId(), stopServiceInterval, serviceInterval);
          } else {
            isActiveTrip = _calendarService.isLocalizedServiceIdActiveInRange(trip.getServiceId(),
                    stopServiceInterval,
                    serviceInterval);
          }
          if (!isActiveTrip) continue;//skip this trip if not active
        }

        routeCollectionIds.add(trip.getRouteCollection().getId());
      }
    }

    return routeCollectionIds;
  }
}
