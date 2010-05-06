/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.offline.interpolation;

import edu.washington.cs.rse.collections.stats.DCounter;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.collections.tuple.T2;
import edu.washington.cs.rse.text.CSVLibrary;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.PatternTimepoints;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.TPIPath;
import edu.washington.cs.rse.transit.common.model.TransLink;
import edu.washington.cs.rse.transit.common.model.aggregate.StopTimepointInterpolation;
import edu.washington.cs.rse.transit.common.offline.Indexed;

import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class ServicePatternStopInterpolation {

  private MetroKCDAO _dao;

  private ServicePattern _pattern;

  private List<PatternTimepoints> _patterns;

  private Map<PatternTimepoints, List<TPIPath>> _tpiPathsByPatterns;

  private List<T2<StopLocation, Boolean>> _stops;

  private TransLink _firstLink = null;

  private TransLink _lastLink = null;

  /**
   * The total length of each pattern between timepoints
   */
  private DCounter<PatternTimepoints> _patternLengths = new DCounter<PatternTimepoints>();

  /**
   * Map from TransLinks to their LinkInterpolation info. Since a given link can
   * occur multiple times for a pattern (loops,etc), there are potentially
   * multiple interpolations for the same link
   */
  private Map<TransLink, List<LinkInterpolation>> _linksToInterpolation = new HashMap<TransLink, List<LinkInterpolation>>();

  private SortedMap<Indexed<StopLocation>, Double> _distFromStart = new TreeMap<Indexed<StopLocation>, Double>();

  private List<Indexed<StopLocation>> _indexedStops = new ArrayList<Indexed<StopLocation>>();

  private Map<Indexed<StopLocation>, TransLink> _stopToLink = new HashMap<Indexed<StopLocation>, TransLink>();

  private Set<Indexed<StopLocation>> _notOnLink = new HashSet<Indexed<StopLocation>>();

  private SortedSet<Indexed<StopLocation>> _unassignedStops = new TreeSet<Indexed<StopLocation>>();

  private Set<Indexed<StopLocation>> _firstPass = new HashSet<Indexed<StopLocation>>();

  private SortedMap<Indexed<StopLocation>, StopTimepointInterpolation> _results = new TreeMap<Indexed<StopLocation>, StopTimepointInterpolation>();

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public ServicePatternStopInterpolation(MetroKCDAO dao, ServicePattern pattern) {
    _dao = dao;
    _pattern = pattern;
  }

  public void run() {

    // Initial data
    _patterns = _dao.getPatternTimepointsByServicePattern(_pattern);
    _tpiPathsByPatterns = _dao.getPatternTimepointsAndTPIPathByServicePattern(_pattern);
    _stops = _dao.getStopLocationsAndPptFlagByServicePattern(_pattern, true);

    if (true)
      System.out.println(_pattern.getId() + " route="
          + _pattern.getRoute().getNumber());

    computePathLengths();
    computeLinkInterpolations();
    snapStopsToAssignedOrNearestTransLink();
    doInitialAssignment();

    while (!_unassignedStops.isEmpty()) {

      List<List<Indexed<StopLocation>>> clusters = getClusters(_unassignedStops);

      Min<List<Indexed<StopLocation>>> m2 = new Min<List<Indexed<StopLocation>>>();
      Map<Indexed<StopLocation>, LinkInterpolation> assignments = new HashMap<Indexed<StopLocation>, LinkInterpolation>();

      for (List<Indexed<StopLocation>> cluster : clusters) {
        double score = getMostLikelyAssignment(cluster, assignments);
        m2.add(score, cluster);
      }

      List<Indexed<StopLocation>> minElements = m2.getMinElement();

      for (Indexed<StopLocation> element : minElements) {

        LinkInterpolation p = assignments.get(element);

        _results.put(element, p.interpolate(_pattern, element));
        _distFromStart.put(element, p.getTotalOffsetLength(element.getValue()));
        _unassignedStops.remove(element);

        if (false) {
          int score = (int) m2.getMinValue();
          System.out.println(CSVLibrary.getAsCSV(score,
              _pattern.getId().getId(), _pattern.getRoute().getNumber(),
              element.getValue().getId(), element.getIndex()));
        }
      }
    }

    checkResults();

    _dao.saveAllEntities(_results.values());
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  /**
   * Compute the length between time-points along a {@link PatternTimepoints} by
   * summing up the length of all the TPIPaths along the Pattern
   */
  private void computePathLengths() {

    for (Map.Entry<PatternTimepoints, List<TPIPath>> entry : _tpiPathsByPatterns.entrySet()) {
      PatternTimepoints pattern = entry.getKey();
      List<TPIPath> paths = entry.getValue();
      for (TPIPath path : paths) {
        TransLink link = path.getTransLink();
        _patternLengths.increment(pattern, link.getLinkLen());
        if (_firstLink == null)
          _firstLink = link;
        _lastLink = link;
      }
    }
  }

  /**
   * For each pattern, iterate over each of its TransLink, determining relative
   * distances from the start of the ServicePattern and start of the Pattern for
   * us in later interpolation calculations
   */
  private void computeLinkInterpolations() {

    double patternOffset = 0;

    for (int timepointIndex = 0; timepointIndex < _patterns.size() - 1; timepointIndex++) {

      PatternTimepoints from = _patterns.get(timepointIndex);
      PatternTimepoints to = _patterns.get(timepointIndex + 1);

      double linkOffset = 0;
      double patternLength = _patternLengths.getCount(from);
      List<TPIPath> paths = _tpiPathsByPatterns.get(from);

      for (TPIPath path : paths) {

        TransLink link = path.getTransLink();

        LinkInterpolation partial = new LinkInterpolation(_dao, link,
            from.getTimepoint(), to.getTimepoint(), timepointIndex,
            path.getFlowDirection());

        partial.setPatternOffset(patternOffset);
        partial.setPatternLength(patternLength);
        partial.setLinkOffset(linkOffset);

        List<LinkInterpolation> data = _linksToInterpolation.get(link);
        if (data == null) {
          data = new ArrayList<LinkInterpolation>();
          _linksToInterpolation.put(link, data);
        }
        data.add(partial);
        linkOffset += link.getLinkLen();
      }

      patternOffset += linkOffset;
    }
  }

  /**
   * Loop through each stop, making sure its translink is on the route path. If
   * not, we find the closest translink on the route to use. Special care is
   * taken for the first and last stop, which may be beyond the actual transit
   * pattern, typically indicated by the PPT flag
   */

  private void snapStopsToAssignedOrNearestTransLink() {

    int stopIndex = 0;

    for (T2<StopLocation, Boolean> tuple : _stops) {

      StopLocation stop = tuple.getFirst();
      boolean pptFlag = tuple.getSecond();
      Indexed<StopLocation> ins = Indexed.create(stop, stopIndex);
      _indexedStops.add(ins);

      TransLink link = stop.getTransLink();

      if (pptFlag) {
        if (stopIndex == 0) {
          link = _firstLink;
        } else if (stopIndex == _stops.size() - 1
            || stopIndex == _stops.size() - 2) {
          link = _lastLink;
        } else {
          System.out.println("  ======= ignore for now? ======");
          //throw new IllegalStateException();
        }
      }

      List<LinkInterpolation> data = _linksToInterpolation.get(link);

      if (data == null || data.size() == 0) {
        _notOnLink.add(ins);
        Min<TransLink> m = new Min<TransLink>();
        for (TransLink validLink : _linksToInterpolation.keySet()) {
          Point pa = validLink.getTransNodeFrom().getLocation();
          Point pb = validLink.getTransNodeTo().getLocation();
          LineSegment segment = new LineSegment(pa.getCoordinate(),
              pb.getCoordinate());
          double d = segment.distance(stop.getLocation().getCoordinate());
          m.add(d, validLink);
        }
        link = m.getMinElement();
      }

      _stopToLink.put(ins, link);

      stopIndex++;
    }

  }

  /**
   * For each stop, use its assigned link to lookup possible interpolation
   * information. If just one interpolation is possible, we can go ahead and
   * assign that stop to that interpolation. Otherwise, we keep track of the
   * unassigned stops for later processing
   */
  private void doInitialAssignment() {

    int stopIndex = 0;

    for (Indexed<StopLocation> ins : _indexedStops) {
      StopLocation stop = ins.getValue();
      TransLink link = _stopToLink.get(ins);
      List<LinkInterpolation> data = _linksToInterpolation.get(link);

      if (data != null && data.size() == 1) {
        LinkInterpolation partial = data.get(0);
        StopTimepointInterpolation sti = partial.interpolate(_pattern, ins);
        _results.put(ins, sti);
        _distFromStart.put(ins, partial.getTotalOffsetLength(stop));
        _firstPass.add(ins);
      } else {
        _unassignedStops.add(ins);
      }

      stopIndex++;
    }
  }

  /**
   * Group unassigned stops into clusters, where each cluster is a group of
   * consecutive stops
   */
  private List<List<Indexed<StopLocation>>> getClusters(
      Collection<Indexed<StopLocation>> elements) {

    List<List<Indexed<StopLocation>>> clusters = new ArrayList<List<Indexed<StopLocation>>>();

    if (elements.isEmpty())
      return clusters;

    Indexed<StopLocation> prev = null;
    List<Indexed<StopLocation>> current = new ArrayList<Indexed<StopLocation>>();
    clusters.add(current);

    for (Indexed<StopLocation> element : elements) {
      if (prev != null && prev.getIndex() + 1 < element.getIndex()) {
        current = new ArrayList<Indexed<StopLocation>>();
        clusters.add(current);
      }
      current.add(element);
      prev = element;
    }
    return clusters;
  }

  private double getMostLikelyAssignment(List<Indexed<StopLocation>> elements,
      Map<Indexed<StopLocation>, LinkInterpolation> assignments) {

    Min<Map<Indexed<StopLocation>, LinkInterpolation>> m = new Min<Map<Indexed<StopLocation>, LinkInterpolation>>();

    rMostLikelyAssignment(assignments, m, elements, 0);

    assignments.putAll(m.getMinElement());

    return m.getMinValue() / elements.size();
  }

  private void rMostLikelyAssignment(
      Map<Indexed<StopLocation>, LinkInterpolation> assignments,
      Min<Map<Indexed<StopLocation>, LinkInterpolation>> m,
      List<Indexed<StopLocation>> elements, int index) {

    if (index >= elements.size()) {
      SortedMap<Indexed<StopLocation>, Double> d = new TreeMap<Indexed<StopLocation>, Double>(
          _distFromStart);

      for (Indexed<StopLocation> element : elements) {
        LinkInterpolation p = assignments.get(element);
        double len = p.getTotalOffsetLength(element.getValue());
        d.put(element, len);
      }

      double score = 0;
      for (Indexed<StopLocation> element : elements) {
        double len = d.remove(element);
        score += getScore(d, element, len);
        d.put(element, len);
      }

      m.add(score, assignments);
      return;
    }

    Indexed<StopLocation> ins = elements.get(index);
    TransLink link = _stopToLink.get(ins);

    List<LinkInterpolation> data = _linksToInterpolation.get(link);

    for (LinkInterpolation p : data) {
      HashMap<Indexed<StopLocation>, LinkInterpolation> assigned = new HashMap<Indexed<StopLocation>, LinkInterpolation>(
          assignments);
      assigned.put(ins, p);
      rMostLikelyAssignment(assigned, m, elements, index + 1);
    }
  }

  private double getScore(SortedMap<Indexed<StopLocation>, Double> dFromStart,
      Indexed<StopLocation> ins, double len) {

    if (dFromStart.containsKey(ins) || dFromStart.isEmpty())
      throw new IllegalStateException();

    SortedMap<Indexed<StopLocation>, Double> before = dFromStart.headMap(ins);
    SortedMap<Indexed<StopLocation>, Double> after = dFromStart.tailMap(ins);

    if (before.isEmpty()) {
      double to = dFromStart.get(after.firstKey());
      if (len <= to)
        return 0;
      return len - to;
    } else if (after.isEmpty()) {
      double from = dFromStart.get(before.lastKey());
      if (from <= len)
        return 0;
      return from - len;
    } else {
      double from = dFromStart.get(before.lastKey());
      double to = dFromStart.get(after.firstKey());
      if (from <= len && len <= to)
        return 0;
      return Math.min(Math.abs(from - len), Math.abs(len - to));
    }
  }

  private void checkResults() {

    double prev = Double.NEGATIVE_INFINITY;
    StopTimepointInterpolation prevSti = null;
    Indexed<StopLocation> prevKey = null;

    for (Map.Entry<Indexed<StopLocation>, StopTimepointInterpolation> entry : _results.entrySet()) {

      Indexed<StopLocation> key = entry.getKey();
      StopTimepointInterpolation sti = entry.getValue();

      double offset = _distFromStart.get(key);

      if (offset < prev) {
        TransLink fromLink = _stopToLink.get(prevKey);
        TransLink toLink = _stopToLink.get(key);
        System.out.println("============= BACKWARDS! =================");

        String pre = "route(" + _pattern.getRoute().getNumber() + ")sp("
            + _pattern.getId().getId() + ")";

        System.out.println(pre + "stop(" + prevSti.getStop().getId() + ")link("
            + fromLink.getId() + ")");
        System.out.println(pre + "stop(" + sti.getStop().getId() + ")link("
            + toLink.getId() + ")");

        System.out.println("    prev=" + prevKey + " " + getDesc(prevSti)
            + " offset=" + prev + " first=" + _firstPass.contains(prevKey));
        System.out.println("    next=" + key + " " + getDesc(sti) + " offset="
            + offset + " first=" + _firstPass.contains(key));
        System.out.println("    diff=" + (prev - offset));
      }

      prev = offset;
      prevSti = sti;
      prevKey = key;
      if (false) {
        String row = getDesc(sti);
        System.out.println("  " + row);
      }
    }
  }

  private String getDesc(StopTimepointInterpolation sti) {
    String row = CSVLibrary.getAsCSV(sti.getFromTimepoint().getId(),
        sti.getToTimepoint().getId(), sti.getRatio());
    return row;
  }
}
