/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.transit_data_federation.impl.time.GenericBinarySearch.ValueAdapter;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;

import java.util.Comparator;

public abstract class StopTimeOp implements Comparator<StopTimeEntry>, ValueAdapter<StopTimeEntry> {

  public static final StopTimeOp ARRIVAL = new ByArrivalTime();

  public static final StopTimeOp DEPARTURE = new ByDepartureTime();

  public abstract double getValue(StopTimeEntry proxy);

  public abstract double getValue(StopTimeInstance proxy);

  public int compare(StopTimeEntry o1, StopTimeEntry o2) {
    double a = getValue(o1);
    double b = getValue(o2);
    return a == b ? 0 : (a < b ? -1 : 1);
  }

  private static class ByArrivalTime extends StopTimeOp {

    @Override
    public double getValue(StopTimeEntry proxy) {
      return proxy.getArrivalTime();
    }

    @Override
    public double getValue(StopTimeInstance proxy) {
      return proxy.getArrivalTime();
    }
  }

  private static class ByDepartureTime extends StopTimeOp {

    @Override
    public double getValue(StopTimeEntry proxy) {
      return proxy.getDepartureTime();
    }

    @Override
    public double getValue(StopTimeInstance proxy) {
      return proxy.getDepartureTime();
    }
  }
}