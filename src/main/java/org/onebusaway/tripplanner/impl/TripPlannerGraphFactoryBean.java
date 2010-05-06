package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.tuple.Pair;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.tripplanner.NoPathException;
import org.onebusaway.tripplanner.StopTransferWalkPlannerService;
import org.onebusaway.tripplanner.model.StopEntry;
import org.onebusaway.tripplanner.model.TripEntry;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.Walk;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TripPlannerGraphFactoryBean implements FactoryBean {

  private GtdfDao _dao;

  private int _stopPatternNodeIndex = 0;

  private StopPatternNode _rootNode = new StopPatternNode(
      _stopPatternNodeIndex++);

  private TripPlannerConstants _constants;

  private StopTransferWalkPlannerService _stopTransferWalkPlanner;

  @Autowired
  public void setGtdfDao(GtdfDao dao) {
    _dao = dao;
  }

  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  public void setStopTransferWalkPlannerService(
      StopTransferWalkPlannerService stopTransferWalkPlanner) {
    _stopTransferWalkPlanner = stopTransferWalkPlanner;
  }

  public Class<?> getObjectType() {
    return TripPlannerGraph.class;
  }

  public boolean isSingleton() {
    return false;
  }

  @Transactional
  public Object getObject() throws Exception {

    System.out.println("======== TripPlannerGraphFactory =>");
    System.out.println("  really?");

    TripPlannerGraph graph = new TripPlannerGraph();

    _dao.getAllRoutes();
    List<Trip> trips = _dao.getAllTrips();
    List<StopTime> stopTimes = _dao.getAllStopTimes();

    Map<String, List<Trip>> tripsByBlockId = CollectionsLibrary.mapToValueList(
        trips, "blockId", String.class);
    Map<String, List<StopTime>> stopTimesByTripId = CollectionsLibrary.mapToValueList(
        stopTimes, "trip.id", String.class);
    Map<String, List<StopTime>> stopTimesByStopId = CollectionsLibrary.mapToValueList(
        stopTimes, "stop.id", String.class);

    Map<Stop, Set<Integer>> stopPatternIdsByStop = new FactoryMap<Stop, Set<Integer>>(
        new HashSet<Integer>());

    int tripIndex = 0;
    int noStopTimes = 0;
    for (Trip trip : trips) {

      trip.getRoute().getLongName();

      if (tripIndex % 1000 == 0)
        System.out.println("trip: " + tripIndex + "/" + trips.size());
      tripIndex++;

      List<StopTime> stopTimesForTrip = stopTimesByTripId.get(trip.getId());

      if (stopTimesForTrip == null) {
        stopTimesForTrip = new ArrayList<StopTime>();
        noStopTimes++;
      } else {
        Collections.sort(stopTimesForTrip);
      }

      int stopPatternId = getStopPatternId(stopTimesForTrip);

      TripEntry tripEntry = new TripEntry();
      tripEntry.setStopTimes(stopTimesForTrip);
      tripEntry.setStopPatternId(stopPatternId);
      graph.putTripEntry(trip.getId(), tripEntry);

      StopTime prev = null;
      for (StopTime st : stopTimesForTrip) {

        Stop stop = st.getStop();
        stopPatternIdsByStop.get(stop).add(stopPatternId);

        if (prev != null) {
          int t = st.getArrivalTime() - prev.getDepartureTime();
          graph.setMinTransitTime(prev.getStop().getId(), st.getStop().getId(),
              t);
        }
        prev = st;
      }
    }

    System.out.println("noStopTimes=" + noStopTimes + "/" + trips.size());

    // Sort the blocks into proper trip order
    TripBlockComparator c = new TripBlockComparator(graph);
    int blockIndex = 0;
    for (List<Trip> block : tripsByBlockId.values()) {
      if (blockIndex % 1000 == 0)
        System.out.println("block: " + blockIndex + "/" + tripsByBlockId.size());
      blockIndex++;
      Collections.sort(block, c);
    }

    tripIndex = 0;

    for (Trip trip : trips) {

      if (tripIndex % 1000 == 0)
        System.out.println("trip: " + tripIndex + "/" + trips.size());
      tripIndex++;

      List<Trip> block = tripsByBlockId.get(trip.getBlockId());
      if (block == null)
        continue;
      int index = block.indexOf(trip);
      if (index == -1)
        throw new IllegalStateException("trip is not part of block?");
      if (index < block.size() - 1) {
        Trip next = block.get(index + 1);
        TripEntry entry = graph.getTripEntryByTripId(trip.getId());
        entry.setNextTrip(next);
      }
    }

    Map<Integer, List<Stop>> stopsByStopPatternId = _rootNode.getStopsById();

    int stopIndex = 0;

    List<Stop> stops = _dao.getAllStops();

    for (Stop stop : stops) {

      if (stopIndex % 500 == 0)
        System.out.println("stops: " + stopIndex + "/" + stops.size());
      stopIndex++;

      List<StopTime> times = stopTimesByStopId.get(stop.getId());
      if (times == null)
        times = new ArrayList<StopTime>();

      StopEntry stopEntry = new StopEntry();
      stopEntry.setStop(stop);

      // handleNearbyStops(stop, stopEntry);
      handleStopTimes(times, stop, stopEntry);

      graph.putStopEntry(stop, stopEntry);
    }

    graph.initialize();

    Set<Pair<Integer>> stopPatternPotentialTransfers = new HashSet<Pair<Integer>>();

    System.out.println("stopPatterns=" + stopsByStopPatternId.size());

    System.out.println("determining potential stopPattern transfers");
    stopIndex = 0;
    int transferCount = 0;
    StopCache cache = new StopCache();

    for (Stop stop : stops) {

      if (stopIndex % 500 == 0)
        System.out.println("stops: " + stopIndex + "/" + stops.size());
      stopIndex++;

      Point p = stop.getLocation();
      Geometry boundary = p.buffer(_constants.getMaxTransferDistance()).getBoundary();
      List<Stop> nearbyStops = graph.getStopsByLocation(boundary);
      for (Stop nearbyStop : nearbyStops) {

        if (stop.equals(nearbyStop))
          continue;

        // Verify that there is actually a walking path between the two stops
        // and that the path is not too long
        try {
          Walk walk = _stopTransferWalkPlanner.getWalkPlan(stop, nearbyStop);
          if (walk.getDistance() > _constants.getMaxTransferDistance())
            continue;
          cache.setWalkPlan(stop, nearbyStop, walk);
        } catch (NoPathException ex) {
          continue;
        }

        transferCount++;

        Set<Integer> idsA = stopPatternIdsByStop.get(stop);
        Set<Integer> idsB = stopPatternIdsByStop.get(nearbyStop);
        for (int idA : idsA) {
          for (int idB : idsB) {
            if (idA == idB)
              continue;
            Pair<Integer> pair = Pair.createPair(idA, idB);
            stopPatternPotentialTransfers.add(pair);
          }
        }
      }
    }

    System.out.println("  found " + stopPatternPotentialTransfers.size()
        + " potential transfers (transferCount=" + transferCount);

    int potentialIndex = 0;

    for (Pair<Integer> potential : stopPatternPotentialTransfers) {

      if (potentialIndex % 100 == 0)
        System.out.println("  " + potentialIndex + "/"
            + stopPatternPotentialTransfers.size());
      potentialIndex++;

      int stopPatternA = potential.getFirst();
      int stopPatternB = potential.getSecond();
      List<Stop> stopsA = stopsByStopPatternId.get(stopPatternA);
      List<Stop> stopsB = stopsByStopPatternId.get(stopPatternB);

      if (stopsA.isEmpty() || stopsB.isEmpty())
        continue;

      StopPatternTransfersImpl impl = new StopPatternTransfersImpl(_constants,
          graph, stopsA, stopsB, cache);
      Map<Pair<String>, Double> transfers = impl.computeTransfers();
      for (Pair<String> transfer : transfers.keySet()) {
        Double distance = transfers.get(transfer);
        String fromStop = transfer.getFirst();
        String toStop = transfer.getSecond();
        StopEntry entry = graph.getStopEntryByStopId(fromStop);
        entry.addTransfer(toStop, distance);
      }

    }

    return graph;
  }

  private int getStopPatternId(List<StopTime> stopTimes) {
    StopPatternNode node = _rootNode;
    for (StopTime stopTime : stopTimes)
      node = node.extend(stopTime.getStop());
    node.setEndpoint();
    return node.getId();
  }

  private void handleStopTimes(List<StopTime> stopTimes, Stop stop,
      StopEntry stopEntry) {

    Map<String, List<StopTime>> stopTimesByServiceId = CollectionsLibrary.mapToValueList(
        stopTimes, "trip.serviceId", String.class);
    for (List<StopTime> st : stopTimesByServiceId.values())
      Collections.sort(st);
    stopEntry.setStopTimes(stopTimesByServiceId);
  }

  private class StopPatternNode {

    private Map<Stop, StopPatternNode> _nodes = new HashMap<Stop, StopPatternNode>();

    private int _id;

    private boolean _endPoint = false;

    public StopPatternNode(int id) {
      _id = id;
    }

    public int getId() {
      return _id;
    }

    public StopPatternNode extend(Stop stop) {
      StopPatternNode node = _nodes.get(stop);
      if (node == null) {
        node = new StopPatternNode(_stopPatternNodeIndex++);
        _nodes.put(stop, node);
      }
      return node;
    }

    public void setEndpoint() {
      _endPoint = true;
    }

    public Map<Integer, List<Stop>> getStopsById() {
      Map<Integer, List<Stop>> byId = new HashMap<Integer, List<Stop>>();
      getStopsById(new LinkedList<Stop>(), byId);
      return byId;
    }

    private void getStopsById(LinkedList<Stop> stops,
        Map<Integer, List<Stop>> byId) {
      if (_endPoint)
        byId.put(_id, new ArrayList<Stop>(stops));

      for (Map.Entry<Stop, StopPatternNode> entry : _nodes.entrySet()) {
        Stop stop = entry.getKey();
        StopPatternNode node = entry.getValue();
        stops.addLast(stop);
        node.getStopsById(stops, byId);
        stops.removeLast();
      }
    }

  }

  private static class TripBlockComparator implements Comparator<Trip> {

    private TripPlannerGraph _graph;

    public TripBlockComparator(TripPlannerGraph graph) {
      _graph = graph;
    }

    public int compare(Trip o1, Trip o2) {
      TripEntry e1 = _graph.getTripEntryByTripId(o1.getId());
      TripEntry e2 = _graph.getTripEntryByTripId(o2.getId());
      List<StopTime> st1 = e1.getStopTimes();
      List<StopTime> st2 = e2.getStopTimes();

      if (st1.isEmpty() && st2.isEmpty())
        return o1.getId().compareTo(o2.getId());
      if (st1.isEmpty())
        return -1;
      if (st2.isEmpty())
        return 1;

      int t1 = st1.get(0).getDepartureTime();
      int t2 = st2.get(0).getDepartureTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }

  }

  private static class StopCache implements StopTransferWalkPlannerService {

    private Map<Stop, Map<Stop, Walk>> _cache = new HashMap<Stop, Map<Stop, Walk>>();

    public void setWalkPlan(Stop from, Stop to, Walk walk) {
      Map<Stop, Walk> m = _cache.get(from);
      if (m == null) {
        m = new HashMap<Stop, Walk>();
        _cache.put(from, m);
      }
      m.put(to, walk);
    }

    public Walk getWalkPlan(Stop from, Stop to) throws NoPathException {
      Map<Stop, Walk> m = _cache.get(from);
      if (m == null)
        return null;
      Walk walk = m.get(to);
      if (walk == null)
        return null;
      return walk;
    }
  }
}
