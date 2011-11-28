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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.bundle.tasks.ShapePointHelper;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockConfigurationEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockConfigurationEntriesFactory {

  private static Logger _log = LoggerFactory.getLogger(BlockConfigurationEntriesFactory.class);

  private BlockTripComparator _blockTripComparator = new BlockTripComparator();

  private BlockConfigurationComparator _blockConfigurationComparator = new BlockConfigurationComparator();

  private ServiceIdOverlapCache _serviceIdOverlapCache;

  private ShapePointHelper _shapePointHelper;

  @Autowired
  public void setServiceIdOverlapCache(
      ServiceIdOverlapCache serviceIdOverlapCache) {
    _serviceIdOverlapCache = serviceIdOverlapCache;
  }

  @Autowired
  public void setShapePointHelper(ShapePointHelper shapePointHelper) {
    _shapePointHelper = shapePointHelper;
  }

  public void processBlockConfigurations(BlockEntryImpl block,
      List<TripEntryImpl> tripsInBlock) {

    Map<LocalizedServiceId, List<TripEntryImpl>> tripsByServiceId = getTripsByServiceId(
        block, tripsInBlock);

    List<ServiceIdActivation> combinations = _serviceIdOverlapCache.getOverlappingServiceIdCombinations(tripsByServiceId.keySet());

    ArrayList<BlockConfigurationEntry> configurations = new ArrayList<BlockConfigurationEntry>();

    for (ServiceIdActivation serviceIds : combinations) {

      BlockConfigurationEntryImpl.Builder builder = processTripsForServiceIdConfiguration(
          block, tripsByServiceId, serviceIds);

      configurations.add(builder.create());
    }

    Collections.sort(configurations, _blockConfigurationComparator);
    configurations.trimToSize();

    if (configurations.isEmpty())
      _log.warn("no active block configurations found for block: "
          + block.getId());

    block.setConfigurations(configurations);

  }

  /****
   * Private Methods
   ****/

  private Map<LocalizedServiceId, List<TripEntryImpl>> getTripsByServiceId(
      BlockEntryImpl block, List<TripEntryImpl> tripsInBlock) {

    Map<LocalizedServiceId, List<TripEntryImpl>> tripsByServiceId = new FactoryMap<LocalizedServiceId, List<TripEntryImpl>>(
        new ArrayList<TripEntryImpl>());

    TimeZone tz = null;

    for (TripEntryImpl trip : tripsInBlock) {

      LocalizedServiceId serviceId = trip.getServiceId();

      if (tz == null) {
        tz = serviceId.getTimeZone();
      } else if (!tz.equals(serviceId.getTimeZone())) {
        throw new IllegalStateException(
            "trips in block must all have same timezone: block=" + block
                + " trip=" + trip + " execpted=" + tz + " actual="
                + serviceId.getTimeZone());
      }

      tripsByServiceId.get(serviceId).add(trip);
    }

    return tripsByServiceId;
  }

  private BlockConfigurationEntryImpl.Builder processTripsForServiceIdConfiguration(
      BlockEntryImpl block,
      Map<LocalizedServiceId, List<TripEntryImpl>> tripsByServiceId,
      ServiceIdActivation serviceIds) {

    ArrayList<TripEntry> trips = new ArrayList<TripEntry>();

    for (LocalizedServiceId serviceId : serviceIds.getActiveServiceIds()) {
      trips.addAll(tripsByServiceId.get(serviceId));
    }

    Collections.sort(trips, _blockTripComparator);
    trips.trimToSize();

    double[] tripGapDistances = computeGapDistancesBetweenTrips(trips);

    BlockConfigurationEntryImpl.Builder builder = BlockConfigurationEntryImpl.builder();
    builder.setBlock(block);
    builder.setServiceIds(serviceIds);
    builder.setTrips(trips);
    builder.setTripGapDistances(tripGapDistances);
    return builder;
  }


  private double[] computeGapDistancesBetweenTrips(List<TripEntry> trips) {

    double[] tripGapDistances = new double[trips.size()];

    if (_shapePointHelper == null)
      return tripGapDistances;

    for (int index = 0; index < trips.size() - 1; index++) {

      TripEntry tripA = trips.get(index);
      TripEntry tripB = trips.get(index + 1);

      double d = 0;

      ShapePoints shapeFrom = _shapePointHelper.getShapePointsForShapeId(tripA.getShapeId());
      ShapePoints shapeTo = _shapePointHelper.getShapePointsForShapeId(tripB.getShapeId());

      if (shapeFrom != null && shapeTo != null && !shapeFrom.isEmpty()
          && !shapeTo.isEmpty()) {
        int n = shapeFrom.getSize();
        double lat1 = shapeFrom.getLatForIndex(n - 1);
        double lon1 = shapeFrom.getLonForIndex(n - 1);
        double lat2 = shapeTo.getLatForIndex(0);
        double lon2 = shapeTo.getLonForIndex(0);
        d = SphericalGeometryLibrary.distance(lat1, lon1, lat2, lon2);
      }

      tripGapDistances[index] = d;
    }

    return tripGapDistances;
  }

  private static class BlockTripComparator implements Comparator<TripEntry> {

    public int compare(TripEntry o1, TripEntry o2) {

      int t1 = getAverageTime(o1);
      int t2 = getAverageTime(o2);

      return t1 - t2;
    }

    private int getAverageTime(TripEntry trip) {

      List<StopTimeEntry> stopTimes = trip.getStopTimes();

      if (stopTimes == null || stopTimes.isEmpty())
        throw new IllegalStateException("no StopTimes defined for trip " + trip);

      int departureTimes = 0;

      for (StopTimeEntry stopTime : stopTimes) {
        departureTimes += stopTime.getDepartureTime();
      }

      return departureTimes / stopTimes.size();
    }

  }

  private static class BlockConfigurationComparator implements
      Comparator<BlockConfigurationEntry> {

    @Override
    public int compare(BlockConfigurationEntry o1, BlockConfigurationEntry o2) {

      return o1.getServiceIds().compareTo(o2.getServiceIds());
    }
  }

}
