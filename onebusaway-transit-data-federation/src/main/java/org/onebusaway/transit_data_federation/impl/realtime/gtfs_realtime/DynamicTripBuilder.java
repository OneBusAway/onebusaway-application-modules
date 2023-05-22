/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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


import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.realtime.api.EVehicleType;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.transit_data_federation.services.transit_graph.dynamic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.onebusaway.geospatial.services.SphericalGeometryLibrary.distance;

/**
 * Inspired by the transit-data-federation-builder, this is the entry point
 * into how dynamic trips are generated.
 */
public class DynamicTripBuilder {

  private static Logger _log = LoggerFactory.getLogger(DynamicTripBuilder.class);

  private TransitGraphDao _graph;
  public void setTransitGraphDao(TransitGraphDao dao) {
    _graph = dao;
  }

  private Map<String, DynamicRouteEntry> _routeCache = new HashMap<>();
  public BlockDescriptor createBlockDescriptor(AddedTripInfo addedTripInfo) {
    // from the addedTripInfo generate the trips and stops, and return in the block descriptor
    BlockDescriptor dynamicBd = new BlockDescriptor();
    dynamicBd.setScheduleRelationship(BlockDescriptor.ScheduleRelationship.ADDED);
    dynamicBd.setBlockInstance(createBlockInstance(addedTripInfo));
    dynamicBd.setStartTime(addedTripInfo.getTripStartTime());
    dynamicBd.setStartDate(new ServiceDate(new Date(addedTripInfo.getServiceDate())));
    return dynamicBd;
  }

  private BlockInstance createBlockInstance(AddedTripInfo addedTripInfo) {
    return new BlockInstance(createBlockConfiguration(addedTripInfo),
            addedTripInfo.getServiceDate());
  }

  private BlockConfigurationEntry createBlockConfiguration(AddedTripInfo addedTripInfo) {
    DynamicBlockConfigurationEntryImpl.Builder configBuilder = DynamicBlockConfigurationEntryImpl.builder();
    BlockEntry blockEntry = createBlockEntry(addedTripInfo);
    configBuilder.setBlock(blockEntry);
    configBuilder.setServiceIds(createServiceIdActivation(addedTripInfo));
    configBuilder.setTrips(new ArrayList<>());
    configBuilder.getTrips().add(createTrip(addedTripInfo, blockEntry));
    DynamicBlockConfigurationEntryImpl config = new DynamicBlockConfigurationEntryImpl(configBuilder);
    blockEntry.getConfigurations().add(config);
    return config;
  }

  private TripEntry createTrip(AddedTripInfo addedTripInfo, BlockEntry block) {
    DynamicTripEntryImpl trip = new DynamicTripEntryImpl();
    trip.setId(new AgencyAndId(addedTripInfo.getAgencyId(), addedTripInfo.getTripId()));
    trip.setRoute(createRoute(addedTripInfo));
    trip.setDirectionId(addedTripInfo.getDirectionId());
    trip.setBlock((DynamicBlockEntry) block);
    trip.setServiceId(createLocalizedServiceId(addedTripInfo));
    // todo we don't have shape -- would be nice to either copy from
    // route or generate one based on stops
    // todo Merha
    trip.setStopTimes(createStopTimes(addedTripInfo, trip));
    trip.setTotalTripDistance(calculateTripDistance(trip));
    return trip;
  }

  private List<StopTimeEntry> createStopTimes(AddedTripInfo addedTripInfo, DynamicTripEntryImpl trip) {
    List<StopTimeEntry> stops = new ArrayList<>();
    int sequence = 0;
    for (AddedStopInfo stopInfo : addedTripInfo.getStops()) {
      StopEntry stop = findStop(addedTripInfo.getAgencyId(), stopInfo.getStopId());
      DynamicStopTimeEntryImpl stopTime = new DynamicStopTimeEntryImpl();
      stopTime.setStop(copyFromStop(stop));
      stopTime.setArrivalTime(toSecondsInDay(stopInfo.getArrivalTime(), addedTripInfo.getServiceDate()));
      stopTime.setDepartureTime(toSecondsInDay(stopInfo.getDepartureTime(), addedTripInfo.getServiceDate()));
      stopTime.setSequence(sequence);
      stopTime.setTrip(trip);
      if (stopTime.getArrivalTime() < 1 && stopTime.getDepartureTime() < 1) {
        _log.error("invalid stoptime -- no data: " + stopInfo);
        continue;
      }
      sequence++;
      stops.add(stopTime);
    }
    return stops;
  }

  private int toSecondsInDay(long time, long serviceDate) {
    return Math.toIntExact((time - serviceDate) / 1000);
  }

  private DynamicStopEntryImpl copyFromStop(StopEntry staticStop) {
    return new DynamicStopEntryImpl(staticStop.getId(),
            staticStop.getStopLat(), staticStop.getStopLon(),
            staticStop.getParent());

  }

  private StopEntry findStop(String agencyId, String stopId) {
    return _graph.getStopEntryForId(new AgencyAndId(agencyId, stopId), true);
  }

  private double calculateTripDistance(DynamicTripEntryImpl trip) {
    double distance = 0.0;
    if (trip.getStopTimes() == null || trip.getStopTimes().size() < 2)
      return distance;
    CoordinatePoint lastLocation = null;
    for (StopTimeEntry stopTime : trip.getStopTimes()) {
      if (lastLocation == null) {
        lastLocation = stopTime.getStop().getStopLocation();
      } else {
        CoordinatePoint stopLocation = stopTime.getStop().getStopLocation();
        distance += distance(lastLocation, stopLocation);
        lastLocation = stopLocation;
      }
    }
    return distance;
  }


  private DynamicRouteEntry createRoute(AddedTripInfo addedTripInfo) {
    String routeId = addedTripInfo.getRouteId();
    if (!_routeCache.containsKey(routeId)) {
      RouteEntry staticRouteEntry = findRouteEntry(addedTripInfo.getAgencyId(), routeId);
      if (staticRouteEntry == null) throw new IllegalStateException("no such route " + routeId);
      _routeCache.put(routeId, copyFromRoute(staticRouteEntry));
    }
    return _routeCache.get(routeId);
  }

  private DynamicRouteEntry copyFromRoute(RouteEntry staticRouteEntry) {
    DynamicRouteEntry route = new DynamicRouteEntry();
    route.setId(staticRouteEntry.getId());
    route.setParent(staticRouteEntry.getParent());
    route.setTrips(new ArrayList<>());
    route.setType(EVehicleType.BUS.getGtfsType());
    return route;
  }

  private RouteEntry findRouteEntry(String agency, String routeId) {
    return _graph.getRouteForId(new AgencyAndId(agency, routeId));
  }

  private BlockEntry createBlockEntry(AddedTripInfo addedTripInfo) {
    DynamicBlockEntry entry = new DynamicBlockEntry();
    entry.setId(new AgencyAndId(addedTripInfo.getAgencyId(), addedTripInfo.getTripId()));
    entry.setConfigurations(new ArrayList<>()); // bidirectional reference
    return entry;
  }

  private ServiceIdActivation createServiceIdActivation(AddedTripInfo addedTripInfo) {
    return new ServiceIdActivation(createLocalizedServiceId(addedTripInfo));
  }

  private LocalizedServiceId createLocalizedServiceId(AddedTripInfo addedTripInfo) {
    // here we default to simply printing out the service date
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String serviceIdDate = sdf.format(addedTripInfo.getServiceDate());
    LocalizedServiceId serviceId = new LocalizedServiceId(
            new AgencyAndId(addedTripInfo.getAgencyId(), "DYN-" + serviceIdDate),
            TimeZone.getDefault()); // todo this could come from GTFS
    return serviceId;
  }

}
