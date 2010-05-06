package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.impl.tripplanner.DistanceLibrary;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceBlock;
import org.onebusaway.transit_data_federation.model.StopSequenceBlockKey;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.same.TreeUnionFind;
import edu.washington.cs.rse.collections.stats.Counter;
import edu.washington.cs.rse.collections.stats.Max;
import edu.washington.cs.rse.collections.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Construct a set of {@link StopSequenceBlock} blocks for each route. A block
 * contains a set of {@link StopSequence} sequences that are headed in the same
 * direction for a particular route, along with a general description of the
 * destinations for those stop sequences and general start and stop locations
 * for the sequences.
 * 
 * @author bdferris
 */
public class StopSequenceBlocksLibrary {

  private static final double SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN = 0.2;

  private static final double STOP_SEQUENCE_MIN_COMMON_RATIO = 0.3;

  public List<StopSequenceBlock> getStopSequencesAsBlocks(List<StopSequence> sequences) {

    pruneEmptyStopSequences(sequences);

    if (sequences.isEmpty())
      throw new IllegalStateException("no stops in sequences");

    Map<StopSequence, PatternStats> sequenceStats = getStatsForStopSequences(sequences);
    Map<String, List<StopSequence>> sequenceGroups = getGroupsForStopSequences(sequences);

    return constructBlocks(sequenceStats, sequenceGroups);
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
   * set of regions for the destination of the stop sequence
   * 
   * @param sequences
   * @return the computed statistics
   */
  private Map<StopSequence, PatternStats> getStatsForStopSequences(List<StopSequence> sequences) {

    Map<StopSequence, PatternStats> patternStats = new HashMap<StopSequence, PatternStats>();

    for (StopSequence sequence : sequences) {
      PatternStats stats = new PatternStats();
      stats.tripCounts = sequence.getTripCount();
      stats.tripHeadsignCounts = getTripHeadsignCountsForSequence(sequence);
      stats.segment = getSegmentForStopSequence(sequence);
      patternStats.put(sequence, stats);
    }

    return patternStats;
  }

  private Counter<String> getTripHeadsignCountsForSequence(StopSequence sequence) {
    Counter<String> counts = new Counter<String>();
    for (Trip trip : sequence.getTrips()) {
      String headSign = trip.getTripHeadsign();
      if (headSign != null && headSign.length() > 0)
        counts.increment(headSign);
    }
    return counts;
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
        segment.fromLat = stop.getLat();
        segment.fromLon = stop.getLon();
      } else {
        segment.distance += DistanceLibrary.distance(prev, stop);
      }
      segment.toLat = stop.getLat();
      segment.toLon = stop.getLon();
      prev = stop;
    }

    return segment;
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

    if (allSequencesHaveDirectionId) {
      Map<String, List<StopSequence>> result = groupStopSequencesByDirectionIds(sequences);
      if (result.size() > 1)
        return result;
    }

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

    TreeUnionFind<StopSequence> unionFind = new TreeUnionFind<StopSequence>();

    for (StopSequence stopSequenceA : sequences) {

      unionFind.find(stopSequenceA);

      for (StopSequence stopSequenceB : sequences) {
        if (stopSequenceA == stopSequenceB)
          continue;
        double ratio = getMaxCommonStopSequenceRatio(stopSequenceA, stopSequenceB);
        if (ratio >= STOP_SEQUENCE_MIN_COMMON_RATIO)
          unionFind.union(stopSequenceA, stopSequenceB);

      }
    }

    Map<String, List<StopSequence>> results = new HashMap<String, List<StopSequence>>();
    int index = 0;

    for (Set<StopSequence> sequencesByDirection : unionFind.getSetMembers()) {
      String key = Integer.toString(index);
      List<StopSequence> asList = new ArrayList<StopSequence>(sequencesByDirection);
      results.put(key, asList);
      index++;
    }

    return results;
  }

  /**
   * 
   * @param route
   * @param sequenceStats
   * @param sequencesByStopSequenceBlockId
   * @return
   */
  private List<StopSequenceBlock> constructBlocks(Map<StopSequence, PatternStats> sequenceStats,
      Map<String, List<StopSequence>> sequencesByStopSequenceBlockId) {

    computeContinuations(sequenceStats, sequencesByStopSequenceBlockId);

    Set<String> allNames = new HashSet<String>();
    Map<String, String> directionToName = new HashMap<String, String>();
    Map<String, Segment> segments = new HashMap<String, Segment>();

    for (Map.Entry<String, List<StopSequence>> entry : sequencesByStopSequenceBlockId.entrySet()) {

      String direction = entry.getKey();
      List<StopSequence> sequences = entry.getValue();

      Max<StopSequence> maxTripCount = new Max<StopSequence>();

      Counter<String> names = new Counter<String>();

      for (StopSequence sequence : sequences) {
        maxTripCount.add(sequence.getTripCount(), sequence);
        for (Trip trip : sequence.getTrips()) {
          String headsign = trip.getTripHeadsign();
          if (headsign != null && headsign.length() > 0)
            names.increment(headsign);
        }
      }

      String dName = names.getMax();

      RecursiveStats rs = new RecursiveStats();
      rs.maxTripCount = (long) maxTripCount.getMaxValue();

      exploreStopSequences(rs, sequenceStats, sequences, "");

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

    List<StopSequenceBlock> blocks = new ArrayList<StopSequenceBlock>();

    for (Map.Entry<String, String> entry : directionToName.entrySet()) {

      String direction = entry.getKey();
      String name = entry.getValue();
      List<StopSequence> patterns = sequencesByStopSequenceBlockId.get(direction);

      Segment segment = segments.get(direction);

      // System.out.println("  " + direction + " => " + name);
      StopSequenceBlock block = new StopSequenceBlock();

      if (segment.fromLat == 0.0)
        throw new IllegalStateException("what?");

      StopSequenceBlockKey key = new StopSequenceBlockKey(null, direction);
      block.setId(key);
      block.setDescription(name);
      block.setStopSequences(patterns);
      block.setStartLat(segment.fromLat);
      block.setStartLon(segment.fromLon);
      block.setEndLat(segment.toLat);
      block.setEndLon(segment.toLon);

      blocks.add(block);
    }

    return blocks;
  }

  /**
   * For each given StopSequence, we wish to compute the set of StopSequences
   * that continue the given StopSequence. We say one StopSequence continues
   * another if the two stops sequences have the same route and direction id and
   * each trip in the first StopSequence is immediately followed by a Trip from
   * the second StopSequence, as defined by a block id.
   * 
   * @param sequenceStats
   * @param sequencesByStopSequenceBlockId
   */
  private void computeContinuations(Map<StopSequence, PatternStats> sequenceStats,
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

    BlockComparator compareByBlockSequenceId = new BlockComparator();

    for (List<Trip> tripsInBlock : blockTripsByBlockId.values()) {

      Collections.sort(tripsInBlock, compareByBlockSequenceId);

      StopSequence prevSequence = null;
      String prevGroupId = null;

      for (Trip trip : tripsInBlock) {

        StopSequence stopSequence = stopSequencesByTrip.get(trip);
        String groupId = stopSequenceBlockIds.get(stopSequence);

        if (prevSequence != null) {
          if (!prevSequence.equals(stopSequence) && groupId.equals(prevGroupId)) {
            Stop prevStop = prevSequence.getStops().get(prevSequence.getStops().size() - 1);
            Stop nextStop = stopSequence.getStops().get(0);
            double d = DistanceLibrary.distance(prevStop, nextStop);
            if (d < 5280 / 4) {
              /*
               * System.out.println("distance=" + d + " from=" +
               * prevStop.getId() + " to=" + nextStop.getId() + " ssFrom=" +
               * prevSequence.getId() + " ssTo=" + stopSequence.getId());
               */
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
        segment = new Segment(prevSegment, segment, prevSegment.distance + segment.distance);

      rs.longestSegment.add(segment.distance, segment);

      Set<StopSequence> nextPatterns = stats.continuations;

      if (!nextPatterns.isEmpty()) {
        rs.visited.add(pattern);
        rs.prevSegment = segment;
        exploreStopSequences(rs, patternStats, nextPatterns, depth + "  ");
        rs.visited.remove(pattern);
      }
    }
  }

  private double getMaxCommonStopSequenceRatio(StopSequence a, StopSequence b) {
    Set<Pair<Stop>> pairsA = getStopSequenceAsStopPairSet(a);
    Set<Pair<Stop>> pairsB = getStopSequenceAsStopPairSet(b);
    int common = 0;
    for (Pair<Stop> pairA : pairsA) {
      if (pairsB.contains(pairA))
        common++;
    }

    double ratioA = ((double) common) / pairsA.size();
    double ratioB = ((double) common) / pairsB.size();
    return Math.max(ratioA, ratioB);
  }

  private Set<Pair<Stop>> getStopSequenceAsStopPairSet(StopSequence stopSequence) {
    Set<Pair<Stop>> pairs = new HashSet<Pair<Stop>>();
    Stop prev = null;
    for (Stop stop : stopSequence.getStops()) {
      if (prev != null) {
        Pair<Stop> pair = Pair.createPair(prev, stop);
        pairs.add(pair);
      }
      prev = stop;
    }
    return pairs;
  }

  private static class BlockComparator implements Comparator<Trip> {

    public BlockComparator() {

    }

    public int compare(Trip o1, Trip o2) {
      return o2.getBlockSequenceId() - o1.getBlockSequenceId();
    }
  }

  private static class PatternStats {
    long tripCounts;
    Segment segment;
    Set<StopSequence> continuations = new HashSet<StopSequence>();
    Counter<String> tripHeadsignCounts;
  }

  private static class RecursiveStats {
    Max<Segment> longestSegment = new Max<Segment>();
    Set<StopSequence> visited = new HashSet<StopSequence>();
    long maxTripCount;
    Segment prevSegment;
  }

  private static class Segment {

    double fromLon;
    double fromLat;
    double toLon;
    double toLat;
    double distance;

    public Segment() {

    }

    public Segment(Segment prevSegment, Segment toSegment, double d) {
      this.fromLat = prevSegment.fromLat;
      this.fromLon = prevSegment.fromLon;
      this.toLat = toSegment.toLat;
      this.toLon = toSegment.toLon;
      this.distance = d;
    }
  }
}
