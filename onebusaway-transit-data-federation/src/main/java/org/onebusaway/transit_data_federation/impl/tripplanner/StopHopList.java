package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.util.AbstractList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;

/**
 * A memory-optimize structure for holding a list of {@link StopTransfer}
 * objects
 * 
 * @author bdferris
 */
public class StopHopList extends AbstractList<StopHop> {

  private final StopEntry[] stops;

  private final int[] minTravelTimes;

  public StopHopList(List<StopHop> stopHops) {

    int n = stopHops.size();

    stops = new StopEntry[n];
    minTravelTimes = new int[n];

    for (int i = 0; i < n; i++) {
      StopHop stopTransfer = stopHops.get(i);
      stops[i] = stopTransfer.getStop();
      minTravelTimes[i] = stopTransfer.getMinTravelTime();
    }
  }

  @Override
  public int size() {
    return stops.length;
  }

  @Override
  public StopHop get(int index) {
    StopEntry stop = stops[index];
    int minTravelTime = minTravelTimes[index];
    return new StopHop(stop, minTravelTime);
  }
}
