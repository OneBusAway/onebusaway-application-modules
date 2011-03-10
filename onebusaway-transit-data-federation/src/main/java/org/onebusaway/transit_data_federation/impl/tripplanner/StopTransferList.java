package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

/**
 * A memory-optimize structure for holding a list of {@link StopTransfer}
 * objects
 * 
 * @author bdferris
 */
public class StopTransferList extends AbstractList<StopTransfer> {

  private final StopEntry[] stops;

  private final int[] minTravelTimes;

  private final double[] distances;

  public StopTransferList(List<StopTransfer> stopTransfers) {

    int n = stopTransfers.size();

    stops = new StopEntry[n];
    minTravelTimes = new int[n];
    distances = new double[n];

    for (int i = 0; i < n; i++) {
      StopTransfer stopTransfer = stopTransfers.get(i);
      stops[i] = stopTransfer.getStop();
      minTravelTimes[i] = stopTransfer.getMinTransferTime();
      distances[i] = stopTransfer.getDistance();
    }
  }

  @Override
  public int size() {
    return stops.length;
  }

  @Override
  public StopTransfer get(int index) {
    StopEntry stop = stops[index];
    int minTravelTime = minTravelTimes[index];
    double distance = distances[index];
    return new StopTransfer(stop, minTravelTime, distance);
  }
}
