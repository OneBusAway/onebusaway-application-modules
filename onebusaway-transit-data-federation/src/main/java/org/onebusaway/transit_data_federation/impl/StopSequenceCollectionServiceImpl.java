/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Max;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceCollection;
import org.onebusaway.transit_data_federation.model.StopSequenceCollectionKey;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.StopSequenceCollectionService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.collections.TreeUnionFind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Construct a set of {@link StopSequenceCollection} collection for each route.
 * A collection contains a set of {@link StopSequence} sequences that are headed
 * in the same direction for a particular route, along with a general
 * description of the destinations for those stop sequences and general start
 * and stop locations for the sequences.
 * 
 * @author bdferris
 */
@Component
public class StopSequenceCollectionServiceImpl implements
    StopSequenceCollectionService {

  private static final double SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN = 0.2;

  private static final double STOP_SEQUENCE_MIN_COMMON_RATIO = 0.3;

  private NarrativeService _narrativeService;

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  public List<StopSequenceCollection> getStopSequencesAsCollections(
      List<StopSequence> sequences) {

    pruneEmptyStopSequences(sequences);

    if (sequences.isEmpty())
      return new ArrayList<StopSequenceCollection>();

    Map<StopSequence, PatternStats> sequenceStats = getStatsForStopSequences(sequences);
    Map<String, List<StopSequence>> sequenceGroups = getGroupsForStopSequences(sequences);

    return constructCollections(sequenceStats, sequenceGroups);
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
  private Map<StopSequence, PatternStats> getStatsForStopSequences(
      List<StopSequence> sequences) {

    Map<StopSequence, PatternStats> patternStats = new HashMap<StopSequence, PatternStats>();

    for (StopSequence sequence : sequences) {
      PatternStats stats = new PatternStats();
      stats.tripCounts = sequence.getTripCount();
      stats.segment = getSegmentForStopSequence(sequence);
      patternStats.put(sequence, stats);
    }

    return patternStats;
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

    List<StopEntry> stops = pattern.getStops();
    StopEntry prev = null;

    for (StopEntry stop : stops) {
      if (prev == null) {
        segment.fromLat = stop.getStopLat();
        segment.fromLon = stop.getStopLon();
      } else {
        segment.distance += SphericalGeometryLibrary.distance(
            prev.getStopLat(), prev.getStopLon(), stop.getStopLat(),
            stop.getStopLon());
      }
      segment.toLat = stop.getStopLat();
      segment.toLon = stop.getStopLon();
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
  private Map<String, List<StopSequence>> getGroupsForStopSequences(
      List<StopSequence> sequences) {

    boolean allSequencesHaveDirectionId = true;

    for (StopSequence sequence : sequences) {
      if (sequence.getDirectionId() == null)
        allSequencesHaveDirectionId = false;
    }

    if (allSequencesHaveDirectionId) {
      Map<String, List<StopSequence>> result = groupStopSequencesByDirectionIds(sequences);
      if (result.size() > 0)
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
  private Map<String, List<StopSequence>> groupStopSequencesByDirectionIds(
      Iterable<StopSequence> sequences) {

    Map<String, List<StopSequence>> groups = new FactoryMap<String, List<StopSequence>>(
        new ArrayList<StopSequence>());

    for (StopSequence sequence : sequences) {
      String directionId = sequence.getDirectionId();
      groups.get(directionId).add(sequence);
    }

    return groups;
  }

  private Map<String, List<StopSequence>> groupStopSequencesByNotDirectionIds(
      Iterable<StopSequence> sequences) {

    TreeUnionFind<StopSequence> unionFind = new TreeUnionFind<StopSequence>();

    for (StopSequence stopSequenceA : sequences) {

      unionFind.find(stopSequenceA);

      for (StopSequence stopSequenceB : sequences) {
        if (stopSequenceA == stopSequenceB)
          continue;
        double ratio = getMaxCommonStopSequenceRatio(stopSequenceA,
            stopSequenceB);
        if (ratio >= STOP_SEQUENCE_MIN_COMMON_RATIO)
          unionFind.union(stopSequenceA, stopSequenceB);

      }
    }

    Map<String, List<StopSequence>> results = new HashMap<String, List<StopSequence>>();
    int index = 0;

    for (Set<StopSequence> sequencesByDirection : unionFind.getSetMembers()) {
      String key = Integer.toString(index);
      List<StopSequence> asList = new ArrayList<StopSequence>(
          sequencesByDirection);
      results.put(key, asList);
      index++;
    }

    return results;
  }

  /**
   * 
   * @param route
   * @param sequenceStats
   * @param sequencesByGroupId
   * @return
   */
  private List<StopSequenceCollection> constructCollections(
      Map<StopSequence, PatternStats> sequenceStats,
      Map<String, List<StopSequence>> sequencesByGroupId) {

    computeContinuations(sequenceStats, sequencesByGroupId);

    Set<String> allNames = new HashSet<String>();
    Map<String, String> directionToName = new HashMap<String, String>();
    Map<String, Segment> segments = new HashMap<String, Segment>();

    for (Map.Entry<String, List<StopSequence>> entry : sequencesByGroupId.entrySet()) {

      String direction = entry.getKey();
      List<StopSequence> sequences = entry.getValue();

      Max<StopSequence> maxTripCount = new Max<StopSequence>();

      Counter<String> names = new Counter<String>();

      for (StopSequence sequence : sequences) {
        maxTripCount.add(sequence.getTripCount(), sequence);

        for (BlockTripEntry blockTrip : sequence.getTrips()) {
          TripEntry trip = blockTrip.getTrip();
          TripNarrative tripNarrative = _narrativeService.getTripForId(trip.getId());
          String headsign = tripNarrative.getTripHeadsign();
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

    List<StopSequenceCollection> blocks = new ArrayList<StopSequenceCollection>();

    for (Map.Entry<String, String> entry : directionToName.entrySet()) {

      String direction = entry.getKey();
      String name = entry.getValue();
      List<StopSequence> patterns = sequencesByGroupId.get(direction);

      Segment segment = segments.get(direction);

      // System.out.println("  " + direction + " => " + name);
      StopSequenceCollection block = new StopSequenceCollection();

      if (segment.fromLat == 0.0)
        throw new IllegalStateException("what?");

      StopSequenceCollectionKey key = new StopSequenceCollectionKey(null,
          direction);
      block.setId(key);
      block.setPublicId(direction);
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
   * @param sequencesByGroupId
   */
  private void computeContinuations(
      Map<StopSequence, PatternStats> sequenceStats,
      Map<String, List<StopSequence>> sequencesByGroupId) {

    Map<BlockTripEntry, StopSequence> stopSequencesByTrip = new HashMap<BlockTripEntry, StopSequence>();

    Map<StopSequence, String> stopSequenceGroupIds = new HashMap<StopSequence, String>();
    for (Map.Entry<String, List<StopSequence>> entry : sequencesByGroupId.entrySet()) {
      String id = entry.getKey();
      for (StopSequence sequence : entry.getValue())
        stopSequenceGroupIds.put(sequence, id);
    }

    for (StopSequence sequence : sequenceStats.keySet()) {

      String groupId = stopSequenceGroupIds.get(sequence);

      for (BlockTripEntry trip : sequence.getTrips()) {

        BlockTripEntry prevTrip = trip.getPreviousTrip();

        if (prevTrip == null)
          continue;

        StopSequence prevSequence = stopSequencesByTrip.get(prevTrip);

        // No continuations if incoming is not part of the sequence collection
        if (prevSequence == null)
          continue;

        // No continuation if it's the same stop sequence
        if (prevSequence.equals(sequence))
          continue;

        // No contination if the the block group ids don't match
        String prevGroupId = stopSequenceGroupIds.get(prevSequence);
        if (!groupId.equals(prevGroupId))
          continue;

        StopEntry prevStop = prevSequence.getStops().get(
            prevSequence.getStops().size() - 1);
        StopEntry nextStop = sequence.getStops().get(0);
        double d = SphericalGeometryLibrary.distance(prevStop.getStopLat(),
            prevStop.getStopLon(), nextStop.getStopLat(), nextStop.getStopLon());
        if (d < 5280 / 4) {
          /*
           * System.out.println("distance=" + d + " from=" + prevStop.getId() +
           * " to=" + nextStop.getId() + " ssFrom=" + prevSequence.getId() +
           * " ssTo=" + stopSequence.getId());
           */
          PatternStats stats = sequenceStats.get(prevSequence);
          stats.continuations.add(sequence);
        }
      }
    }
  }

  private void exploreStopSequences(RecursiveStats rs,
      Map<StopSequence, PatternStats> patternStats,
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
        segment = new Segment(prevSegment, segment, prevSegment.distance
            + segment.distance);

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
    Set<Pair<StopEntry>> pairsA = getStopSequenceAsStopPairSet(a);
    Set<Pair<StopEntry>> pairsB = getStopSequenceAsStopPairSet(b);
    int common = 0;
    for (Pair<StopEntry> pairA : pairsA) {
      if (pairsB.contains(pairA))
        common++;
    }

    double ratioA = ((double) common) / pairsA.size();
    double ratioB = ((double) common) / pairsB.size();
    return Math.max(ratioA, ratioB);
  }

  private Set<Pair<StopEntry>> getStopSequenceAsStopPairSet(
      StopSequence stopSequence) {
    Set<Pair<StopEntry>> pairs = new HashSet<Pair<StopEntry>>();
    StopEntry prev = null;
    for (StopEntry stop : stopSequence.getStops()) {
      if (prev != null) {
        Pair<StopEntry> pair = Tuples.pair(prev, stop);
        pairs.add(pair);
      }
      prev = stop;
    }
    return pairs;
  }

  /*
   * private static class BlockComparator implements Comparator<Trip> {
   * 
   * public BlockComparator() {
   * 
   * }
   * 
   * public int compare(Trip o1, Trip o2) { return o2.getBlockSequenceId() -
   * o1.getBlockSequenceId(); } }
   */

  private static class PatternStats {
    long tripCounts;
    Segment segment;
    Set<StopSequence> continuations = new HashSet<StopSequence>();
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
