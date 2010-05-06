package org.onebusaway.tripplanner.impl;


import com.vividsolutions.jts.geom.Geometry;

import org.onebusaway.common.graph.Graph;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripEntry;
import org.onebusaway.tripplanner.model.TripPlannerGraph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PruneTransfersServiceImpl implements Runnable {

  // Miles per hour
  private static final double WALKING_SPEED_MPH = 2.5;

  private static final double WALKING_SPEED_FEET_PER_MS = WALKING_SPEED_MPH
      * 5280 /* feet/mile */* (1 / (60 * 60 * 1000.0)) /* hour/ms */;

  private TripPlannerGraph _graph;

  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public void run() {

    _graph.initialize();

    Set<String> stopIds = getStopIds();

    for (String stopId : stopIds) {
      StopEntry entry = _graph.getStopEntryByStopId(stopId);
      Stop stop = entry.getStop();
      Geometry boundary = stop.getLocation().buffer(5280 / 2).getBoundary();
      List<Stop> nearbyStops = _graph.getStopsByLocation(boundary);

      System.out.println("count=" + nearbyStops.size());

      for (Stop nearbyStop : nearbyStops) {

        if (stopId.equals(nearbyStop.getId()))
          continue;

        go(stop, nearbyStop);
      }

    }
  }

  private boolean go(Stop from, Stop to) {

    System.out.println("from=" + from.getId() + " to=" + to.getId());

    StopEntry fromEntry = _graph.getStopEntryByStopId(from.getId());

    Graph<Stop> fromGraph = new Graph<Stop>();
    Set<String> routes = new HashSet<String>();
    for (List<StopTime> sts : fromEntry.getStopTimes().values()) {
      for (StopTime st : sts) {
        
        Trip trip = st.getTrip();
        routes.add(trip.getRoute().getShortName());
        TripEntry tripEntry = _graph.getTripEntryByTripId(trip.getId());
        List<StopTime> tripTimes = tripEntry.getStopTimes();

        int index = tripTimes.indexOf(st);
        if (index == -1)
          throw new IllegalStateException();
        for (int i = 0; i < index; i++) {
          StopTime a = tripTimes.get(i);
          StopTime b = tripTimes.get(i + 1);
          fromGraph.addEdge(a.getStop(), b.getStop());
        }
      }
    }
    
    System.out.println("from=" + routes);

    StopEntry toEntry = _graph.getStopEntryByStopId(to.getId());

    Graph<Stop> toGraph = new Graph<Stop>();

    for (List<StopTime> sts : toEntry.getStopTimes().values()) {
      for (StopTime st : sts) {
        Trip trip = st.getTrip();
        TripEntry tripEntry = _graph.getTripEntryByTripId(trip.getId());
        List<StopTime> tripTimes = tripEntry.getStopTimes();

        int index = tripTimes.indexOf(st);
        if (index == -1)
          throw new IllegalStateException();
        for (int i = index; i + 1 < tripTimes.size(); i++) {
          StopTime a = tripTimes.get(i);
          StopTime b = tripTimes.get(i + 1);
          toGraph.addEdge(a.getStop(), b.getStop());
        }
      }
    }

    System.out.println("pre");
    for (Stop stop : fromGraph.getTopologicalSort(null)) {
      System.out.println(stop.getLat() + " " + stop.getLon());
    }

    System.out.println("post");
    for (Stop stop : toGraph.getTopologicalSort(null)) {
      System.out.println(stop.getLat() + " " + stop.getLon());
    }

    System.exit(-1);

    double checkWalkDistance = from.getLocation().distance(to.getLocation());
    double checkWalkTime = checkWalkDistance / WALKING_SPEED_FEET_PER_MS;

    Map<Stop, Double> fromDistances = getShortestPathsTo(fromGraph, from, true);
    Map<Stop, Double> toDistances = getShortestPathsTo(toGraph, to, false);

    for (Stop fromStop : fromGraph.getNodes()) {

      if (fromStop.equals(from))
        continue;

      for (Stop toStop : toGraph.getNodes()) {

        if (toStop.equals(to))
          continue;

        double busTimeA = fromDistances.get(fromStop) * 1000;
        double busTimeB = toDistances.get(toStop) * 1000;

        double directWalkDistance = fromStop.getLocation().distance(
            toStop.getLocation());
        double directWalkTime = directWalkDistance / WALKING_SPEED_FEET_PER_MS;

        if (busTimeA + checkWalkTime + busTimeB < directWalkTime) {
          System.out.println("  fromStop=" + fromStop + " toStop=" + toStop);
          System.out.println("    yeah");
          return true;
        }
      }

    }

    return false;
  }

  private Map<Stop, Double> getShortestPathsTo(Graph<Stop> graph, Stop target,
      boolean isTo) {

    Map<Stop, Double> distances = new HashMap<Stop, Double>();
    for (Stop stop : graph.getNodes())
      distances.put(stop, Double.POSITIVE_INFINITY);
    distances.put(target, 0.0);

    SortedSet<Stop> stops = new TreeSet<Stop>(new DistanceComparator(distances));
    stops.addAll(graph.getNodes());

    while (!stops.isEmpty()) {
      Stop stop = stops.first();
      stops.remove(stop);
      Set<Stop> adjacent = isTo ? graph.getInboundNodes(stop)
          : graph.getOutboundNodes(stop);
      for (Stop n : adjacent) {
        String idFrom = isTo ? n.getId() : stop.getId();
        String idTo = isTo ? stop.getId() : n.getId();
        int time = _graph.getMinTransitTime(idFrom, idTo);
        if (time == -1)
          throw new IllegalStateException();
        double d = time + distances.get(stop);
        if (d < distances.get(n)) {
          stops.remove(n);
          distances.put(n, d);
          stops.add(n);
        }
      }
    }

    return distances;
  }

  private Set<String> getStopIds() {
    if (false)
      return _graph.getStopIds();

    Set<String> ids = new HashSet<String>();
    ids.add("10030");
    return ids;
  }

  private static class DistanceComparator implements Comparator<Stop> {

    private Map<Stop, Double> _distances;

    public DistanceComparator(Map<Stop, Double> distances) {
      _distances = distances;
    }

    public int compare(Stop o1, Stop o2) {
      return _distances.get(o1).compareTo(_distances.get(o2));
    }
  }
}
