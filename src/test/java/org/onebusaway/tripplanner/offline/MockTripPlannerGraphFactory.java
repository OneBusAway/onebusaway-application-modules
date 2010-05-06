package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.offline.StopEntryImpl;
import org.onebusaway.tripplanner.offline.StopProxyImpl;
import org.onebusaway.tripplanner.offline.TripPlannerGraphImpl;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class MockTripPlannerGraphFactory {

  private static GeometryFactory _geometryFactory = new GeometryFactory();

  private TripPlannerGraphImpl _graph = new TripPlannerGraphImpl();

  private int _stopTimeIndex = 0;

  public TripPlannerGraphImpl getGraph() {
    return _graph;
  }

  public StopProxy addStop(String stopId, double x, double y) {
    return addStop(stopId, x, y, 0, 0);
  }

  public StopProxy addStop(String stopId, double x, double y, double lat, double lon) {
    StopEntryImpl entry = _graph.getStopEntryByStopId(stopId);
    if (entry != null)
      throw new IllegalArgumentException("duplicate stop id: " + stopId);

    Point point = _geometryFactory.createPoint(new Coordinate(x, y));
    StopProxyImpl stop = new StopProxyImpl(stopId, point, lat, lon);

    entry = new StopEntryImpl(stop, null);
    _graph.putStopEntry(stopId, entry);
    return stop;
  }

  public TripEntry addTrip(String tripId, String serviceId, String routeId, String[] stopIds, int[] times) {

    if (stopIds.length != times.length)
      throw new IllegalArgumentException();
    if (stopIds.length == 0)
      throw new IllegalArgumentException();

    List<StopTimeProxyImpl> stopTimeProxies = new ArrayList<StopTimeProxyImpl>();

    for (int i = 0; i < stopIds.length; i++) {
      StopEntry entry = _graph.getStopEntryByStopId(stopIds[i]);
      StopProxy proxy = entry.getProxy();
      int t = times[i];
      StopTimeProxyImpl st = new StopTimeProxyImpl(_stopTimeIndex++, t, t, tripId, serviceId, routeId, i,proxy);
      stopTimeProxies.add(st);
    }

    for (int i = 0; i < stopTimeProxies.size() - 1; i++)
      addMinTransferTime(stopTimeProxies.get(i), stopTimeProxies.get(i + 1));

    TripEntryImpl entry = new TripEntryImpl(stopTimeProxies);
    _graph.putTripEntry(tripId, entry);
    return entry;
  }

  public void addMinTransferTime(String stopFrom, String stopTo, int travelTime) {

    StopEntryImpl fromEntry = _graph.getStopEntryByStopId(stopFrom);
    if (fromEntry == null)
      throw new IllegalArgumentException("uknown from stop: " + stopFrom);

    StopEntryImpl toEntry = _graph.getStopEntryByStopId(stopTo);
    if (toEntry == null)
      throw new IllegalArgumentException("uknown to stop: " + stopTo);

    fromEntry.addNextStopWithMinTravelTime(stopTo, travelTime);
    toEntry.addPreviousStopWithMinTravelTime(stopFrom, travelTime);
  }

  private void addMinTransferTime(StopTimeProxy stopTimeFrom, StopTimeProxy stopTimeTo) {
    StopProxy stopFrom = stopTimeFrom.getStop();
    StopProxy stopTo = stopTimeTo.getStop();
    addMinTransferTime(stopFrom.getStopId(), stopTo.getStopId(), stopTimeTo.getArrivalTime()
        - stopTimeFrom.getDepartureTime());
  }

  public void addBlock(String... tripIds) {

    String prevTripId = null;
    TripEntryImpl prevEntry = null;

    for (String tripId : tripIds) {

      TripEntryImpl tripEntry = _graph.getTripEntryByTripId(tripId);

      if (prevEntry != null) {

        prevEntry.setNextTripId(tripId);
        tripEntry.setPrevTripId(prevTripId);

        List<StopTimeProxy> prevStopTimes = prevEntry.getStopTimes();
        StopTimeProxy prevStopTime = prevStopTimes.get(prevStopTimes.size() - 1);

        List<StopTimeProxy> nextStopTimes = tripEntry.getStopTimes();
        StopTimeProxy nextStopTime = nextStopTimes.get(0);

        addMinTransferTime(prevStopTime, nextStopTime);
      }

      prevTripId = tripId;
      prevEntry = tripEntry;
    }
  }

  public void addTransferManhattanDistance(String... stopIds) {

    for (int i = 0; i < stopIds.length; i++) {

      String idA = stopIds[i];
      StopEntryImpl stopEntryA = _graph.getStopEntryByStopId(idA);

      for (int j = 0; j < i; j++) {

        String idB = stopIds[j];
        StopEntryImpl stopEntryB = _graph.getStopEntryByStopId(idB);

        double d = getManhattanDistance(stopEntryA, stopEntryB);
        stopEntryA.addTransfer(idB, d);
        stopEntryB.addTransfer(idA, d);
      }
    }
  }

  private double getManhattanDistance(StopEntryImpl a, StopEntryImpl b) {
    StopProxy sa = a.getProxy();
    StopProxy sb = b.getProxy();
    Point pa = sa.getStopLocation();
    Point pb = sb.getStopLocation();
    return Math.abs(pa.getX() - pb.getX()) + Math.abs(pa.getY() - pb.getY());
  }
}
