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
package org.onebusaway.metrokc2gtdf.handlers;

import edu.washington.cs.rse.collections.FactoryMap;
import edu.washington.cs.rse.collections.stats.DCounter;
import edu.washington.cs.rse.collections.stats.Min;
import edu.washington.cs.rse.text.CSVLibrary;

import edu.emory.mathcs.backport.java.util.Collections;

import org.onebusaway.gtdf.model.Route;
import org.onebusaway.metrokc2gtdf.TranslationContext;
import org.onebusaway.metrokc2gtdf.model.Indexed;
import org.onebusaway.metrokc2gtdf.model.MetroKCOrderedPatternStop;
import org.onebusaway.metrokc2gtdf.model.MetroKCPatternTimepoint;
import org.onebusaway.metrokc2gtdf.model.MetroKCServicePattern;
import org.onebusaway.metrokc2gtdf.model.MetroKCStop;
import org.onebusaway.metrokc2gtdf.model.MetroKCTPIPath;
import org.onebusaway.metrokc2gtdf.model.MetroKCTransLink;
import org.onebusaway.metrokc2gtdf.model.MetroKCTransNode;
import org.onebusaway.metrokc2gtdf.model.RouteSchedulePatternId;
import org.onebusaway.metrokc2gtdf.model.ServicePatternKey;
import org.onebusaway.metrokc2gtdf.model.StopTimepointInterpolation;

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

  private TranslationContext _context;

  private ServicePatternKey _pattern;

  /****
   * Handlers
   ****/

  private ServicePatternHandler _spHandler;

  private TransLinkHandler _transLinkHandler;

  private TransNodeHandler _transNodeHandler;

  /****
   * Data
   ****/

  private SortedMap<MetroKCPatternTimepoint, List<MetroKCTPIPath>> _tpiPathsByPatterns;

  private List<MetroKCOrderedPatternStop> _stops;

  private MetroKCTransLink _firstLink = null;

  private MetroKCTransLink _lastLink = null;

  private Map<MetroKCPatternTimepoint, Double> _patternTimepointDistanceOffsets = new TreeMap<MetroKCPatternTimepoint, Double>();

  /**
   * The total length of each pattern between timepoints
   */
  private DCounter<MetroKCPatternTimepoint> _patternLengths = new DCounter<MetroKCPatternTimepoint>();

  /**
   * Map from TransLinks to their LinkInterpolation info. Since a given link can
   * occur multiple times for a pattern (loops,etc), there are potentially
   * multiple interpolations for the same link
   */
  private Map<MetroKCTransLink, List<LinkInterpolation>> _linksToInterpolation = new FactoryMap<MetroKCTransLink, List<LinkInterpolation>>(
      new ArrayList<LinkInterpolation>());

  private SortedMap<Indexed<MetroKCStop>, Double> _distFromStart = new TreeMap<Indexed<MetroKCStop>, Double>();

  private List<Indexed<MetroKCStop>> _indexedStops = new ArrayList<Indexed<MetroKCStop>>();

  private Map<Indexed<MetroKCStop>, MetroKCTransLink> _stopToLink = new HashMap<Indexed<MetroKCStop>, MetroKCTransLink>();

  private Set<Indexed<MetroKCStop>> _notOnLink = new HashSet<Indexed<MetroKCStop>>();

  private SortedSet<Indexed<MetroKCStop>> _unassignedStops = new TreeSet<Indexed<MetroKCStop>>();

  private Set<Indexed<MetroKCStop>> _firstPass = new HashSet<Indexed<MetroKCStop>>();

  private SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> _results = new TreeMap<Indexed<MetroKCStop>, StopTimepointInterpolation>();

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public ServicePatternStopInterpolation(TranslationContext context,
      ServicePatternKey pattern) {
    _context = context;
    _pattern = pattern;
    _spHandler = _context.getHandler(ServicePatternHandler.class);
    _transLinkHandler = _context.getHandler(TransLinkHandler.class);
    _transNodeHandler = _context.getHandler(TransNodeHandler.class);
  }

  public Map<MetroKCPatternTimepoint, Double> getPatternTimepointDistanceOffsets() {
    return _patternTimepointDistanceOffsets;
  }

  public SortedMap<Indexed<MetroKCStop>, StopTimepointInterpolation> run() {

    // Initial data
    TPIPathHandler _tpipHandler = _context.getHandler(TPIPathHandler.class);
    _tpiPathsByPatterns = _tpipHandler.getTPIPathsByServicePatternId(_pattern);

    if (false) {
      for (Map.Entry<MetroKCPatternTimepoint, List<MetroKCTPIPath>> entry : _tpiPathsByPatterns.entrySet()) {
        MetroKCPatternTimepoint pt = entry.getKey();
        System.out.println(pt.getTimepointId() + " " + pt.getSequence() + " "
            + pt.getTpiId());
        List<MetroKCTPIPath> tpips = entry.getValue();
        for (MetroKCTPIPath tpip : tpips)
          System.out.println("  " + tpip.getTransLink() + " "
              + tpip.getSequence() + " " + tpip.getFlowDirection());
      }
    }

    ServicePatternHandler spHandler = _context.getHandler(ServicePatternHandler.class);
    MetroKCServicePattern sp = spHandler.getEntity(_pattern);
    RouteSchedulePatternId rspid = new RouteSchedulePatternId(sp.getRoute(),
        sp.getSchedulePatternId());

    OrderedPatternStopsHandler opsHandler = _context.getHandler(OrderedPatternStopsHandler.class);
    _stops = opsHandler.getOrderedPatternStopsByRouteSchedulePatternId(rspid);

    if (_stops.isEmpty()) {
      _context.addWarning("service pattern has no stops: " + _pattern);
      return new TreeMap<Indexed<MetroKCStop>, StopTimepointInterpolation>();
    } else if (_stops.size() == 1) {
      _context.addWarning("service pattern has just one stop: " + _pattern);
    }

    Collections.sort(_stops);

    computePathLengths();
    computeLinkInterpolations();
    snapStopsToAssignedOrNearestTransLink();
    doInitialAssignment();

    while (!_unassignedStops.isEmpty()) {

      List<List<Indexed<MetroKCStop>>> clusters = getClusters(_unassignedStops);

      Min<List<Indexed<MetroKCStop>>> m2 = new Min<List<Indexed<MetroKCStop>>>();
      Map<Indexed<MetroKCStop>, LinkInterpolation> assignments = new HashMap<Indexed<MetroKCStop>, LinkInterpolation>();

      for (List<Indexed<MetroKCStop>> cluster : clusters) {
        double score = getMostLikelyAssignment(cluster, assignments);
        m2.add(score, cluster);
      }

      List<Indexed<MetroKCStop>> minElements = m2.getMinElement();

      for (Indexed<MetroKCStop> element : minElements) {

        LinkInterpolation p = assignments.get(element);

        _results.put(element, p.interpolate(_pattern, element));
        _distFromStart.put(element, p.getTotalOffsetLength(element.getValue()));
        _unassignedStops.remove(element);

        if (false) {
          int score = (int) m2.getMinValue();
          Route route = _spHandler.getRouteByServicePatternKey(_pattern);
          System.out.println(CSVLibrary.getAsCSV(score, _pattern.getId(),
              route.getShortName(), element.getValue().getId(),
              element.getIndex()));
        }
      }
    }

    checkResults();

    return _results;
  }

  /*****************************************************************************
   * Private Methods
   *****************************************************************************/

  /**
   * Compute the length between time-points along a {@link PatternTimepoints} by
   * summing up the length of all the TPIPaths along the Pattern
   */
  private void computePathLengths() {

    double len = 0;

    for (Map.Entry<MetroKCPatternTimepoint, List<MetroKCTPIPath>> entry : _tpiPathsByPatterns.entrySet()) {

      MetroKCPatternTimepoint pattern = entry.getKey();
      List<MetroKCTPIPath> paths = entry.getValue();

      _patternTimepointDistanceOffsets.put(pattern, len);

      for (MetroKCTPIPath path : paths) {
        MetroKCTransLink link = _transLinkHandler.getEntity(path.getTransLink());
        _patternLengths.increment(pattern, link.getLinkLen());
        if (_firstLink == null)
          _firstLink = link;
        _lastLink = link;
        len += link.getLinkLen();
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

    List<MetroKCPatternTimepoint> patterns = new ArrayList<MetroKCPatternTimepoint>(
        _tpiPathsByPatterns.keySet());

    for (int timepointIndex = 0; timepointIndex < patterns.size() - 1; timepointIndex++) {

      MetroKCPatternTimepoint from = patterns.get(timepointIndex);
      MetroKCPatternTimepoint to = patterns.get(timepointIndex + 1);

      double linkOffset = 0;
      double patternLength = _patternLengths.getCount(from);
      List<MetroKCTPIPath> paths = _tpiPathsByPatterns.get(from);

      for (MetroKCTPIPath path : paths) {

        MetroKCTransLink link = _transLinkHandler.getEntity(path.getTransLink());
        MetroKCTransNode nodeFrom = _transNodeHandler.getEntity(link.getTransNodeFrom());
        MetroKCTransNode nodeTo = _transNodeHandler.getEntity(link.getTransNodeTo());
        LinkInterpolation partial = new LinkInterpolation(link, nodeFrom,
            nodeTo, from.getTimepointId(), to.getTimepointId(), timepointIndex,
            path.getFlowDirection());

        partial.setPatternOffset(patternOffset);
        partial.setPatternLength(patternLength);
        partial.setLinkOffset(linkOffset);

        List<LinkInterpolation> data = _linksToInterpolation.get(link);
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

    StopHandler stopHandler = _context.getHandler(StopHandler.class);

    int stopIndex = 0;

    for (MetroKCOrderedPatternStop ops : _stops) {

      MetroKCStop stop = stopHandler.getEntity(ops.getStop());
      boolean pptFlag = ops.getPptFlag();
      Indexed<MetroKCStop> ins = Indexed.create(stop, stopIndex);
      _indexedStops.add(ins);

      // System.out.println(stop.getId() + " " + stopIndex + " " + pptFlag);

      MetroKCTransLink link = _transLinkHandler.getEntity(stop.getTransLink());

      if (pptFlag) {
        if (stopIndex == 0) {
          link = _firstLink;
        } else if (stopIndex == _stops.size() - 1
            || stopIndex == _stops.size() - 2) {
          link = _lastLink;
        } else {
          System.out.println("  ======= ignore for now? ======");
          // throw new IllegalStateException();
        }
      }

      List<LinkInterpolation> data = _linksToInterpolation.get(link);

      if (data == null || data.size() == 0) {
        _notOnLink.add(ins);
        Min<MetroKCTransLink> m = new Min<MetroKCTransLink>();
        for (MetroKCTransLink validLink : _linksToInterpolation.keySet()) {
          MetroKCTransNode nodeA = _transNodeHandler.getEntity(validLink.getTransNodeFrom());
          MetroKCTransNode nodeB = _transNodeHandler.getEntity(validLink.getTransNodeTo());
          double d = Math.sqrt(Math.pow(nodeA.getX() - nodeB.getX(), 2)
              + Math.pow(nodeA.getY() - nodeB.getY(), 2));
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

    for (Indexed<MetroKCStop> ins : _indexedStops) {
      MetroKCStop stop = ins.getValue();
      MetroKCTransLink link = _stopToLink.get(ins);
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
  private List<List<Indexed<MetroKCStop>>> getClusters(
      Collection<Indexed<MetroKCStop>> elements) {

    List<List<Indexed<MetroKCStop>>> clusters = new ArrayList<List<Indexed<MetroKCStop>>>();

    if (elements.isEmpty())
      return clusters;

    Indexed<MetroKCStop> prev = null;
    List<Indexed<MetroKCStop>> current = new ArrayList<Indexed<MetroKCStop>>();
    clusters.add(current);

    for (Indexed<MetroKCStop> element : elements) {
      if (prev != null && prev.getIndex() + 1 < element.getIndex()) {
        current = new ArrayList<Indexed<MetroKCStop>>();
        clusters.add(current);
      }
      current.add(element);
      prev = element;
    }
    return clusters;
  }

  private double getMostLikelyAssignment(List<Indexed<MetroKCStop>> elements,
      Map<Indexed<MetroKCStop>, LinkInterpolation> assignments) {

    Min<Map<Indexed<MetroKCStop>, LinkInterpolation>> m = new Min<Map<Indexed<MetroKCStop>, LinkInterpolation>>();

    rMostLikelyAssignment(assignments, m, elements, 0);

    assignments.putAll(m.getMinElement());

    return m.getMinValue() / elements.size();
  }

  private void rMostLikelyAssignment(
      Map<Indexed<MetroKCStop>, LinkInterpolation> assignments,
      Min<Map<Indexed<MetroKCStop>, LinkInterpolation>> m,
      List<Indexed<MetroKCStop>> elements, int index) {

    if (index >= elements.size()) {
      SortedMap<Indexed<MetroKCStop>, Double> d = new TreeMap<Indexed<MetroKCStop>, Double>(
          _distFromStart);

      for (Indexed<MetroKCStop> element : elements) {
        LinkInterpolation p = assignments.get(element);
        double len = p.getTotalOffsetLength(element.getValue());
        d.put(element, len);
      }

      double score = 0;
      for (Indexed<MetroKCStop> element : elements) {
        double len = d.remove(element);
        score += getScore(d, element, len);
        d.put(element, len);
      }

      m.add(score, assignments);
      return;
    }

    Indexed<MetroKCStop> ins = elements.get(index);
    MetroKCTransLink link = _stopToLink.get(ins);

    List<LinkInterpolation> data = _linksToInterpolation.get(link);

    for (LinkInterpolation p : data) {
      HashMap<Indexed<MetroKCStop>, LinkInterpolation> assigned = new HashMap<Indexed<MetroKCStop>, LinkInterpolation>(
          assignments);
      assigned.put(ins, p);
      rMostLikelyAssignment(assigned, m, elements, index + 1);
    }
  }

  private double getScore(SortedMap<Indexed<MetroKCStop>, Double> dFromStart,
      Indexed<MetroKCStop> ins, double len) {

    if (dFromStart.containsKey(ins) || dFromStart.isEmpty())
      throw new IllegalStateException();

    SortedMap<Indexed<MetroKCStop>, Double> before = dFromStart.headMap(ins);
    SortedMap<Indexed<MetroKCStop>, Double> after = dFromStart.tailMap(ins);

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
    Indexed<MetroKCStop> prevKey = null;

    for (Map.Entry<Indexed<MetroKCStop>, StopTimepointInterpolation> entry : _results.entrySet()) {

      Indexed<MetroKCStop> key = entry.getKey();
      StopTimepointInterpolation sti = entry.getValue();

      double offset = _distFromStart.get(key);

      if (offset < prev) {
        MetroKCTransLink fromLink = _stopToLink.get(prevKey);
        MetroKCTransLink toLink = _stopToLink.get(key);
        System.out.println("============= BACKWARDS! =================");

        Route route = _spHandler.getRouteByServicePatternKey(_pattern);
        String pre = "route(" + route.getShortName() + ")sp("
            + _pattern.getId() + ")";

        System.out.println(pre + "stop(" + prevSti.getStop() + ")link("
            + fromLink.getId() + ")");
        System.out.println(pre + "stop(" + sti.getStop() + ")link("
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
    String row = CSVLibrary.getAsCSV(sti.getTimepointFrom().getTimepoint(),
        sti.getTimepointTo().getTimepoint(), sti.getRatio());
    return row;
  }
}
