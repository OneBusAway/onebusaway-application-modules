package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockConfigurationEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockConfigurationEntriesFactory {

  private BlockTripComparator _blockTripComparator = new BlockTripComparator();

  private BlockConfigurationComparator _blockConfigurationComparator = new BlockConfigurationComparator();

  private ServiceIdOverlapCache _serviceIdOverlapCache;

  private ShapePointService _shapePointService;

  @Autowired
  public void setServiceIdOverlapCache(
      ServiceIdOverlapCache serviceIdOverlapCache) {
    _serviceIdOverlapCache = serviceIdOverlapCache;
  }

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  public void processBlockConfigurations(BlockEntryImpl block,
      List<TripEntryImpl> tripsInBlock) {

    processFrequencyBlockConfigurations(block, tripsInBlock, null);
  }

  public void processFrequencyBlockConfigurations(BlockEntryImpl block,
      List<TripEntryImpl> tripsInBlock,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlock) {

    Map<LocalizedServiceId, List<TripEntryImpl>> tripsByServiceId = getTripsByServiceId(
        block, tripsInBlock);

    List<ServiceIdActivation> combinations = _serviceIdOverlapCache.getOverlappingServiceIdCombinations(tripsByServiceId.keySet());

    ArrayList<BlockConfigurationEntry> configurations = new ArrayList<BlockConfigurationEntry>();

    for (ServiceIdActivation serviceIds : combinations) {

      BlockConfigurationEntryImpl.Builder builder = processTripsForServiceIdConfiguration(
          block, tripsByServiceId, serviceIds);

      List<TripEntry> trips = builder.getTrips();

      if (frequenciesAlongBlock != null) {
        List<FrequencyEntry> frequencies = computeBlockFrequencies(block,
            trips, frequenciesAlongBlock);
        builder.setFrequencies(frequencies);
      }

      configurations.add(builder.create());
    }

    Collections.sort(configurations, _blockConfigurationComparator);
    configurations.trimToSize();

    if (configurations.isEmpty())
      System.out.println("no block configurations found for block: "
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

  private List<FrequencyEntry> computeBlockFrequencies(BlockEntryImpl block,
      List<TripEntry> trips,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlock) {

    List<FrequencyEntry> frequencies = null;

    for (TripEntry trip : trips) {

      List<FrequencyEntry> potentialFrequencies = frequenciesAlongBlock.get(trip.getId());

      if (frequencies == null) {
        frequencies = potentialFrequencies;
      } else {
        if (!frequencies.equals(potentialFrequencies)) {
          throw new IllegalStateException(
              "frequency-based trips in same block don't have same frequencies: blockId="
                  + block.getId());
        }
      }
    }

    return frequencies;
  }

  private double[] computeGapDistancesBetweenTrips(List<TripEntry> trips) {

    double[] tripGapDistances = new double[trips.size()];

    if (_shapePointService == null)
      return tripGapDistances;

    for (int index = 0; index < trips.size() - 1; index++) {

      TripEntry tripA = trips.get(index);
      TripEntry tripB = trips.get(index + 1);

      double d = 0;

      ShapePoints shapeFrom = _shapePointService.getShapePointsForShapeId(tripA.getShapeId());
      ShapePoints shapeTo = _shapePointService.getShapePointsForShapeId(tripB.getShapeId());

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
