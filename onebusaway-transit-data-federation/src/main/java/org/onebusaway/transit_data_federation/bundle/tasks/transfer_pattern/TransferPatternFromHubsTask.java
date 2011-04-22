package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.HasStopTimeInstanceTransitVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.OriginVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPArrivalAndTransferEdge;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPBlockArrivalVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPTransferVertex;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.model.ServiceDateSummary;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.StopScheduleService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.otp.OTPConfigurationService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.GenericDijkstra;
import org.opentripplanner.routing.algorithm.strategies.SkipVertexStrategy;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.pqueue.PriorityQueueImpl;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.spt.MultiShortestPathTree;
import org.opentripplanner.routing.spt.SPTEdge;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.springframework.beans.factory.annotation.Autowired;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class TransferPatternFromHubsTask implements Runnable {

  private static SPTVertexDurationComparator _sptVertexDurationComparator = new SPTVertexDurationComparator();

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _transitGraphDao;

  private GraphService _graphService;

  private OTPConfigurationService _otpConfigurationService;

  private StopScheduleService _stopScheduleService;

  private StopTimeService _stopTimeService;

  private int _serviceDateCount = 4;

  private double _transferPatternFrequencyCutoff = 0.1;

  private double _transferPatternFrequencySlack = 0.5;

  private double _transferPatternWeightImprovement = 0.66;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setGraphProvider(GraphService graphService) {
    _graphService = graphService;
  }

  @Autowired
  public void setOtpConfigurationService(
      OTPConfigurationService otpConfigurationService) {
    _otpConfigurationService = otpConfigurationService;
  }

  @Autowired
  public void setStopScheduleService(StopScheduleService stopScheduleService) {
    _stopScheduleService = stopScheduleService;
  }

  @Autowired
  public void setStopTimeService(StopTimeService stopTimeService) {
    _stopTimeService = stopTimeService;
  }

  public void setServiceDateCount(int serviceDateCount) {
    _serviceDateCount = serviceDateCount;
  }

  public void setTransferPatternFrequencyCutoff(
      double transferPatternFrequencyCutoff) {
    _transferPatternFrequencyCutoff = transferPatternFrequencyCutoff;
  }

  public void setTransferPatternFrequencySlack(
      double transferPatternFrequencySlack) {
    _transferPatternFrequencySlack = transferPatternFrequencySlack;
  }

  @Override
  public void run() {

    List<StopEntry> stops = loadHubStops();

    Graph graph = _graphService.getGraph();
    GraphContext context = _otpConfigurationService.createGraphContext();

    Map<AgencyAndId, MutableTransferPattern> patternsByStopId = new HashMap<AgencyAndId, MutableTransferPattern>();

    for (StopEntry stop : stops) {

      System.out.println("stop=" + stop.getId());

      MutableTransferPattern pattern = new MutableTransferPattern(stop);

      List<ServiceDate> serviceDates = computeServiceDates(stop);

      for (ServiceDate serviceDate : serviceDates) {

        System.out.println("  serviceDate=" + serviceDate);

        Range interval = _stopTimeService.getDepartureForStopAndServiceDate(
            stop.getId(), serviceDate);

        if (interval.isEmpty())
          continue;

        long tFrom = (long) interval.getMin();
        long tTo = (long) interval.getMax();

        List<StopTimeInstance> instances = _stopTimeService.getStopTimeInstancesInTimeRange(
            stop, new Date(tFrom), new Date(tTo));

        System.out.println("      instances=" + instances.size());

        OBATraverseOptions options = _otpConfigurationService.createTraverseOptions();

        options.maxComputationTime = -1;
        options.shortestPathTreeFactory = MultiShortestPathTree.FACTORY;
        options.priorityQueueFactory = PriorityQueueImpl.FACTORY;
        options.waitAtBeginningFactor = 1.0;
        options.extraSpecialMode = true;

        GenericDijkstra dijkstra = new GenericDijkstra(graph, options);
        dijkstra.setSkipVertexStrategy(new SkipVertexImpl(stop, tFrom));

        OriginVertex origin = new OriginVertex(context, stop, instances);
        State state = new State(tFrom, new OBAStateData());

        MultiShortestPathTree spt = (MultiShortestPathTree) dijkstra.getShortestPathTree(
            origin, state);

        processTree(spt, pattern, stop);
      }

      avgPaths(pattern, stop);

      patternsByStopId.put(stop.getId(), pattern);
    }

    writeData(patternsByStopId);
  }

  private void writeData(
      Map<AgencyAndId, MutableTransferPattern> patternsByStopId) {

    File path = _bundle.getTransferPatternsPath();

    try {

      PrintWriter out = openOutput(path);

      long index = 0;
      for (MutableTransferPattern pattern : patternsByStopId.values()) {
        index = pattern.writeTransferPatternsToPrintWriter(out, index);
      }
      out.close();
    } catch (Throwable ex) {
      throw new IllegalStateException("error writing output to " + path, ex);
    }
  }

  private PrintWriter openOutput(File path) throws IOException {
    OutputStream out = new FileOutputStream(path);
    if (path.getName().endsWith(".gz"))
      out = new GZIPOutputStream(out);
    return new PrintWriter(new OutputStreamWriter(out));
  }

  private void avgPaths(MutableTransferPattern pattern, StopEntry origin) {
    DoubleArrayList values = new DoubleArrayList();
    for (StopEntry stop : pattern.getStops()) {
      List<List<Pair<StopEntry>>> paths = pattern.getPathsForStop(stop);
      values.add(paths.size());
    }

    if (values.isEmpty())
      return;

    values.sort();

    System.out.println("    mu=" + Descriptive.mean(values));
    System.out.println("median=" + Descriptive.median(values));
    System.out.println("   max=" + Descriptive.max(values));
  }

  private List<StopEntry> loadHubStops() {

    List<StopEntry> stops = new ArrayList<StopEntry>();

    File path = _bundle.getHubStopsPath();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(path));
      String line = null;
      while ((line = reader.readLine()) != null) {
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(line);
        StopEntry stop = _transitGraphDao.getStopEntryForId(stopId, true);
        stops.add(stop);
      }
    } catch (IOException ex) {
      throw new IllegalStateException("error reading hub stops", ex);
    }

    return stops;
  }

  private List<ServiceDate> computeServiceDates(StopEntry stop) {

    List<ServiceDateSummary> summaries = _stopScheduleService.getServiceDateSummariesForStop(stop.getId());
    Collections.sort(summaries);

    List<ServiceDate> serviceDates = new ArrayList<ServiceDate>();

    int floor = Math.max(0, summaries.size() - _serviceDateCount);

    for (int i = summaries.size() - 1; i >= floor; i--) {
      ServiceDateSummary summary = summaries.get(i);
      List<ServiceDate> dates = summary.getDates();
      serviceDates.add(dates.get(dates.size() / 2));
    }

    return serviceDates;
  }

  private void processTree(MultiShortestPathTree spt,
      MutableTransferPattern pattern, StopEntry originStop) {

    Map<SPTVertex, List<StopEntry>> parentsBySPTVertex = new HashMap<SPTVertex, List<StopEntry>>();

    Map<StopEntry, List<TPBlockArrivalVertex>> arrivalsByStop = new FactoryMap<StopEntry, List<TPBlockArrivalVertex>>(
        new ArrayList<TPBlockArrivalVertex>());

    for (Vertex v : spt.getVertices()) {
      if (!(v instanceof TPBlockArrivalVertex))
        continue;

      TPBlockArrivalVertex bav = (TPBlockArrivalVertex) v;
      StopEntry stop = bav.getStop();
      arrivalsByStop.get(stop).add(bav);
    }

    for (Map.Entry<StopEntry, List<TPBlockArrivalVertex>> entry : arrivalsByStop.entrySet()) {
      processArrivalsForStop(entry.getKey(), entry.getValue(), originStop, spt,
          pattern, parentsBySPTVertex);
    }
  }

  private void processArrivalsForStop(StopEntry arrivalStop,
      List<TPBlockArrivalVertex> arrivals, StopEntry originStop,
      MultiShortestPathTree spt, MutableTransferPattern pattern,
      Map<SPTVertex, List<StopEntry>> parentsBySPTVertex) {

    Collections.sort(arrivals);

    Counter<List<Pair<StopEntry>>> pathCounts = new Counter<List<Pair<StopEntry>>>();
    Map<List<Pair<StopEntry>>, List<SPTVertex>> paths = new FactoryMap<List<Pair<StopEntry>>, List<SPTVertex>>(
        new ArrayList<SPTVertex>());

    SortedMap<Long, List<SPTVertex>> m = new TreeMap<Long, List<SPTVertex>>();
    m = FactoryMap.createSorted(m, new ArrayList<SPTVertex>());

    Map<SPTVertex, List<Pair<StopEntry>>> pathsByVertex = new HashMap<SPTVertex, List<Pair<StopEntry>>>();

    long startTime = 0;

    for (TPBlockArrivalVertex arrival : arrivals) {

      Collection<SPTVertex> sptVertices = pruneSptVertices(spt.getSPTVerticesForVertex(arrival));

      for (SPTVertex sptVertex : sptVertices) {

        List<Pair<StopEntry>> path = constructTransferPattern(originStop,
            arrival, sptVertex, parentsBySPTVertex);

        State state = sptVertex.state;
        boolean cut = state.getStartTime() <= startTime;

        if (!cut) {
          startTime = state.getStartTime();
          pathCounts.increment(path);
          paths.get(path).add(sptVertex);

          m.get(state.getTime()).add(sptVertex);
          pathsByVertex.put(sptVertex, path);
        }
      }
    }

    List<Pair<StopEntry>> max = pathCounts.getMax();
    int maxCount = pathCounts.getCount(max);

    List<List<Pair<StopEntry>>> toKeep = new ArrayList<List<Pair<StopEntry>>>();

    for (List<Pair<StopEntry>> path : pathCounts.getSortedKeys()) {

      int count = pathCounts.getCount(path);

      if (count <= _transferPatternFrequencyCutoff * maxCount) {

        List<SPTVertex> sptVertices = paths.get(path);
        boolean keep = false;

        for (SPTVertex sptVertex : sptVertices) {

          State state = sptVertex.state;
          long t = state.getTime();
          SPTVertex nextSptVertex = getNextVertex(m, pathsByVertex, t, path);
          if (nextSptVertex == null) {
            keep = true;
            break;
          }
          State nextState = nextSptVertex.state;
          int duration = (int) (Math.abs(state.getTime() - state.getStartTime()) / 1000);
          double additional = ((nextState.getTime() - state.getTime()) / 1000);
          if (additional / duration > _transferPatternFrequencySlack) {
            keep = true;
            break;
          }
        }

        if (!keep)
          continue;
      }

      toKeep.add(path);

    }

    for (List<Pair<StopEntry>> path : toKeep)
      pattern.addPath(path);
  }

  private Collection<SPTVertex> pruneSptVertices(
      Collection<SPTVertex> sptVerticesOrig) {

    if (sptVerticesOrig.size() == 1)
      return sptVerticesOrig;

    List<SPTVertex> sptVertices = new ArrayList<SPTVertex>(sptVerticesOrig);

    /**
     * This will put the spt vertices in order of increasing trip duration
     */
    Collections.sort(sptVertices, _sptVertexDurationComparator);

    double bestRatio = Double.MAX_VALUE;

    for (int i = 0; i < sptVertices.size(); i++) {
      SPTVertex sptVertex = sptVertices.get(i);
      double duration = getSPTVertexDuration(sptVertex);
      double weight = sptVertex.weightSum;
      double ratio = weight / duration;

      if (ratio / bestRatio > _transferPatternWeightImprovement) {
        return sptVertices.subList(0, i);
      } else {
        bestRatio = ratio;
      }
    }

    return sptVertices;

  }

  private SPTVertex getNextVertex(SortedMap<Long, List<SPTVertex>> m,
      Map<SPTVertex, List<Pair<StopEntry>>> pathsByVertex, long t,
      List<Pair<StopEntry>> currentPath) {

    SortedMap<Long, List<SPTVertex>> after = m.tailMap(t + 1);

    for (List<SPTVertex> sptVertices : after.values()) {
      for (SPTVertex sptVertex : sptVertices) {
        List<Pair<StopEntry>> path = pathsByVertex.get(sptVertex);
        if (!path.equals(currentPath))
          return sptVertex;
      }
    }

    return null;
  }

  private List<Pair<StopEntry>> constructTransferPattern(StopEntry originStop,
      TPBlockArrivalVertex arrival, SPTVertex sptVertex,
      Map<SPTVertex, List<StopEntry>> parentsBySPTVertex) {

    List<StopEntry> path = computeParentsForSPTVertex(sptVertex, originStop,
        parentsBySPTVertex);

    path = new ArrayList<StopEntry>(path);
    path.add(arrival.getStop());

    if (path.size() % 2 != 0)
      throw new IllegalArgumentException();

    List<Pair<StopEntry>> pairs = new ArrayList<Pair<StopEntry>>();

    for (int i = 0; i < path.size(); i += 2)
      pairs.add(Tuples.pair(path.get(i), path.get(i + 1)));

    return pairs;
  }

  private List<StopEntry> computeParentsForSPTVertex(SPTVertex sptVertex,
      StopEntry originStop, Map<SPTVertex, List<StopEntry>> parentsBySPTVertex) {

    List<StopEntry> parent = parentsBySPTVertex.get(sptVertex);

    if (parent != null)
      return parent;

    SPTEdge sptEdge = sptVertex.incoming;

    if (sptEdge == null) {
      parent = Arrays.asList(originStop);
    } else {

      Edge payload = sptEdge.payload;

      if (payload instanceof TPArrivalAndTransferEdge) {

        TPBlockArrivalVertex fromV = (TPBlockArrivalVertex) sptEdge.narrative.getFromVertex();
        TPTransferVertex toV = (TPTransferVertex) sptEdge.narrative.getToVertex();

        List<StopEntry> incomingPattern = computeParentsForSPTVertex(
            sptEdge.fromv, originStop, parentsBySPTVertex);
        ArrayList<StopEntry> extendedPattern = new ArrayList<StopEntry>(
            incomingPattern);

        extendedPattern.add(fromV.getStop());
        extendedPattern.add(toV.getStop());

        parent = extendedPattern;

      } else {

        parent = computeParentsForSPTVertex(sptEdge.fromv, originStop,
            parentsBySPTVertex);
      }
    }

    parentsBySPTVertex.put(sptVertex, parent);

    return parent;
  }

  private static int getSPTVertexDuration(SPTVertex v) {
    State state = v.state;
    return (int) (Math.abs(state.getTime() - state.getStartTime()) / 1000);
  }

  private static class SkipVertexImpl implements SkipVertexStrategy {

    private final Set<StopEntry> _stops = new HashSet<StopEntry>();

    private final StopEntry _originStop;

    private final long _serviceDate;

    public SkipVertexImpl(StopEntry originStop, long serviceDate) {
      _originStop = originStop;
      _serviceDate = serviceDate;
    }

    @Override
    public boolean shouldSkipVertex(Vertex origin, Vertex target,
        SPTVertex parent, Vertex current, State state, ShortestPathTree spt,
        TraverseOptions traverseOptions) {

      /**
       * We prune any arrivals that loop back to the origin stop
       */
      if (current instanceof TPBlockArrivalVertex) {
        TPBlockArrivalVertex bav = (TPBlockArrivalVertex) current;
        StopTimeInstance instance = bav.getInstance();
        if (instance.getStop() == _originStop)
          return true;
      }

      /**
       * Skip a vertex that has moved on to the next service date
       */
      if (current instanceof HasStopTimeInstanceTransitVertex) {
        HasStopTimeInstanceTransitVertex v = (HasStopTimeInstanceTransitVertex) current;
        StopTimeInstance instance = v.getInstance();
        if (instance.getServiceDate() > _serviceDate + 12 * 60 * 60 * 1000)
          return true;
      }

      /**
       * Print the visited stop count as a show of progress
       */
      if (current instanceof HasStopTransitVertex) {
        HasStopTransitVertex v = (HasStopTransitVertex) current;
        StopEntry stop = v.getStop();
        if (_stops.add(stop) && _stops.size() % 100 == 0) {
          System.out.println("stops=" + _stops.size());
        }
      }

      return false;
    }
  }

  private static class SPTVertexDurationComparator implements
      Comparator<SPTVertex> {

    @Override
    public int compare(SPTVertex o1, SPTVertex o2) {
      int t1 = getSPTVertexDuration(o1);
      int t2 = getSPTVertexDuration(o2);
      if (t1 != t2)
        return (t1 < t2 ? -1 : 1);

      double w1 = o1.weightSum;
      double w2 = o2.weightSum;
      return Double.compare(w1, w2);
    }
  }
}
