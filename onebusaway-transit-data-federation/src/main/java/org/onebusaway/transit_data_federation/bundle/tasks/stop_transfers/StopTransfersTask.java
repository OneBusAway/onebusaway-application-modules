package org.onebusaway.transit_data_federation.bundle.tasks.stop_transfers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Min;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.impl.tripplanner.StopTransferData;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.opentripplanner.routing.services.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class StopTransfersTask implements Runnable {

  public static final String PROPERTY_PRINT_TRANSFERS = StopTransfersTask.class.getName()
      + ".printTransfers";

  private TripPlannerConstants _constants = new TripPlannerConstants();

  private StopWalkPlanCache _cachedStopTransferWalkPlanner = new StopWalkPlanCache();

  private Map<Pair<AgencyAndId>, Double> _cachedStopDistance = new HashMap<Pair<AgencyAndId>, Double>();

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _transitGraphDao;

  private BlockIndexService _blockIndexService;

  private RefreshService _refreshService;

  private PathService _pathService;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockS(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  @Autowired
  public void setPathService(PathService pathService) {
    _pathService = pathService;
  }

  @Transactional
  public void run() {

    try {

      _cachedStopTransferWalkPlanner.setPathService(_pathService);

      StopSequenceData data = new StopSequenceData();

      for (TripEntry trip : _transitGraphDao.getAllTrips())
        generateStatsForTrip(trip, data);

      System.out.println("stopSequences=" + data.size());

      Map<AgencyAndId, List<StopTransferData>> stopTransfers = processStopTransfers(data);

      System.out.println("stop walk cache: "
          + _cachedStopTransferWalkPlanner.getCacheHits() + " / "
          + _cachedStopTransferWalkPlanner.getTotalHits());

      if (System.getProperties().containsKey(PROPERTY_PRINT_TRANSFERS))
        printTransfers(stopTransfers);

      ObjectSerializationLibrary.writeObject(_bundle.getStopTransfersPath(),
          stopTransfers);

      _refreshService.refresh(RefreshableResources.STOP_TRANSFER_DATA);

    } catch (Exception ex) {
      throw new IllegalStateException("error in stop transfers task", ex);
    }
  }

  private void printTransfers(
      Map<AgencyAndId, List<StopTransferData>> stopTransfers) {
    for (Map.Entry<AgencyAndId, List<StopTransferData>> entry : stopTransfers.entrySet()) {
      AgencyAndId stopFromId = entry.getKey();
      StopEntry stopFrom = _transitGraphDao.getStopEntryForId(stopFromId);
      List<StopTransferData> datas = entry.getValue();
      for (StopTransferData stopTransfer : datas) {
        AgencyAndId toStopId = stopTransfer.getStopId();
        StopEntry stopTo = _transitGraphDao.getStopEntryForId(toStopId);
        System.out.println(stopFrom.getStopLat() + "," + stopFrom.getStopLon()
            + "," + stopTo.getStopLat() + "," + stopTo.getStopLon());
      }
    }
  }

  private void generateStatsForTrip(TripEntry trip, StopSequenceData data) {

    List<StopEntry> stops = new ArrayList<StopEntry>();
    List<StopTimeEntry> stopTimes = trip.getStopTimes();
    for (StopTimeEntry stopTime : stopTimes)
      stops.add(stopTime.getStop());

    StopSequenceKey key = new StopSequenceKey(stops);
    StopSequenceStats stats = data.getStatsForSequence(key);

    key = stats.getKey();

    stats.addTrip(trip);

    data.putSequenceForTrip(trip, stats.getKey());
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
   * @return
   */
  private Map<AgencyAndId, List<StopTransferData>> processStopTransfers(
      StopSequenceData data) {

    Map<Pair<StopEntry>, Transfer> activeTransfers = new HashMap<Pair<StopEntry>, Transfer>();

    int entryIndex = 0;

    for (StopSequenceKey key : data.getSequences()) {

      if (entryIndex % 20 == 0)
        System.out.println("stopSequences=" + entryIndex + "/" + data.size());
      entryIndex++;

      // Compute the potential transfers to another stop, specif
      Map<StopEntry, List<StopEntry>> potentialTransfers = computePotentialTransfers(key);

      // Keep only the best transfers
      Map<Pair<StopEntry>, Transfer> bestTransfers = getBestTransfers(
          potentialTransfers, data, key);
      activeTransfers.putAll(bestTransfers);

    }

    Map<AgencyAndId, List<StopTransferData>> stopTransfersBySourceStop = new FactoryMap<AgencyAndId, List<StopTransferData>>(
        new ArrayList<StopTransferData>());

    // Store all the transfers
    for (Map.Entry<Pair<StopEntry>, Transfer> entry : activeTransfers.entrySet()) {
      Pair<StopEntry> transfer = entry.getKey();
      Transfer t = entry.getValue();
      double distance = t.getWalkingDistance();
      StopEntry fromStop = transfer.getFirst();
      StopEntry toStop = transfer.getSecond();

      StopTransferData stopTransfer = new StopTransferData(toStop.getId(), 0,
          distance);
      stopTransfersBySourceStop.get(fromStop.getId()).add(stopTransfer);
    }

    return stopTransfersBySourceStop;
  }

  /**
   * Compute the set of potential transfers for a given trip block
   * 
   * @param key
   * 
   * @param key
   * @param transfers
   * @return
   * @return the set of potential transfer end-points, along with the list of
   *         potential transfer start-points for each end-point as a list of
   *         StopTimes
   */
  private Map<StopEntry, List<StopEntry>> computePotentialTransfers(
      StopSequenceKey key) {

    Map<StopEntry, List<StopEntry>> transfers = new FactoryMap<StopEntry, List<StopEntry>>(
        new ArrayList<StopEntry>());

    for (StopEntry stop : key.getStops()) {

      CoordinateBounds bounds = DistanceLibrary.bounds(stop.getStopLocation(),
          _constants.getMaxTransferDistance());

      List<StopEntry> nearbyStops = _transitGraphDao.getStopsByLocation(bounds);
      for (StopEntry nearbyStop : nearbyStops) {
        transfers.get(nearbyStop).add(stop);
      }
    }

    return transfers;
  }

  private Map<Pair<StopEntry>, Transfer> getBestTransfers(
      Map<StopEntry, List<StopEntry>> potentialTransferStartPointsByEndPoint,
      StopSequenceData data, StopSequenceKey key) {

    /**
     * Finds a set of new stops that aren't reachable in our current block, but
     * ARE reachable with a transfer to another route. Compute the set of new
     * stops, along with the transfer end-points from our current block along
     * with the travel time from the transfer end-point to the new stop
     */
    Map<StopEntry, Map<StopEntry, Integer>> newStopsAndPotentialTransferEndPoints = getPotentialTransfersToNewRoutes(
        potentialTransferStartPointsByEndPoint, data, key);

    Map<Pair<StopEntry>, Transfer> bestTransfers = new HashMap<Pair<StopEntry>, Transfer>();

    StopSequenceStats stats = data.getStatsForSequence(key);

    /**
     * Note that we don't really care about the new stop that is the eventual
     * destination for out transfer. We just care that it exists and we know the
     * travel times to it
     */
    for (Map<StopEntry, Integer> potentialTransferEndPointsWithTravelTimes : newStopsAndPotentialTransferEndPoints.values()) {

      List<Transfer> transfers = computeFastestTransferToNewStops(
          potentialTransferStartPointsByEndPoint,
          potentialTransferEndPointsWithTravelTimes, key, stats);

      optimisticallyUpdateTransferWalkingDistanceAndKeepBest(transfers,
          bestTransfers);
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
   * @param transferStartPointsByEndPoint
   * 
   * @return a map from stops not currently reachable in the current block trip
   *         along with the set of stops that are transfer points to that stop
   *         and the time in seconds for travel to the new stop
   */

  private Map<StopEntry, Map<StopEntry, Integer>> getPotentialTransfersToNewRoutes(
      Map<StopEntry, List<StopEntry>> potentialTransferStartPointsByEndPoint,
      StopSequenceData data, StopSequenceKey key) {

    // The set of already seen stops is the set of stops that can be transferred
    // to directly by walking
    Set<StopEntry> alreadySeenStops = Collections.unmodifiableSet(potentialTransferStartPointsByEndPoint.keySet());

    // Map from stops not in the already-seen set to stops in the already-seen
    // set that can reach the new stop, along with travel times in seconds
    Map<StopEntry, Map<StopEntry, Integer>> potentialTransfersWithTravelTimes = new HashMap<StopEntry, Map<StopEntry, Integer>>();

    // For a potential transfer destination
    for (StopEntry stopEntry : potentialTransferStartPointsByEndPoint.keySet()) {

      // Consider all the stop times at that stop

      Set<StopSequenceKey> outboundSequences = new HashSet<StopSequenceKey>();

      List<BlockStopTimeIndex> blockStopTimeIndices = _blockIndexService.getStopTimeIndicesForStop(stopEntry);
      for (BlockStopTimeIndex index : blockStopTimeIndices) {
        for (BlockStopTimeEntry blockStopTime : index.getStopTimes()) {
          BlockTripEntry blockTrip = blockStopTime.getTrip();
          StopSequenceKey outboundSequence = data.getSequenceForTrip(blockTrip.getTrip());
          outboundSequences.add(outboundSequence);
        }
      }

      for (StopSequenceKey outboundSequence : outboundSequences) {

        int stopIndex = outboundSequence.getIndexOfStop(stopEntry);

        StopSequenceStats outboundStats = data.getStatsForSequence(outboundSequence);

        findNewStopAlongTrip(outboundSequence, outboundStats, stopIndex,
            alreadySeenStops, potentialTransfersWithTravelTimes);
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
   * @param outboundSequence - the trip entry of the current trip
   * @param fromIndex - index into the list of StopTimes for the current trip
   * @param alreadySeen - the set of already-seen stops
   * @param potentialTransfersWithTravelTimes - map from already-seen stops
   *          along the specified trip along with their travel times in seconds
   *          to the new stop
   * 
   * @return the new stop not in the already-seen set
   */
  private void findNewStopAlongTrip(StopSequenceKey outboundSequence,
      StopSequenceStats stats, int fromIndex, Set<StopEntry> alreadySeen,
      Map<StopEntry, Map<StopEntry, Integer>> potentialTransfersWithTravelTimes) {

    List<StopEntry> outboundStops = outboundSequence.getStops();
    StopEntry fromStop = outboundStops.get(fromIndex);

    int toIndex = fromIndex;

    while (toIndex < outboundStops.size()) {

      StopEntry toNewStopEntry = outboundStops.get(toIndex);

      if (!alreadySeen.contains(toNewStopEntry)) {

        int time = stats.getTimeFromStart(toIndex)
            - stats.getTimeFromStart(fromIndex);

        Map<StopEntry, Integer> a = potentialTransfersWithTravelTimes.get(toNewStopEntry);

        if (a == null) {
          a = new HashMap<StopEntry, Integer>();
          potentialTransfersWithTravelTimes.put(toNewStopEntry, a);
        }

        Integer t = a.get(fromStop);
        if (t == null || time < t)
          a.put(fromStop, time);

        return;
      }

      toIndex++;
    }
  }

  private List<Transfer> computeFastestTransferToNewStops(
      Map<StopEntry, List<StopEntry>> potentialTransferStartPointsByEndPoint,
      Map<StopEntry, Integer> potentialTransferEndPointsWithTravelTimes,
      StopSequenceKey key, StopSequenceStats stats) {

    Map<Pair<Stop>, Min<Transfer>> minsByTransferPoint = new FactoryMap<Pair<Stop>, Min<Transfer>>(
        new Min<Transfer>());

    for (Map.Entry<StopEntry, Integer> entry : potentialTransferEndPointsWithTravelTimes.entrySet()) {

      StopEntry transferToEntry = entry.getKey();
      double busTimeSecondTrip = entry.getValue() * 1000;

      for (StopEntry transferFromEntry : potentialTransferStartPointsByEndPoint.get(transferToEntry)) {

        Pair<StopEntry> pair = Tuples.pair(transferFromEntry, transferToEntry);

        int index = key.getIndexOfStop(transferFromEntry);

        int busTimeFirstTrip = stats.getTimeFromStart(index) * 1000;

        double walkingDistance = getMinimumWalkDistanceBetweenStops(
            transferFromEntry, transferToEntry);

        Transfer transfer = new Transfer(pair, busTimeFirstTrip
            + busTimeSecondTrip, walkingDistance);

        minsByTransferPoint.get(pair).add(transfer.getTime(), transfer);
      }
    }

    List<Transfer> transfers = new ArrayList<Transfer>();

    for (Map.Entry<Pair<Stop>, Min<Transfer>> entry : minsByTransferPoint.entrySet()) {
      Min<Transfer> min = entry.getValue();
      transfers.addAll(min.getMinElements());
    }

    Collections.sort(transfers);

    return transfers;
  }

  private double getMinimumWalkDistanceBetweenStops(StopEntry stopA,
      StopEntry stopB) {

    AgencyAndId stopIdA = stopA.getId();
    AgencyAndId stopIdB = stopB.getId();
    Pair<AgencyAndId> pair = Tuples.pair(stopIdA, stopIdB);

    if (stopIdA.compareTo(stopIdB) > 0)
      pair = pair.swap();

    Double distance = _cachedStopDistance.get(pair);
    if (distance == null) {
      distance = SphericalGeometryLibrary.distance(stopA.getStopLocation(),
          stopB.getStopLocation());
      _cachedStopDistance.put(pair, distance);
    }
    return distance;
  }

  private void optimisticallyUpdateTransferWalkingDistanceAndKeepBest(
      List<Transfer> transfers,
      Map<Pair<StopEntry>, Transfer> resultingBestTransfers) {

    double minT = Double.POSITIVE_INFINITY;
    Transfer minTransfer = null;

    for (Transfer transfer : transfers) {

      if (transfer.getTime() > minT)
        break;

      try {

        double distance = _cachedStopTransferWalkPlanner.getWalkPlanDistanceForStopToStop(transfer.getStops());

        if (distance > _constants.getMaxTransferDistance())
          continue;

        transfer.setWalkingDistance(distance);

        if (transfer.getTime() < minT) {
          minTransfer = transfer;
          minT = transfer.getTime();
        }
      } catch (NoPathException ex) {

      }
    }

    if (minTransfer != null) {
      Pair<StopEntry> stops = minTransfer.getStops();
      resultingBestTransfers.put(stops, minTransfer);
    }
  }

  private class Transfer implements Comparable<Transfer> {

    private Pair<StopEntry> _stops;

    private double _busTime;

    private double _walkingDistance;

    public Transfer(Pair<StopEntry> pair, double busTime, double walkingDistance) {
      _stops = pair;
      _busTime = busTime;
      _walkingDistance = walkingDistance;
    }

    public void setWalkingDistance(double walkingDistance) {
      _walkingDistance = walkingDistance;
    }

    public double getWalkingDistance() {
      return _walkingDistance;
    }

    public Pair<StopEntry> getStops() {
      return _stops;
    }

    public double getTime() {
      return _busTime
          + (_walkingDistance == 0.0 ? 0 : _walkingDistance
              / _constants.getWalkingVelocity());
    }

    public int compareTo(Transfer o) {
      double t1 = getTime();
      double t2 = o.getTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }

  private static class StopSequenceData {

    private Map<StopSequenceKey, StopSequenceStats> _statsBySequence = new HashMap<StopSequenceKey, StopSequenceStats>();

    private Map<TripEntry, StopSequenceKey> _stopSequencesByTrip = new HashMap<TripEntry, StopSequenceKey>();

    public Set<StopSequenceKey> getSequences() {
      return _statsBySequence.keySet();
    }

    public StopSequenceKey getSequenceForTrip(TripEntry trip) {
      return _stopSequencesByTrip.get(trip);
    }

    public StopSequenceStats getStatsForSequence(StopSequenceKey key) {
      StopSequenceStats stats = _statsBySequence.get(key);
      if (stats == null) {
        stats = new StopSequenceStats(key);
        _statsBySequence.put(key, stats);
      }
      return stats;
    }

    public int size() {
      return _statsBySequence.size();
    }

    public void putSequenceForTrip(TripEntry trip, StopSequenceKey key) {
      _stopSequencesByTrip.put(trip, key);
    }
  }

  private static class StopSequenceKey {

    private List<StopEntry> _stops;

    public StopSequenceKey(List<StopEntry> stops) {
      _stops = stops;
    }

    public int getIndexOfStop(StopEntry stop) {
      return _stops.indexOf(stop);
    }

    public List<StopEntry> getStops() {
      return _stops;
    }

    @Override
    public int hashCode() {
      return _stops.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      StopSequenceKey other = (StopSequenceKey) obj;
      return _stops.equals(other._stops);
    }
  }

  private static class StopSequenceStats {

    private int[] _minTravelTime = null;

    private int[] _cumulativeTravelTime = null;

    private List<TripEntry> _trips = new ArrayList<TripEntry>();

    private StopSequenceKey _key;

    public StopSequenceStats(StopSequenceKey key) {
      _key = key;
    }

    public StopSequenceKey getKey() {
      return _key;
    }

    public void addTrip(TripEntry trip) {

      List<StopTimeEntry> stopTimes = trip.getStopTimes();
      ensureData(stopTimes);

      for (int i = 0; i < stopTimes.size() - 1; i++) {
        StopTimeEntry from = stopTimes.get(i);
        StopTimeEntry to = stopTimes.get(i);
        int time = to.getArrivalTime() - from.getDepartureTime();
        _minTravelTime[i] = Math.min(_minTravelTime[i], time);
      }

      _trips.add(trip);
    }

    public void pack() {
      _cumulativeTravelTime = new int[_minTravelTime.length + 1];
      _cumulativeTravelTime[0] = 0;
      for (int i = 0; i < _minTravelTime.length; i++)
        _cumulativeTravelTime[i + 1] = _cumulativeTravelTime[i]
            + _minTravelTime[i];
    }

    public int getTimeFromStart(int stopIndex) {
      if (_cumulativeTravelTime == null)
        pack();
      return _cumulativeTravelTime[stopIndex];
    }

    private void ensureData(List<StopTimeEntry> stopTimes) {
      if (stopTimes.isEmpty())
        return;
      int n = stopTimes.size() - 1;
      if (_minTravelTime == null) {
        _minTravelTime = new int[n];
        for (int i = 0; i < n; i++)
          _minTravelTime[i] = Integer.MAX_VALUE;
      }
    }

  }
}
