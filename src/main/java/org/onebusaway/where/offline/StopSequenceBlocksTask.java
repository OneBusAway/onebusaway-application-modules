package org.onebusaway.where.offline;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.Max;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.graph.Graph;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.model.StopSequenceBlock;
import org.onebusaway.where.model.StopSequenceBlockKey;
import org.onebusaway.where.services.WhereDao;
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

@Component
public class StopSequenceBlocksTask implements Runnable {

  private static final double SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN = 0.2;

  @Autowired
  private GtdfDao _gtdfDao;

  @Autowired
  private WhereDao _whereDao;

  private List<Route> getRoutes() {

    if (true)
      return _gtdfDao.getAllRoutes();

    List<Route> routes = new ArrayList<Route>();
    routes.add(_gtdfDao.getRouteByShortName("30"));
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

      Map<StopSequence, PatternStats> patternStats = new HashMap<StopSequence, PatternStats>();
      getStatsForSequences(sequences, patternStats);

      boolean allSequencesHaveDirectionId = true;

      for (StopSequence sequence : sequences) {
        if (sequence.getDirectionId() == null)
          allSequencesHaveDirectionId = false;
      }

      if (allSequencesHaveDirectionId) {
        handleRouteWithDirectionIds(route, patternStats);
      } else {
        handleRouteWithoutDirectionIds(route, patternStats);
      }
    }
  }

  private void pruneEmptyStopSequences(List<StopSequence> stopSequences) {
    for (Iterator<StopSequence> it = stopSequences.iterator(); it.hasNext();) {
      StopSequence st = it.next();
      if (st.getStops().isEmpty())
        it.remove();
    }
  }

  private void getStatsForSequences(List<StopSequence> sequences,
      Map<StopSequence, PatternStats> patternStats) {
    for (StopSequence sequence : sequences) {
      PatternStats stats = new PatternStats();
      stats.tripCounts = sequence.getTripCount();
      stats.regions = getRegions(sequence);
      stats.segment = getSegment(sequence);
      patternStats.put(sequence, stats);
    }
  }

  private void handleRouteWithDirectionIds(Route route,
      Map<StopSequence, PatternStats> patternStats) {

    Map<String, List<StopSequence>> groups = new FactoryMap<String, List<StopSequence>>(
        new ArrayList<StopSequence>());

    for (StopSequence sequence : patternStats.keySet()) {
      String directionId = sequence.getDirectionId();
      groups.get(directionId).add(sequence);
    }

    go(route, patternStats, groups);
  }

  public void handleRouteWithoutDirectionIds(Route route,
      Map<StopSequence, PatternStats> patternStats) {
    throw new UnsupportedOperationException("implement me");
  }

  private void go(Route route, Map<StopSequence, PatternStats> sequenceStats,
      Map<String, List<StopSequence>> sequencesByStopSequenceBlockId) {

    Map<Trip, StopTime> firstStopTimes = _gtdfDao.getFirstStopTimesByRoute(route);

    pruneCommonLayers(sequenceStats);

    Map<Trip, String> stopSequenceBlockIdsByTrip = new HashMap<Trip, String>();
    Map<Trip, StopSequence> stopSequencesByTrip = new HashMap<Trip, StopSequence>();

    for (Map.Entry<String, List<StopSequence>> entry : sequencesByStopSequenceBlockId.entrySet()) {
      String stopSequenceBlockId = entry.getKey();
      for (StopSequence sequence : entry.getValue()) {
        for (Trip trip : sequence.getTrips()) {
          stopSequenceBlockIdsByTrip.put(trip, stopSequenceBlockId);
          stopSequencesByTrip.put(trip, sequence);
        }
      }
    }

    for (StopSequence sequence : sequenceStats.keySet()) {

      List<Trip> trips = sequence.getTrips();

      Set<String> blockIds = new HashSet<String>();
      for (Trip trip : trips) {
        if (trip.getBlockId() != null)
          blockIds.add(trip.getBlockId());
      }

      List<Trip> blockTrips = _gtdfDao.getTripsByBlockId(blockIds);

      Map<String, List<Trip>> blockTripsByBlockId = CollectionsLibrary.mapToValueList(
          blockTrips, "blockId", String.class);

      for (List<Trip> tripsInBlock : blockTripsByBlockId.values()) {

        Map<Trip, StopTime> tripAndFirstStopTime = new HashMap<Trip, StopTime>();
        for (Trip trip : tripsInBlock) {
          StopTime stopTime = firstStopTimes.get(trip);
          if (stopTime != null)
            tripAndFirstStopTime.put(trip, stopTime);
        }

        tripsInBlock = new ArrayList<Trip>(tripAndFirstStopTime.keySet());
        Collections.sort(tripsInBlock,
            new BlockComparator(tripAndFirstStopTime));

        String prevStopSequenceBlockId = null;
        StopSequence prevSequence = null;

        for (Trip trip : tripsInBlock) {
          String stopSequenceBlockId = stopSequenceBlockIdsByTrip.get(trip);
          StopSequence stopSequence = stopSequencesByTrip.get(trip);

          if (prevStopSequenceBlockId != null && stopSequenceBlockId != null
              && prevStopSequenceBlockId.equals(stopSequenceBlockId)) {
            PatternStats stats = sequenceStats.get(prevSequence);
            stats.continuations.add(stopSequence);
          }
          prevStopSequenceBlockId = stopSequenceBlockId;
          prevSequence = stopSequence;
        }
      }
    }

    Set<String> allNames = new HashSet<String>();
    Map<String, String> directionToName = new HashMap<String, String>();
    Map<String, Segment> segments = new HashMap<String, Segment>();

    for (Map.Entry<String, List<StopSequence>> entry : sequencesByStopSequenceBlockId.entrySet()) {

      String direction = entry.getKey();
      List<StopSequence> patterns = entry.getValue();

      Max<StopSequence> maxTripCount = new Max<StopSequence>();

      for (StopSequence pattern : patterns)
        maxTripCount.add(pattern.getTripCount(), pattern);

      RecursiveStats rs = new RecursiveStats();
      rs.maxTripCount = (long) maxTripCount.getMaxValue();
      rs.graph = new Graph<SortedMap<Layer, Region>>();

      exploreStopSequences(rs, sequenceStats, patterns);

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

  private Segment getSegment(StopSequence pattern) {

    Segment segment = new Segment();

    List<Stop> stops = pattern.getStops();
    Stop prev = null;

    for (Stop stop : stops) {
      if (prev == null) {
        segment.from = stop.getLocation();
        segment.fromLat = stop.getLat();
        segment.fromLon = stop.getLon();
      } else {
        segment.distance += UtilityLibrary.distance(prev.getLocation(),
            stop.getLocation());
      }
      segment.to = stop.getLocation();
      segment.toLat = stop.getLat();
      segment.toLon = stop.getLon();
      prev = stop;
    }

    return segment;
  }

  private void exploreStopSequences(RecursiveStats rs,
      Map<StopSequence, PatternStats> patternStats,
      Iterable<StopSequence> patterns) {

    SortedMap<Layer, Region> prevRegions = rs.prevRegions;
    Segment prevSegment = rs.prevSegment;

    for (StopSequence pattern : patterns) {

      if (rs.visited.contains(pattern))
        continue;

      PatternStats stats = patternStats.get(pattern);

      double count = stats.tripCounts;
      double ratio = count / rs.maxTripCount;

      if (ratio < SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN)
        continue;

      Segment segment = stats.segment;

      if (prevSegment != null)
        segment = new Segment(prevSegment, segment, prevSegment.distance
            + segment.distance);

      rs.longestSegment.add(segment.distance, segment);

      rs.graph.addNode(stats.regions);

      if (prevRegions != null && !prevRegions.equals(stats.regions))
        rs.graph.addEdge(prevRegions, stats.regions);

      Set<StopSequence> nextPatterns = stats.continuations;

      if (!nextPatterns.isEmpty()) {
        rs.visited.add(pattern);
        rs.prevSegment = segment;
        rs.prevRegions = stats.regions;
        exploreStopSequences(rs, patternStats, nextPatterns);
        rs.visited.remove(pattern);
      }
    }
  }

  private void pruneCommonLayers(Map<StopSequence, PatternStats> patternStats) {

    while (true) {

      Layer currentLayer = null;
      Region currentRegion = null;

      for (PatternStats stats : patternStats.values()) {
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

      for (PatternStats stats : patternStats.values()) {
        SortedMap<Layer, Region> regions = stats.regions;
        regions.remove(currentLayer);
      }
    }
  }

  private SortedMap<Layer, Region> getRegions(StopSequence sequence) {

    List<Stop> stops = sequence.getStops();

    for (int index = stops.size() - 1; index >= 0; index--) {
      Stop stop = stops.get(index);
      SortedMap<Layer, Region> regions = _whereDao.getRegionsByStop(stop);
      if (!regions.isEmpty())
        return regions;
    }
    throw new IllegalStateException("no regions?");
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
