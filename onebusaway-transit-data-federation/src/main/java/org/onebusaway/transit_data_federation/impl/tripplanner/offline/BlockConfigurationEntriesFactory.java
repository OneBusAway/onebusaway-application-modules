package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockConfigurationEntriesFactory {

  private BlockTripComparator _blockTripComparator = new BlockTripComparator();

  private BlockConfigurationComparator _blockConfigurationComparator = new BlockConfigurationComparator();

  private ServiceIdOverlapCache _serviceIdOverlapCache;

  @Autowired
  public void setServiceIdOverlapCache(
      ServiceIdOverlapCache serviceIdOverlapCache) {
    _serviceIdOverlapCache = serviceIdOverlapCache;
  }

  public void processBlockConfigurations(BlockEntryImpl block,
      List<TripEntryImpl> tripsInBlock) {

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

    List<ServiceIdActivation> combinations = _serviceIdOverlapCache.getOverlappingServiceIdCombinations(tripsByServiceId.keySet());

    ArrayList<BlockConfigurationEntry> configurations = new ArrayList<BlockConfigurationEntry>();

    for (ServiceIdActivation serviceIds : combinations) {

      ArrayList<TripEntry> trips = new ArrayList<TripEntry>();
      for (LocalizedServiceId serviceId : serviceIds.getActiveServiceIds()) {
        trips.addAll(tripsByServiceId.get(serviceId));
      }

      Collections.sort(trips, _blockTripComparator);
      trips.trimToSize();

      BlockConfigurationEntryImpl.Builder builder = BlockConfigurationEntryImpl.builder();
      builder.setBlock(block);
      builder.setServiceIds(serviceIds);
      builder.setTrips(trips);

      configurations.add(builder.create());
    }

    Collections.sort(configurations, _blockConfigurationComparator);
    configurations.trimToSize();

    block.setConfigurations(configurations);
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
