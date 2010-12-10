package org.onebusaway.transit_data_federation.impl.otp;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

public class SupportLibrary {

  public static boolean hasPreviousStopTime(StopTimeInstance instance) {
    return instance.getStopTime().getBlockSequence() > 0;
  }

  public static boolean hasNextStopTime(StopTimeInstance instance) {
    BlockStopTimeEntry stopTime = instance.getStopTime();
    BlockTripEntry trip = stopTime.getTrip();
    BlockConfigurationEntry blockConfig = trip.getBlockConfiguration();
    List<BlockStopTimeEntry> stopTimes = blockConfig.getStopTimes();
    return stopTime.getBlockSequence() + 1 < stopTimes.size();
  }

  public static long getNextTimeWindow(GraphContext context, long time) {
    int interval = context.getStopTimeSearchInterval() * 60 * 1000;
    long snapped = (time / interval) * interval;
    while (snapped < time)
      snapped += interval;
    if (snapped == time)
      return time + interval;
    return snapped;
  }

  public static long getNextTimeWindow(int stopTimeSearchInterval, long time) {
    int interval = stopTimeSearchInterval * 60 * 1000;
    long snapped = (time / interval) * interval;
    while (snapped < time)
      snapped += interval;
    if (snapped == time)
      return time + interval;
    return snapped;
  }

  public static long getPreviousTimeWindow(GraphContext context, long time) {
    int interval = context.getStopTimeSearchInterval() * 60 * 1000;
    long snapped = (time / interval) * interval;
    if (snapped == time)
      return snapped - interval;
    return snapped;
  }
}
