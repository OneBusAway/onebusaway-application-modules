/**
 * 
 */
package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.services.StopTimeInstanceProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;

import java.util.Comparator;

class TimeOp implements Comparator<StopTimeProxy> {

  public static final TimeOp ARRIVAL = new TimeOp(true);

  public static final TimeOp DEPARTURE = new TimeOp(false);

  private final boolean _useArrivalTime;

  private TimeOp(boolean useArrivalTime) {
    _useArrivalTime = useArrivalTime;
  }

  public int getTime(StopTimeProxy proxy) {
    return _useArrivalTime ? proxy.getArrivalTime() : proxy.getDepartureTime();
  }

  public long getTime(StopTimeInstanceProxy proxy) {
    return _useArrivalTime ? proxy.getArrivalTime() : proxy.getDepartureTime();
  }

  public int compare(StopTimeProxy o1, StopTimeProxy o2) {
    int a = getTime(o1);
    int b = getTime(o2);
    return a == b ? 0 : (a < b ? -1 : 1);
  }
}