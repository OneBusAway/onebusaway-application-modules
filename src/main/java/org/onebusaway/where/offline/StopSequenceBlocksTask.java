package org.onebusaway.where.offline;

import org.onebusaway.common.graph.Graph;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.StopSequenceBlockKey;
import org.onebusaway.where.services.WhereDao;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Max;

import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Construct a set of {@link StopSequenceBlock} blocks for each route. A block
 * contains a set of {@link StopSequence} sequences that are headed in the same
 * direction for a particular route, along with a general description of the
 * destinations for those stop sequences and general start and stop locations
 * for the sequences.
 * 
 * @author bdferris
 */
@Component
public class StopSequenceBlocksTask implements Runnable {

  private static final double SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN = 0.2;

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private WhereDao _whereDao;

  private List<Route> getRoutes() {

    if (true)
      return _gtfsDao.getAllRoutes();

    List<Route> routes = new ArrayList<Route>();
    routes.add(_gtfsDao.getRouteByShortName("30"));
    return routes;
  }

  @Transactional
  public void run() {

    for (Route route : getRoutes()) {

      System.out.println("== ROUTE " + route.getShortName() + " ==");

      List<StopSequence> sequences = _whereDao.getStopSequencesByRoute(route);

      pruneEmptyStopSequences(sequences);

      if (sequences.isEmpty())
        throw new IllegalStateException("no stop sequences for route: " + route);

      Map<StopSequence, PatternStats> sequenceStats = getStatsForStopSequences(sequences);
      Map<String, List<StopSequence>> sequenceGroups = getGroupsForStopSequences(sequences);

      go(route, sequenceStats, sequenceGroups);
    }
  }

  /**
   * Remove stop sequences from a list that do not contain any stops
   * 
   * @param stopSequences
   */
  private void pruneEmptyStopSequences(List<StopSequence> stopSequences) {
    for (Iterator<StopSequence> it = stopSequences.iterator(); it.hasNext();) {
      StopSequence st = it.next();
      if (st.getStops().isEmpty())
        it.remove();
    }
  }

  /**
   * Computes some general statistics for each {@link StopSequence} in a
   * collection, including the number of trips taking that stop sequence, the
   * set of {@link Region} regions for the destination of the stop sequence
   * 
   * @param sequences
   * @return the computed statistics
   */
  private Map<StopSequence, PatternStats> getStatsForStopSequences(List<StopSequence> sequences) {

    Map<StopSequence, PatternStats> patternStats = new HashMap<StopSequence, PatternStats>();

    for (StopSequence sequence : sequences) {
      PatternStats stats = new PatternStats();
      stats.tripCounts = sequence.getTripCount();
      stats.regions = getRegionsForLastStopInSequence(sequence);
      stats.segment = getSegmentForStopSequence(sequence);
      patternStats.put(sequence, stats);
    }

    pruneCommonLayersFromStats(patternStats.values());

    return patternStats;
  }

  /**
   * For the specified {@link StopSequence}, determine the set of
   * {@link Regions} containing the last stop in the sequence. If the last stop
   * in the sequence is not contained by any regions, then the previous stop is
   * examined in turn until a stop is found which is contained by a region.
   * 
   * @param sequence
   * @return the regions in a SortedMap keyed by the {@link Layer} for each
   *         region
   */
  private SortedMap<Layer, Region> getRegionsForLastStopInSequence(StopSequence sequence) {

    List<Stop> stops = sequence.getStops();

    for (int index = stops.size() - 1; index >= 0; index--) {
      Stop stop = stops.get(index);
      SortedMap<Layer, Region> regions = _whereDao.getRegionsByStop(stop);
      if (!regions.isEmpty())
        return regions;
    }
    throw new IllegalStateException("no regions?");
  }

  /**
   * Compute a {@link Segment} object for the specified {@link StopSequence}. A
   * Segment generally captures the start and end location of the stop sequence,
   * along with the sequence's total length.
   * 
   * @param pattern
   * @return
   */
  private Segment getSegmentForStopSequence(StopSequence pattern) {

    Segment segment = new Segment();

    List<Stop> stops = pattern.getStops();
    Stop prev = null;

    for (Stop stop : stops) {
      if (prev == null) {
        segment.from = stop.getLocation();
        segment.fromLat = stop.getLat();
        segment.fromLon = stop.getLon();
      } else {
        segment.distance += UtilityLibrary.distance(prev.getLocation(), stop.getLocation());
      }
      segment.to = stop.getLocation();
      segment.toLat = stop.getLat();
      segment.toLon = stop.getLon();
      prev = stop;
    }

    return segment;
  }

  /**
   * For a set of PatternStats, each containing a map of Layers and Regions, we
   * prune all layers where the layer is present in each PatternStats map and
   * the region is the same across all layers.
   * 
   * @param patternStats
   */
  private void pruneCommonLayersFromStats(Iterable<PatternStats> patternStats) {

    while (true) {

      Layer currentLayer = null;
      Region currentRegion = null;

      for (PatternStats stats : patternStats) {
        SortedMap<Layer, Region> regions = stats.regions;

        if (regions.isEmpty())
          throw new IllegalStateException("bad");
        if (regions.size() == 1)
          return;
        Layer layer = regions.firstKey();
        Region region = regions.get(layer);

        if (currentLayer == null) {
          currentLayer = layer;
          currentRegion = region;
        } else if (!layer.equals(currentLayer) || !region.equals(currentRegion))
          return;
      }

      // If we made it this far, then everyone has the same first layer and
      // region
      // System.out.println("removing common region: " +
      // currentRegion.getName());

      for (PatternStats stats : patternStats) {
        SortedMap<Layer, Region> regions = stats.regions;
        regions.remove(currentLayer);
      }
    }
  }

  /**
   * Group StopSequences by common direction. If all the stopSequences have a
   * direction id, then we use that to do the grouping. Otherwise...
   * 
   * @param sequences
   * 
   * @return
   */
  private Map<String, List<StopSequence>> getGroupsForStopSequences(List<StopSequence> sequences) {

    boolean allSequencesHaveDirectionId = true;

    for (StopSequence sequence : sequences) {
      if (sequence.getDirectionId() == null)
        allSequencesHaveDirectionId = false;
    }

    if (allSequencesHaveDirectionId)
      return groupStopSequencesByDirectionIds(sequences);

    return groupStopSequencesByNotDirectionIds(sequences);
  }

  /**
   * Group the StopSequences by their direction ids.
   * 
   * @param sequences
   * @return
   */
  private Map<String, List<StopSequence>> groupStopSequencesByDirectionIds(Iterable<StopSequence> sequences) {

    Map<String, List<StopSequence>> groups = new FactoryMap<String, List<StopSequence>>(new ArrayList<StopSequence>());

    for (StopSequence sequence : sequences) {
      String directionId = sequence.getDirectionId();
      groups.get(directionId).add(sequence);
    }

    return groups;
  }

  public Map<String, List<StopSequence>> groupStopSequencesByNotDirectionIds(Iterable<StopSequence> sequences) {
    throw new UnsupportedOperationException("implement me");
  }

  /**
   * 
   * @param route
   * @param sequenceStats
   * @param sequencesByStopSequenceBlockId
   */
  private void go(Route route, Map<StopSequence, PatternStats> sequenceStats,
      Map<String, List<StopSequence>> sequencesByStopSequenceBlockId) {

    computeContinuations(route, sequenceStats, sequencesByStopSequenceBlockId);

    Set<String> allNames = new HashSet<String>();
    Map<String, String> directionToName = new HashMap<String, String>();
    Map<String, Segment> segments = new HashMap<String, Segment>();

    for (Map.Entry<String, List<StopSequence>> entry : sequencesByStopSequenceBlockId.entrySet()) {

      String direction = entry.getKey();
      List<StopSequence> patterns = entry.getValue();

      System.out.println("direction=" + direction);
      for (StopSequence sequence : patterns) {
        PatternStats stats = sequenceStats.get(sequence);
        System.out.println("  " + stats.regions);
        System.out.println("  " + stats.segment.from);
        System.out.println("  " + stats.segment.to);
      }

      Max<StopSequence> maxTripCount = new Max<StopSequence>();

      for (StopSequence pattern : patterns)
        maxTripCount.add(pattern.getTripCount(), pattern);

      RecursiveStats rs = new RecursiveStats();
      rs.maxTripCount = (long) maxTripCount.getMaxValue();
      rs.graph = new Graph<SortedMap<Layer, Region>>();

      exploreStopSequences(rs, sequenceStats, patterns, "");

      for (SortedMap<Layer, Region> m : rs.graph.getNodes()) {
        System.out.println("node = " + m);
        for (SortedMap<Layer, Region> mOut : rs.graph.getOutboundNodes(m))
          System.out.println("  " + mOut);
      }
      List<SortedMap<Layer, Region>> names = rs.graph.getTopologicalSort(null);
      String dName = getRegionsAsName(names);
      allNames.add(dName);
      directionToName.put(direction, dName);

      segments.put(direction, rs.longestSegment.getMaxElement());
    }

    if (allNames.size() < directionToName.size()) {
      for (Map.Entry<String, String> entry : directionToName.entrySet()) {
        String direction = entry.getKey();
        String name = entry.getValue();
        direction = direction.charAt(0) + direction.substring(1).toLowerCase();
        entry.setValue(name + " - " + direction);
      }
    }

    for (Map.Entry<String, String> entry : directionToName.entrySet()) {

      String direction = entry.getKey();
      String name = entry.getValue();
      List<StopSequence> patterns = sequencesByStopSequenceBlockId.get(direction);

      Segment segment = segments.get(direction);

      System.out.println("  " + direction + " => " + name);
      StopSequenceBlock block = new StopSequenceBlock();

      if (segment.fromLat == 0.0)
        throw new IllegalStateException("what?");

      StopSequenceBlockKey key = new StopSequenceBlockKey(route, direction);
      block.setId(key);
      block.setDescription(name);
      block.setStopSequences(patterns);
      block.setStartLocation(segment.from);
      block.setStartLat(segment.fromLat);
      block.setStartLon(segment.fromLon);
      block.setEndLocation(segment.to);
      block.setEndLat(segment.toLat);
      block.setEndLon(segment.toLon);

      _whereDao.save(block);
    }
  }

  /**
   * For each given StopSequence, we wish to compute the set of StopSequences
   * that continue the given StopSequence. We say one StopSequence continues
   * another if the two stops sequences have the same route and direction id and
   * each trip in the first StopSequence is immediately followed by a Trip from
   * the second StopSequence, as defined by a block id.
   * 
   * @param route
   * @param sequenceStats
   * @param sequencesByStopSequenceBlockId
   */
  private void computeContinuations(Route route, Map<StopSequence, PatternStats> sequenceStats,
      Map<String, List<StopSequence>> sequencesByStopSequenceBlockId) {

    Set<Trip> trips = new HashSet<Trip>();
    Map<Trip, StopSequence> stopSequencesByTrip = new HashMap<Trip, StopSequence>();
    Map<String, List<Trip>> blockTripsByBlockId = new FactoryMap<String, List<Trip>>(new ArrayList<Trip>());

    Map<StopSequence, String> stopSequenceBlockIds = new HashMap<StopSequence, String>();
    for (Map.Entry<String, List<StopSequence>> entry : sequencesByStopSequenceBlockId.entrySet()) {
      String id = entry.getKey();
      for (StopSequence sequence : entry.getValue())
        stopSequenceBlockIds.put(sequence, id);
    }

    for (StopSequence sequence : sequenceStats.keySet()) {
      for (Trip trip : sequence.getTrips()) {
        if (trip.getBlockId() != null) {
          if (trips.add(trip)) {
            blockTripsByBlockId.get(trip.getBlockId()).add(trip);
            stopSequencesByTrip.put(trip, sequence);
          }
        }
      }
    }

    // We can use the first stop time for a trip for ordering trips in the same
    // block
    Map<Trip, StopTime> firstStopTimes = _gtfsDao.getFirstStopTimesByTrips(trips);

    BlockComparator compareByFirstStopTime = new BlockComparator(firstStopTimes);

    for (List<Trip> tripsInBlock : blockTripsByBlockId.values()) {

      Collections.sort(tripsInBlock, compareByFirstStopTime);

      StopSequence prevSequence = null;
      String prevGroupId = null;

      for (Trip trip : tripsInBlock) {

        StopSequence stopSequence = stopSequencesByTrip.get(trip);
        String groupId = stopSequenceBlockIds.get(stopSequence);

        if (prevSequence != null) {
          if (!prevSequence.equals(stopSequence) && groupId.equals(prevGroupId)) {
            Stop prevStop = prevSequence.getStops().get(prevSequence.getStops().size() - 1);
            Stop nextStop = stopSequence.getStops().get(0);
            double d = UtilityLibrary.distance(prevStop.getLocation(), nextStop.getLocation());
            if (d < 5280 / 4) {
              System.out.println("distance=" + d + " from=" + prevStop.getId() + " to=" + nextStop.getId() + " ssFrom="
                  + prevSequence.getId() + " ssTo=" + stopSequence.getId());
              PatternStats stats = sequenceStats.get(prevSequence);
              stats.continuations.add(stopSequence);
            }
          }
        }
        prevSequence = stopSequence;
        prevGroupId = groupId;
      }
    }
  }

  private void exploreStopSequences(RecursiveStats rs, Map<StopSequence, PatternStats> patternStats,
      Iterable<StopSequence> patterns, String depth) {

    SortedMap<Layer, Region> prevRegions = rs.prevRegions;
    Segment prevSegment = rs.prevSegment;

    for (StopSequence pattern : patterns) {

      if (rs.visited.contains(pattern))
        continue;

      System.out.println(depth + "id=" + pattern.getId() + " from=" + prevRegions);

      PatternStats stats = patternStats.get(pattern);

      double count = stats.tripCounts;
      double ratio = count / rs.maxTripCount;

      if (ratio < SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN)
        continue;

      Segment segment = stats.segment;

      if (prevSegment != null)
        segment = new Segment(prevSegment, segment, prevSegment.distance + segment.distance);

      rs.longestSegment.add(segment.distance, segment);

      rs.graph.addNode(stats.regions);

      if (prevRegions != null && !prevRegions.equals(stats.regions)) {
        System.out.println(depth + "adding edge id=" + pattern.getId() + " from=" + prevRegions + " to="
            + stats.regions);
        rs.graph.addEdge(prevRegions, stats.regions);
      }

      Set<StopSequence> nextPatterns = stats.continuations;

      if (!nextPatterns.isEmpty()) {
        rs.visited.add(pattern);
        rs.prevSegment = segment;
        rs.prevRegions = stats.regions;
        exploreStopSequences(rs, patternStats, nextPatterns, depth + "  ");
        rs.visited.remove(pattern);
      }
    }
  }

  private String getRegionsAsName(List<SortedMap<Layer, Region>> regions) {

    StringBuilder b = new StringBuilder();

    while (!regions.isEmpty()) {

      Layer prevLayer = null;
      Region prevRegion = null;

      for (SortedMap<Layer, Region> go : regions) {

        if (go.isEmpty())
          continue;

        Layer nextLayer = go.firstKey();
        Region nextRegion = go.get(nextLayer);

        if (prevLayer == null) {
          prevLayer = nextLayer;
          prevRegion = nextRegion;
          if (b.length() > 0)
            b.append(", ");
          b.append(nextRegion.getName());
        }

        if (prevLayer.equals(nextLayer) && prevRegion.equals(nextRegion)) {
          go.remove(prevLayer);
        } else {
          break;
        }
      }

      if (prevLayer == null)
        break;
    }

    return b.toString();
  }

  private static class BlockComparator implements Comparator<Trip> {

    Map<Trip, StopTime> _tripToStopTimes = new HashMap<Trip, StopTime>();

    public BlockComparator(Map<Trip, StopTime> tripToStopTimes) {
      _tripToStopTimes = tripToStopTimes;
    }

    public int compare(Trip o1, Trip o2) {
      StopTime st1 = _tripToStopTimes.get(o1);
      StopTime st2 = _tripToStopTimes.get(o2);
      int t1 = st1.getDepartureTime();
      int t2 = st2.getDepartureTime();
      return t1 == t2 ? 0 : (t1 < t2 ? -1 : 1);
    }
  }

  private static class PatternStats {
    long tripCounts;
    Segment segment;
    SortedMap<Layer, Region> regions;
    Set<StopSequence> continuations = new HashSet<StopSequence>();
  }

  private static class RecursiveStats {
    Graph<SortedMap<Layer, Region>> graph;
    Max<Segment> longestSegment = new Max<Segment>();
    Set<StopSequence> visited = new HashSet<StopSequence>();
    long maxTripCount;
    SortedMap<Layer, Region> prevRegions;
    Segment prevSegment;

  }

  private static class Segment {

    Point from;
    double fromLon;
    double fromLat;
    Point to;
    double toLon;
    double toLat;
    double distance;

    public Segment() {

    }

    public Segment(Segment prevSegment, Segment toSegment, double d) {
      this.from = prevSegment.from;
      this.fromLat = prevSegment.fromLat;
      this.fromLon = prevSegment.fromLon;
      this.to = toSegment.to;
      this.toLat = toSegment.toLat;
      this.toLon = toSegment.toLon;
      this.distance = d;
    }
  }
}
