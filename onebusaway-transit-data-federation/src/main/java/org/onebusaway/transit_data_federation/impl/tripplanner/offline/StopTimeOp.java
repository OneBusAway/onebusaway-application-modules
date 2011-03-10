/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

import java.util.Comparator;

public class StopTimeOp implements Comparator<StopTimeEntry> {

  public static final StopTimeOp ARRIVAL = new StopTimeOp(true);

  public static final StopTimeOp DEPARTURE = new StopTimeOp(false);

  private final boolean _useArrivalTime;

  private StopTimeOp(boolean useArrivalTime) {
    _useArrivalTime = useArrivalTime;
  }

  public int getTime(StopTimeEntry proxy) {
    return _useArrivalTime ? proxy.getArrivalTime() : proxy.getDepartureTime();
  }

  public long getTime(StopTimeInstanceProxy proxy) {
    return _useArrivalTime ? proxy.getArrivalTime() : proxy.getDepartureTime();
  }

  public int compare(StopTimeEntry o1, StopTimeEntry o2) {
    int a = getTime(o1);
    int b = getTime(o2);
    return a == b ? 0 : (a < b ? -1 : 1);
  }
}