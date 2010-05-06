package org.onebusaway.tripplanner.offline;

import org.onebusaway.common.impl.ObjectSerializationLibrary;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.model.WalkPlan;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.StopTimeProxy;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.collections.tuple.Pair;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TripPlannerGraphTask implements Runnable {

  private static BlockTripSequenceComparator _blockTripComparator = new BlockTripSequenceComparator();

  private static StopTimeComparator _stopTimeComparator = new StopTimeComparator();

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private TripPlannerConstants _constants;

  @Autowired
  private StopWalkPlanCache _cachedStopTransferWalkPlanner;

  private File _outputPath;

  public void setOutputPath(File path) {
    _outputPath = path;
  }

  @Transactional
  public void run() {

    System.out.println("======== TripPlannerGraphFactory =>");

    TripPlannerGraphImpl graph = new TripPlannerGraphImpl();

    List<String> blockIds = _gtfsDao.getAllBlockIds();

    processStops(graph);
    Map<String, List<String>> tripsByBlockId = processBlockTrips(graph, blockIds);
    sortStopTimeIndices(graph);
    processStopTransfers(graph, tripsByBlockId);

    System.out.println("stop walk cache: " + _cachedStopTransferWalkPlanner.getCacheHits() + " / "
        + _cachedStopTransferWalkPlanner.getTotalHits());

    try {
      ObjectSerializationLibrary.writeObject(_outputPath, graph);
    } catch (Exception ex) {
      throw new IllegalStateException("error writing graph to file", ex);
    }
  }

  /**
   * Iterate over each stop, generating a StopEntry for the graph.
   * 
   * @param graph
   */
  private void processStops(TripPlannerGraphImpl graph) {

    int stopIndex = 0;

    List<Stop> stops = _gtfsDao.getAllStops();

    for (Stop stop : stops) {

      StopProxyImpl proxy = new StopProxyImpl(stop.getId(), stop.getLocation(), stop.getLat(), stop.getLon());

      if (stopIndex % 500 == 0)
        System.out.println("stops: " + stopIndex + "/" + stops.size());
      stopIndex++;

      StopEntryImpl stopEntry = new StopEntryImpl(proxy, new StopTimeIndexImpl());

      graph.putStopEntry(proxy.getStopId(), stopEntry);
    }
  }

  /**
   * We loop over blocks of trips, removing any trip that has no stop times,
   * sorting the remaining trips into the proper order, setting the 'nextTrip'
   * property for trips in the block, and setting the 'nextStop' property for
   * stops in the block.
   * 
   * @param graph
   * @param blockIds
   * @return
   */
  private Map<String, List<String>> processBlockTrips(TripPlannerGraphImpl graph, List<String> blockIds) {

    int blockIndex = 0;

    Map<String, List<String>> tripsByBlockId = new HashMap<String, List<String>>();

    for (String blockId : blockIds) {

      if (blockIndex % 10 == 0)
        System.out.println("block: " + blockIndex + "/" + blockIds.size());
      blockIndex++;

      List<Trip> trips = _gtfsDao.getTripsByBlockId(blockId);
      List<StopTime> stopTimes = _gtfsDao.getStopTimesByBlockId(blockId);

      Collections.sort(trips, _blockTripComparator);
      Collections.sort(stopTimes, _stopTimeComparator);

      Map<String, List<StopTimeProxyImpl>> stopTimesByTripId = getStopTimesByTripId(graph, stopTimes);

      processTrips(graph, trips, stopTimesByTripId);

      Trip prevTrip = null;
      TripEntryImpl prevEntry = null;
      StopTimeProxyImpl prevStopTime = null;

      List<String> tripIds = new ArrayList<String>();

      for (Trip trip : trips) {

        TripEntryImpl tripEntry = graph.getTripEntryByTripId(trip.getId());
        tripIds.add(trip.getId());

        // Set PreviousTrip and NextTrip for the trips in the block
        if (prevTrip != null) {
          prevEntry.setNextTripId(trip.getId());
          tripEntry.setPrevTripId(prevTrip.getId());
        }

        // Set the next and previous stops information
        for (StopTimeProxyImpl stopTime : stopTimesByTripId.get(trip.getId())) {

          if (prevStopTime != null) {

            StopProxy stopFrom = prevStopTime.getStop();
            StopProxy stopTo = stopTime.getStop();

            int duration = stopTime.getArrivalTime() - prevStopTime.getDepartureTime();

            if (duration < 0) {
              throw new IllegalStateException();
            }

            StopEntryImpl fromStopEntry = graph.getStopEntryByStopId(stopFrom.getStopId());
            fromStopEntry.addNextStopWithMinTravelTime(stopTo.getStopId(), duration);

            StopEntryImpl toStopEntry = graph.getStopEntryByStopId(stopTo.getStopId());
            toStopEntry.addPreviousStopWithMinTravelTime(stopFrom.getStopId(), duration);
          }
          prevStopTime = stopTime;
        }

        prevTrip = trip;
        prevEntry = tripEntry;
      }

      tripsByBlockId.put(blockId, tripIds);
    }

    return tripsByBlockId;
  }

  private Map<String, List<StopTimeProxyImpl>> getStopTimesByTripId(TripPlannerGraphImpl graph, List<StopTime> stopTimes) {

    Map<String, List<StopTimeProxyImpl>> stopTimesByTripId = new FactoryMap<String, List<StopTimeProxyImpl>>(
        new ArrayList<StopTimeProxyImpl>());

    for (StopTime stopTime : stopTimes) {

      StopEntryImpl stopEntry = graph.getStopEntryByStopId(stopTime.getStop().getId());
      StopTimeIndexImpl index = stopEntry.getStopTimes();

      StopTimeProxyImpl stopTimeProxy = new StopTimeProxyImpl(stopTime, stopEntry.getProxy());
      index.addStopTime(stopTimeProxy);

      stopTimesByTripId.get(stopTime.getTrip().getId()).add(stopTimeProxy);
    }

    return stopTimesByTripId;
  }

  private void sortStopTimeIndices(TripPlannerGraphImpl graph) {
    for (String stopId : graph.getStopIds()) {
      StopEntryImpl entry = graph.getStopEntryByStopId(stopId);
      StopTimeIndexImpl index = entry.getStopTimes();
      index.sort();
    }
  }

  /**
   * Loop over each trip, creating a TripEntry for the graph. Examine the
   * StopTimes for the trip, gathering statistics about minimum travel time
   * between consecutive stops
   * 
   * @param graph
   */
  private void processTrips(TripPlannerGraphImpl graph, List<Trip> trips,
      Map<String, List<StopTimeProxyImpl>> stopTimesByTripId) {

    for (Trip trip : trips) {

      List<StopTimeProxyImpl> stopTimesForTrip = stopTimesByTripId.get(trip.getId());

      TripEntryImpl tripEntry = new TripEntryImpl(stopTimesForTrip);
      graph.putTripEntry(trip.getId(), tripEntry);

      StopTimeProxy prev = null;
      for (StopTimeProxy st : stopTimesForTrip) {

        if (prev != null) {
          int t = st.getArrivalTime() - prev.getDepartureTime();
          StopProxy prevStop = prev.getStop();
          StopProxy nextStop = st.getStop();
          graph.setMinTransitTime(prevStop.getStopId(), nextStop.getStopId(), t);
        }

        prev = st;
      }
    }
  }

  /**
   * General idea:
   * 
   * For a given block of trips, we examine each stop along the block. For each
   * stop, we compute the sets of reachable stops based on the max transfer
   * distance. For each potential transfer, we prune out unnecessary transfers,
   * keeping only the best. See getBestTransfers for more info.
   * 
   * @param tripsByBlockId
   */
  private void processStopTransfers(TripPlannerGraphImpl graph, Map<String, List<String>> tripsByBlockId) {

    graph.initialize();

    Map<Pair<StopProxy>, Double> activeTransfers = new HashMap<Pair<StopProxy>, Double>();

    int blockIndex = 0;

    // For each block id and the ordered list of trips in that block
    for (Map.Entry<String, List<String>> entry : tripsByBlockId.entrySet()) {

      if (blockIndex % 20 == 0)
        System.out.println("blocks=" + blockIndex + "/" + tripsByBlockId.size());
      blockIndex++;

      List<String> tripIds = entry.getValue();
      String firstTripId = tripIds.get(0);

      // Compute the potential transfers to another stop, specif
      Map<StopProxy, List<StopTimeProxy>> transfers = new FactoryMap<StopProxy, List<StopTimeProxy>>(
          new ArrayList<StopTimeProxy>());
      StopTimeProxy firstStop = computePotentialTransfers(graph, firstTripId, transfers);

      // Keep only the best transfers
      Map<Pair<StopProxy>, Double> bestTransfers = getBestTransfers(graph, firstStop, transfers);
      activeTransfers.putAll(bestTransfers);
    }

    // Store all the transfers
    for (Map.Entry<Pair<StopProxy>, Double> entry : activeTransfers.entrySet()) {
      Pair<StopProxy> transfer = entry.getKey();
      double distance = entry.getValue();
      StopProxy fromStop = transfer.getFirst();
      StopProxy toStop = transfer.getSecond();
      StopEntryImpl stopEntry = graph.getStopEntryByStopId(fromStop.getStopId());
      stopEntry.addTransfer(toStop.getStopId(), distance);
    }
  }

  private StopTimeProxy computePotentialTransfers(TripPlannerGraphImpl graph, String tripId,
      Map<StopProxy, List<StopTimeProxy>> transfers) {

    TripEntryImpl entry = graph.getTripEntryByTripId(tripId);
    StopTimeProxy first = null;

    for (StopTimeProxy stopTime : entry.getStopTimes()) {
      if (first == null)
        first = stopTime;

      StopProxy stop = stopTime.getStop();
      Point p = stop.getStopLocation();
      Geometry boundary = p.buffer(_constants.getMaxTransferDistance()).getBoundary();
      List<String> nearbyStopIds = graph.getStopsByLocation(boundary);
      for (String nearbyStopId : nearbyStopIds) {
        StopEntry nearbyStop = graph.getStopEntryByStopId(nearbyStopId);
        transfers.get(nearbyStop.getProxy()).add(stopTime);
      }
    }

    if (entry.getNextTripId() != null) {
      StopTimeProxy st = computePotentialTransfers(graph, entry.getNextTripId(), transfers);
      if (first == null)
        first = st;
    }

    return first;

  }

  private Map<Pair<StopProxy>, Double> getBestTransfers(TripPlannerGraphImpl graph, StopTimeProxy firstStop,
      Map<StopProxy, List<StopTimeProxy>> transfersByStop) {

    Map<StopProxy, Map<StopProxy, Integer>> potentialTransfersWithTravelTimes = getPotentialTransfersToNewRoutes(graph,
        transfersByStop);

    Map<Pair<StopProxy>, Double> bestTransfers = new HashMap<Pair<StopProxy>, Double>();

    for (Map<StopProxy, Integer> m : potentialTransfersWithTravelTimes.values()) {

      Map<Pair<Stop>, Min<Transfer>> minsByTransferPoint = new FactoryMap<Pair<Stop>, Min<Transfer>>(
          new Min<Transfer>());

      for (Map.Entry<StopProxy, Integer> entry : m.entrySet()) {
        StopProxy transferToStop = entry.getKey();
        double busTimeSecondTrip = entry.getValue() * 1000;
        for (StopTimeProxy stopTime : transfersByStop.get(transferToStop)) {
          StopProxy transferFromStop = stopTime.getStop();
          double walkingDistance = UtilityLibrary.distance(transferFromStop.getStopLocation(),
              transferToStop.getStopLocation());
          double busTimeFirstTrip = (stopTime.getArrivalTime() - firstStop.getDepartureTime()) * 1000;

          Pair<StopProxy> pair = Pair.createPair(transferFromStop, transferToStop);
          Transfer transfer = new Transfer(pair, busTimeFirstTrip + busTimeSecondTrip, walkingDistance);
          minsByTransferPoint.get(pair).add(transfer.getTime(), transfer);
        }
      }

      List<Transfer> transfers = new ArrayList<Transfer>();

      for (Map.Entry<Pair<Stop>, Min<Transfer>> entry : minsByTransferPoint.entrySet()) {
        Min<Transfer> min = entry.getValue();
        transfers.addAll(min.getMinElements());
      }

      Collections.sort(transfers);

      double minT = Double.POSITIVE_INFINITY;
      Transfer minTransfer = null;

      for (Transfer transfer : transfers) {

        if (transfer.getTime() > minT)
          break;

        Pair<StopProxy> stops = transfer.getStops();
        try {
          WalkPlan walk = _cachedStopTransferWalkPlanner.getWalkPlanForStopToStop(stops);
          transfer.setWalkingDistance(walk.getDistance());

          if (transfer.getTime() < minT) {
            minTransfer = transfer;
            minT = transfer.getTime();
          }
        } catch (NoPathException e) {

        }
      }

      if (minTransfer != null) {
        Pair<StopProxy> stops = minTransfer.getStops();
        bestTransfers.put(stops, minTransfer.getWalkingDistance());
      }
    }

    return bestTransfers;
  }

  /**
   * Given a set of potential transfers to nearby stops and the list of
   * StopTimes in the current block that can transfer to that nearby stop,
   * return the set of transfers that actually reach a new stop not already
   * reachable by the current block trip
   * 
   * @param graph
   * @param transfersByStop
   * @return a map from stops not currently reachable in the current block trip
   *         along with the set of stops that are transfer points to that stop
   *         and the time in seconds for travel to the new stop
   */
  private Map<StopProxy, Map<StopProxy, Integer>> getPotentialTransfersToNewRoutes(TripPlannerGraphImpl graph,
      Map<StopProxy, List<StopTimeProxy>> transfersByStop) {

    // The set of already seen stops is the set of stops that can be transferred
    // to directly by walking
    Set<StopProxy> alreadySeenStops = new HashSet<StopProxy>(transfersByStop.keySet());

    // Map from stops not in the already-seen set to stops in the already-seen
    // set that can reach the new stop, along with travel times in seconds
    Map<StopProxy, Map<StopProxy, Integer>> potentialTransfersWithTravelTimes = new FactoryMap<StopProxy, Map<StopProxy, Integer>>(
        new HashMap<StopProxy, Integer>());

    // For a potential transfer destination
    for (StopProxy stop : transfersByStop.keySet()) {

      // Consider all the stop times at that stop
      StopEntryImpl stopEntry = graph.getStopEntryByStopId(stop.getStopId());
      StopTimeIndexImpl index = stopEntry.getStopTimes();
      List<StopTimeProxyImpl> stopTimes = index.getAllStopTimes();

      Set<String> routeIds = new HashSet<String>();

      for (StopTimeProxy stopTime : stopTimes) {

        // If the stop time includes a route we have not seen already
        if (routeIds.add(stopTime.getRouteId())) {
          TripEntryImpl tripEntry = graph.getTripEntryByTripId(stopTime.getTripId());
          Map<StopProxy, Integer> travelTimesFromAlreadySeenStopsToNewStop = new HashMap<StopProxy, Integer>();
          StopTimeProxy newStop = findNewStopAlongTrip(graph, tripEntry, stopTime.getSequence(), alreadySeenStops,
              travelTimesFromAlreadySeenStopsToNewStop);
          Map<StopProxy, Integer> x = potentialTransfersWithTravelTimes.get(newStop);
          for (Map.Entry<StopProxy, Integer> mEntry : travelTimesFromAlreadySeenStopsToNewStop.entrySet()) {
            StopProxy mStop = mEntry.getKey();
            int travelTime = mEntry.getValue();
            Integer existingTime = x.get(mStop);
            if (existingTime == null || travelTime < existingTime)
              x.put(mStop, travelTime);
          }
        }
      }
    }
    return potentialTransfersWithTravelTimes;
  }

  /**
   * Given a position along a Trip, follow along the Trip and any subsequent
   * trips joined by a trip-block to find a new stop not in the already-seen
   * set.
   * 
   * The TripEntry + index specifies a position along a trip, where index
   * specifies an index into the List of StopTimes for the specified TripEntry.
   * 
   * During the search for a new stop, if we encounter stops that are in the
   * already-seen set, we add them to the output map with the already-seen stop
   * as key and the travel time in seconds from the already-seen stop to the new
   * stop as the value.
   * 
   * @param graph - the trip planner graph
   * @param tripEntry - the trip entry of the current trip
   * @param index - index into the list of StopTimes for the current trip
   * @param alreadySeen - the set of already-seen stops
   * @param travelTimesFromAlreadySeenStopsToNewStop - map from already-seen
   *          stops along the specified trip along with their travel times in
   *          seconds to the new stop
   * @return the new stop not in the already-seen set
   */
  private StopTimeProxy findNewStopAlongTrip(TripPlannerGraphImpl graph, TripEntryImpl tripEntry, int index,
      Set<StopProxy> alreadySeen, Map<StopProxy, Integer> travelTimesFromAlreadySeenStopsToNewStop) {

    List<StopTimeProxy> stopTimes = tripEntry.getStopTimes();

    if (index < stopTimes.size()) {

      StopTimeProxy stopTime = stopTimes.get(index);

      StopProxy stop = stopTime.getStop();

      if (!alreadySeen.contains(stop))
        return stopTime;

      StopTimeProxy newStopTime = findNewStopAlongTrip(graph, tripEntry, index + 1, alreadySeen,
          travelTimesFromAlreadySeenStopsToNewStop);

      // If we a new stop was found, add the already-seen stop to the result set
      // along with the travel time from the already-seen stop to the new stop
      if (newStopTime != null) {
        int tripLength = newStopTime.getArrivalTime() - stopTime.getDepartureTime();
        Integer existingTime = travelTimesFromAlreadySeenStopsToNewStop.get(stop);
        if (existingTime == null || tripLength < existingTime)
          travelTimesFromAlreadySeenStopsToNewStop.put(stop, tripLength);
      }

      return newStopTime;

    } else if (tripEntry.getNextTripId() != null) {
      tripEntry = graph.getTripEntryByTripId(tripEntry.getNextTripId());
      return findNewStopAlongTrip(graph, tripEntry, 0, alreadySeen, travelTimesFromAlreadySeenStopsToNewStop);
    }

    return null;
  }

  private class Transfer implements Comparable<Transfer> {

    private Pair<StopProxy> _stops;

    private double _busTime;

    private double _walkingDistance;

    public Transfer(Pair<StopProxy> stops, double busTime, double walkingDistance) {
      _stops = stops;
      _busTime = busTime;
      _walkingDistance = walkingDistance;
    }

    public void setWalkingDistance(double walkingDistance) {
      _walkingDistance = walkingDistance;
    }

    public double getWalkingDistance() {
      return _walkingDistance;
    }

    public Pair<StopProxy> getStops() {
      return _stops;
    }

    public double getTime() {
      return _busTime + (_walkingDistance == 0.0 ? 0 : _walkingDistance / _constants.getWalkingVelocity());
    }

    public int compareTo(Transfer o) {
      double t1 = getTime();
      double t2 = o.getTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }

  private static class BlockTripSequenceComparator implements Comparator<Trip> {
    public int compare(Trip o1, Trip o2) {
      return o1.getBlockSequenceId() - o2.getBlockSequenceId();
    }
  }

  private static class StopTimeComparator implements Comparator<StopTime> {

    public int compare(StopTime o1, StopTime o2) {
      return o1.getArrivalTime() - o2.getArrivalTime();
    }

  }
}
