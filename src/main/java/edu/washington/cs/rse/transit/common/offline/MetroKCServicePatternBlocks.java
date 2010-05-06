package edu.washington.cs.rse.transit.common.offline;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.stats.Max;
import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.impl.RegionsDAO;
import edu.washington.cs.rse.transit.common.model.BlockTrip;
import edu.washington.cs.rse.transit.common.model.ChangeDate;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.TransLinkShapePoint;
import edu.washington.cs.rse.transit.common.model.Trip;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlock;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlockKey;

import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class MetroKCServicePatternBlocks {

  private static final double SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN = 0.2;

  private static final BlockComparator BLOCK_COMPARATOR = new BlockComparator();

  @Autowired
  private MetroKCDAO _dao;

  @Autowired
  private RegionsDAO _regionsDAO;

  public static void main(String[] args) {
    ApplicationContext context = MetroKCApplicationContext.getApplicationContext();
    MetroKCServicePatternBlocks m = new MetroKCServicePatternBlocks();
    context.getAutowireCapableBeanFactory().autowireBean(m);
    m.run();
  }

  private List<Route> getRoutes() {

    if (true)
      return _dao.getAllRoutes();

    List<Route> routes = new ArrayList<Route>();
    routes.add(_dao.getRouteByNumber(48));
    return routes;

  }

  private void run() {

    ChangeDate now = _dao.getCurrentServiceRevision();

    for (Route route : getRoutes()) {

      System.out.println("== ROUTE " + route.getNumber() + " ==");

      Map<ServicePattern, Long> counts = _dao.getServicePatternsByChangeDateAndRouteWithTripCounts(
          now, route);

      Set<ServicePattern> servicePatterns = counts.keySet();

      Map<ServicePattern, PatternStats> patternStats = new HashMap<ServicePattern, PatternStats>();

      for (ServicePattern pattern : servicePatterns) {
        PatternStats stats = new PatternStats();
        stats.tripCounts = counts.get(pattern);
        stats.regions = getRegions(pattern);
        stats.segment = getSegment(pattern);
        patternStats.put(pattern, stats);
      }

      pruneCommonLayers(patternStats);

      Map<String, List<ServicePattern>> patternsByDirection = CollectionsLibrary.mapToValueList(
          servicePatterns, "direction", String.class);

      for (ServicePattern pattern : servicePatterns) {

        List<BlockTrip> blocks = _dao.getTripBlocksByServicePattern(pattern);

        Map<Integer, List<BlockTrip>> blockTripsByBlockId = CollectionsLibrary.mapToValueList(
            blocks, "id.id", Integer.class);

        blocks = _dao.getTripBlocksByIds(blockTripsByBlockId.keySet());
        blockTripsByBlockId = CollectionsLibrary.mapToValueList(blocks, "id.id",
            Integer.class);

        for (List<BlockTrip> blocksById : blockTripsByBlockId.values()) {

          Collections.sort(blocksById, BLOCK_COMPARATOR);

          ServicePattern prevPattern = null;
          Trip prevTrip = null;
          for (BlockTrip bt : blocksById) {
            Trip trip = bt.getId().getTrip();
            ServicePattern p = trip.getServicePattern();
            if (prevPattern != null) {
              if (servicePatterns.contains(prevPattern)
                  && servicePatterns.contains(p)
                  && trip.getDirectionName().equals(prevTrip.getDirectionName()))
                patternStats.get(prevPattern).continuations.add(p);
            }
            prevTrip = trip;
            prevPattern = p;
          }
        }
      }

      Set<String> allNames = new HashSet<String>();
      Map<String, String> directionToName = new HashMap<String, String>();
      Map<String, Segment> segments = new HashMap<String, Segment>();

      for (Map.Entry<String, List<ServicePattern>> entry : patternsByDirection.entrySet()) {

        String direction = entry.getKey();
        List<ServicePattern> patterns = entry.getValue();

        Max<ServicePattern> maxTripCount = new Max<ServicePattern>();

        for (ServicePattern pattern : patterns) {
          long count = counts.get(pattern);
          maxTripCount.add(count, pattern);
        }

        RecursiveStats rs = new RecursiveStats();
        rs.maxTripCount = (long) maxTripCount.getMaxValue();
        rs.graph = new Graph<SortedMap<Layer, Region>>();

        exploreServicePatterns(rs, patternStats, patterns);

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
          direction = direction.charAt(0)
              + direction.substring(1).toLowerCase();
          entry.setValue(name + " - " + direction);
        }
      }

      for (Map.Entry<String, String> entry : directionToName.entrySet()) {

        String direction = entry.getKey();
        String name = entry.getValue();
        List<ServicePattern> patterns = patternsByDirection.get(direction);

        Segment segment = segments.get(direction);

        System.out.println("  " + direction + " => + " + name);
        ServicePatternBlock block = new ServicePatternBlock();

        ServicePatternBlockKey key = new ServicePatternBlockKey(route,
            direction);
        block.setId(key);
        block.setDescription(name);
        block.setServicePatterns(patterns);
        block.setStartLocation(segment.from);
        block.setEndLocation(segment.to);

        _dao.save(block);
      }
    }
  }

  private Segment getSegment(ServicePattern pattern) {

    Segment segment = new Segment();

    List<TransLinkShapePoint> points = _dao.getTransLinkShapePointsByServicePattern(pattern);

    for (TransLinkShapePoint shapePoint : points) {
      Point point = shapePoint.getLocation();
      if (segment.from == null)
        segment.from = point;
      else
        segment.distance += segment.to.distance(point);
      segment.to = point;
    }

    return segment;
  }

  private void exploreServicePatterns(RecursiveStats rs,
      Map<ServicePattern, PatternStats> patternStats,
      Iterable<ServicePattern> patterns) {

    SortedMap<Layer, Region> prevRegions = rs.prevRegions;
    Segment prevSegment = rs.prevSegment;

    for (ServicePattern pattern : patterns) {

      if (rs.visited.contains(pattern))
        continue;

      PatternStats stats = patternStats.get(pattern);

      double count = stats.tripCounts;
      double ratio = count / rs.maxTripCount;

      if (ratio < SERVICE_PATTERN_TRIP_COUNT_RATIO_MIN)
        continue;

      Segment segment = stats.segment;

      if (prevSegment != null)
        segment = new Segment(prevSegment.from, segment.to,
            prevSegment.distance + segment.distance);

      rs.longestSegment.add(segment.distance, segment);

      rs.graph.addNode(stats.regions);

      if (prevRegions != null && !prevRegions.equals(stats.regions))
        rs.graph.addEdge(prevRegions, stats.regions);

      Set<ServicePattern> nextPatterns = stats.continuations;

      if (!nextPatterns.isEmpty()) {
        rs.visited.add(pattern);
        rs.prevSegment = segment;
        rs.prevRegions = stats.regions;
        exploreServicePatterns(rs, patternStats, nextPatterns);
        rs.visited.remove(pattern);
      }
    }
  }

  private void pruneCommonLayers(Map<ServicePattern, PatternStats> patternStats) {

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

  private SortedMap<Layer, Region> getRegions(ServicePattern servicePattern) {

    List<Timepoint> timepoints = _dao.getTimepointsByServicePattern(servicePattern);

    Timepoint last = timepoints.get(timepoints.size() - 1);
    Point location = last.getTransNode().getLocation();

    return _regionsDAO.getRegionsByLocation(location);
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

  private static class BlockComparator implements Comparator<BlockTrip> {

    public int compare(BlockTrip o1, BlockTrip o2) {
      int index1 = o1.getTripPosition();
      int index2 = o2.getTripPosition();
      return index1 == index2 ? 0 : (index1 < index2 ? -1 : 1);
    }

  }

  private static class PatternStats {
    long tripCounts;
    Segment segment;
    SortedMap<Layer, Region> regions;
    Set<ServicePattern> continuations = new HashSet<ServicePattern>();
  }

  private static class RecursiveStats {
    Graph<SortedMap<Layer, Region>> graph;
    Max<Segment> longestSegment = new Max<Segment>();
    Set<ServicePattern> visited = new HashSet<ServicePattern>();
    long maxTripCount;
    SortedMap<Layer, Region> prevRegions;
    Segment prevSegment;

  }

  private static class Segment {

    Point from;
    Point to;
    double distance;

    public Segment() {

    }

    public Segment(Point from, Point to, double d) {
      this.from = from;
      this.to = to;
      this.distance = d;
    }
  }
}
