package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.HasStopTimeInstanceTransitVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPOfflineBlockArrivalVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPOfflineNearbyStopsVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPOfflineOriginVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPOfflineTransferEdge;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPOfflineTransferVertex;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.graph.HasStopTransitVertex;
import org.onebusaway.transit_data_federation.model.ServiceDateSummary;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.StopScheduleService;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.StopTimeService.EFrequencyStopTimeBehavior;
import org.onebusaway.transit_data_federation.services.otp.OTPConfigurationService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.utility.IOLibrary;
import org.opentripplanner.routing.algorithm.GenericDijkstra;
import org.opentripplanner.routing.algorithm.strategies.SkipTraverseResultStrategy;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.pqueue.PriorityQueueImpl;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.MultiShortestPathTree;
import org.opentripplanner.routing.spt.SPTEdge;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.springframework.beans.factory.annotation.Autowired;

public class TransferPatternsTask implements Runnable {

  private static SPTVertexDurationComparator _sptVertexDurationComparator = new SPTVertexDurationComparator();

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _transitGraphDao;

  private GraphService _graphService;

  private OTPConfigurationService _otpConfigurationService;

  private StopScheduleService _stopScheduleService;

  private StopTimeService _stopTimeService;

  private ItinerariesService _itinerariesService;

  private int _serviceDateCount = 4;

  private double _transferPatternFrequencyCutoff = 0.1;

  private double _transferPatternFrequencySlack = 0.5;

  private double _transferPatternWeightImprovement = 0.66;

  private int _maxPathCountForLocalStop = 3;

  private int _maxPathCountForHubStop = 5;

  private double _nearbyStopsRadius = 100;

  private boolean _useHubStopsAsSourceStops = false;

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

  @Autowired
  public void setItinerariesService(ItinerariesService itinerariesService) {
    _itinerariesService = itinerariesService;
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

  public void setMaxPathCountForLocalStop(int maxPathCountForLocalStop) {
    _maxPathCountForLocalStop = maxPathCountForLocalStop;
  }

  public void setMaxPathCountForHubStop(int maxPathCountForHubStop) {
    _maxPathCountForHubStop = maxPathCountForHubStop;
  }

  public void setUseHubStopsAsSourceStops(boolean useHubStopsAsSourceStops) {
    _useHubStopsAsSourceStops = useHubStopsAsSourceStops;
  }

  @Override
  public void run() {

    long tIn = System.currentTimeMillis();

    List<StopEntry> hubStops = loadHubStops();
    List<StopEntry> stops = loadSourceStops(hubStops);

    Set<StopEntry> hubStopsAsSet = new HashSet<StopEntry>(hubStops);

    Graph graph = _graphService.getGraph();
    GraphContext context = _otpConfigurationService.createGraphContext();

    Map<AgencyAndId, MutableTransferPattern> patternsByStopId = new HashMap<AgencyAndId, MutableTransferPattern>();

    for (StopEntry stop : stops) {

      Map<StopEntry, Integer> nearbyStopsAndWalkTimes = getNearbyStopsAndWalkTimes(stop);

      boolean isHubStop = hubStopsAsSet.contains(stop);
      System.out.println("stop=" + stop.getId() + " hub=" + isHubStop);

      List<ServiceDate> serviceDates = computeServiceDates(stop);

      Map<StopEntry, Counter<List<Pair<StopEntry>>>> pathCountsByStop = new FactoryMap<StopEntry, Counter<List<Pair<StopEntry>>>>(
          new Counter<List<Pair<StopEntry>>>());

      for (ServiceDate serviceDate : serviceDates) {

        System.out.println("  serviceDate=" + serviceDate);

        List<StopTimeInstance> instances = getStopTimeInstancesForStopAndServiceDate(
            stop, serviceDate);
        System.out.println("      instances=" + instances.size());

        if (instances.isEmpty())
          continue;

        StopTimeInstance first = instances.get(0);
        long tFrom = first.getDepartureTime();

        Map<StopEntry, List<StopTimeInstance>> nearbyStopTimeInstances = getNearbyStopTimeInstances(
            nearbyStopsAndWalkTimes.keySet(), serviceDate);

        OBATraverseOptions options = _otpConfigurationService.createTraverseOptions();

        options.maxComputationTime = -1;
        options.waitAtBeginningFactor = 1.0;
        options.extraSpecialMode = true;
        if (isHubStop)
          options.maxTransfers = Integer.MAX_VALUE;
        else
          options.maxTransfers = 2;

        GenericDijkstra dijkstra = new GenericDijkstra(graph, options);
        dijkstra.setSkipTraverseResultStrategy(new SkipVertexImpl(stop, tFrom));
        dijkstra.setShortestPathTreeFactory(MultiShortestPathTree.FACTORY);
        dijkstra.setPriorityQueueFactory(PriorityQueueImpl.FACTORY);

        TPOfflineOriginVertex origin = new TPOfflineOriginVertex(context, stop,
            instances, nearbyStopsAndWalkTimes, nearbyStopTimeInstances);
        State state = new State(tFrom, new OBAStateData());

        MultiShortestPathTree spt = (MultiShortestPathTree) dijkstra.getShortestPathTree(
            origin, state);

        processTree(spt, stop, pathCountsByStop);
      }

      MutableTransferPattern pattern = new MutableTransferPattern(stop);

      System.out.println("arrivalStops=" + pathCountsByStop.size());

      for (Map.Entry<StopEntry, Counter<List<Pair<StopEntry>>>> entry : pathCountsByStop.entrySet()) {
        boolean verbose = false;//entry.getKey().getId().toString().equals("1_29430");
        Counter<List<Pair<StopEntry>>> pathCounts = entry.getValue();
        List<List<Pair<StopEntry>>> keys = pathCounts.getSortedKeys();
        int maxCount = isHubStop ? _maxPathCountForHubStop
            : _maxPathCountForLocalStop;
        if (verbose) {
          for (List<Pair<StopEntry>> path : keys)
            System.out.println(pathCounts.getCount(path) + "\t" + path);
        }
        while (keys.size() > maxCount)
          keys.remove(0);
        for (List<Pair<StopEntry>> path : keys)
          pattern.addPath(path);
      }

      avgPaths(pattern, stop);

      patternsByStopId.put(stop.getId(), pattern);
    }

    writeData(patternsByStopId);

    long tOut = System.currentTimeMillis();
    int duration = (int) ((tOut - tIn) / 1000);
    System.out.println("duration=" + duration);
  }

  private void writeData(
      Map<AgencyAndId, MutableTransferPattern> patternsByStopId) {

    File path = _bundle.getTransferPatternsPath();

    try {

      PrintWriter out = new PrintWriter(IOLibrary.getFileAsWriter(path));

      long index = 0;
      for (MutableTransferPattern pattern : patternsByStopId.values()) {
        index = pattern.writeTransferPatternsToPrintWriter(out, index);
      }
      out.close();
    } catch (Throwable ex) {
      throw new IllegalStateException("error writing output to " + path, ex);
    }
  }

  private void avgPaths(MutableTransferPattern pattern, StopEntry origin) {
    /*
     * DoubleArrayList values = new DoubleArrayList(); for (StopEntry stop :
     * pattern.getStops()) { TransferParent root = new TransferParent();
     * pattern.getTransfersForStop(stop, root); values.add(root.size()); }
     * 
     * if (values.isEmpty()) return;
     * 
     * values.sort();
     * 
     * System.out.println("    mu=" + Descriptive.mean(values));
     * System.out.println("median=" + Descriptive.median(values));
     * System.out.println("   max=" + Descriptive.max(values));
     */
  }

  private List<StopEntry> loadSourceStops(List<StopEntry> hubStops) {

    List<StopEntry> stops = new ArrayList<StopEntry>();

    String key = _bundle.getKey();
    String totalTaskCountValue = System.getProperty("totalTaskCount");

    if (key != null && totalTaskCountValue != null) {
      int taskIndex = Integer.parseInt(key);
      int totalTaskCount = Integer.parseInt(totalTaskCountValue);

      List<StopEntry> allStops = _transitGraphDao.getAllStops();
      if (_useHubStopsAsSourceStops)
        allStops = hubStops;

      double stopsPerTask = (double) allStops.size() / totalTaskCount;
      int from = (int) (taskIndex * stopsPerTask);
      int to = (int) ((taskIndex + 1) * stopsPerTask);

      for (int i = from; i < to; i++) {
        stops.add(allStops.get(i));
      }

      return stops;
    }

    File path = _bundle.getTransferPatternsSourceStopsPath();

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

  private List<StopEntry> loadHubStops() {

    List<StopEntry> hubStops = new ArrayList<StopEntry>();
    File path = _bundle.getHubStopsPath(false);

    if (path.exists()) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = null;
        while ((line = reader.readLine()) != null) {
          int index = line.indexOf('\t');
          if (index != -1)
            line = line.substring(index + 1);
          AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(line);
          StopEntry stop = _transitGraphDao.getStopEntryForId(stopId, true);
          hubStops.add(stop);
        }
      } catch (IOException ex) {
        throw new IllegalStateException("error loading HubStops file: " + path,
            ex);
      }
    }
    return hubStops;
  }

  private Map<StopEntry, Integer> getNearbyStopsAndWalkTimes(StopEntry stop) {

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        stop.getStopLocation(), _nearbyStopsRadius);

    Map<StopEntry, Integer> nearbyStopsAndWalkTimes = new HashMap<StopEntry, Integer>();

    OBATraverseOptions opts = _otpConfigurationService.createTraverseOptions();
    Date now = new Date();

    List<StopEntry> stops = _transitGraphDao.getStopsByLocation(bounds);
    for (StopEntry nearbyStop : stops) {

      if (nearbyStop == stop)
        continue;

      GraphPath path = _itinerariesService.getWalkingItineraryBetweenStops(
          stop, nearbyStop, now, opts);
      if (path == null)
        continue;

      int duration = (int) (path.getDuration() / 1000);
      nearbyStopsAndWalkTimes.put(nearbyStop, duration);
    }

    return nearbyStopsAndWalkTimes;
  }

  private Map<StopEntry, List<StopTimeInstance>> getNearbyStopTimeInstances(
      Iterable<StopEntry> nearbyStops, ServiceDate serviceDate) {
    Map<StopEntry, List<StopTimeInstance>> nearbyStopTimeInstances = new HashMap<StopEntry, List<StopTimeInstance>>();
    for (StopEntry nearbyStop : nearbyStops) {
      List<StopTimeInstance> nearbyInstances = getStopTimeInstancesForStopAndServiceDate(
          nearbyStop, serviceDate);
      nearbyStopTimeInstances.put(nearbyStop, nearbyInstances);
    }
    return nearbyStopTimeInstances;
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

  private List<StopTimeInstance> getStopTimeInstancesForStopAndServiceDate(
      StopEntry stop, ServiceDate serviceDate) {

    Range interval = _stopTimeService.getDepartureForStopAndServiceDate(
        stop.getId(), serviceDate);

    if (interval.isEmpty())
      return Collections.emptyList();

    long tFrom = (long) interval.getMin();
    long tTo = (long) interval.getMax();

    return _stopTimeService.getStopTimeInstancesInTimeRange(stop, new Date(
        tFrom), new Date(tTo), EFrequencyStopTimeBehavior.INCLUDE_INTERPOLATED);
  }

  private void processTree(MultiShortestPathTree spt, StopEntry originStop,
      Map<StopEntry, Counter<List<Pair<StopEntry>>>> pathCountsByStop) {

    Map<SPTVertex, List<StopEntry>> parentsBySPTVertex = new HashMap<SPTVertex, List<StopEntry>>();
    Map<SPTVertex, StopEntry> actualOriginStops = new HashMap<SPTVertex, StopEntry>();

    Map<StopEntry, List<TPOfflineBlockArrivalVertex>> arrivalsByStop = new FactoryMap<StopEntry, List<TPOfflineBlockArrivalVertex>>(
        new ArrayList<TPOfflineBlockArrivalVertex>());

    for (Vertex v : spt.getVertices()) {
      if (!(v instanceof TPOfflineBlockArrivalVertex))
        continue;

      TPOfflineBlockArrivalVertex bav = (TPOfflineBlockArrivalVertex) v;
      StopEntry stop = bav.getStop();
      arrivalsByStop.get(stop).add(bav);
    }

    for (Map.Entry<StopEntry, List<TPOfflineBlockArrivalVertex>> entry : arrivalsByStop.entrySet()) {
      processArrivalsForStop(entry.getKey(), entry.getValue(), originStop, spt,
          actualOriginStops, parentsBySPTVertex, pathCountsByStop);
    }
  }

  private void processArrivalsForStop(StopEntry arrivalStop,
      List<TPOfflineBlockArrivalVertex> arrivals, StopEntry originStop,
      MultiShortestPathTree spt, Map<SPTVertex, StopEntry> actualOriginStops,
      Map<SPTVertex, List<StopEntry>> parentsBySPTVertex,
      Map<StopEntry, Counter<List<Pair<StopEntry>>>> pathCountsByStop) {

    Collections.sort(arrivals);

    Counter<List<Pair<StopEntry>>> pathCounts = new Counter<List<Pair<StopEntry>>>();
    Map<List<Pair<StopEntry>>, List<SPTVertex>> paths = new FactoryMap<List<Pair<StopEntry>>, List<SPTVertex>>(
        new ArrayList<SPTVertex>());

    SortedMap<Long, List<SPTVertex>> m = new TreeMap<Long, List<SPTVertex>>();
    m = FactoryMap.createSorted(m, new ArrayList<SPTVertex>());

    Map<SPTVertex, List<Pair<StopEntry>>> pathsByVertex = new HashMap<SPTVertex, List<Pair<StopEntry>>>();

    boolean verbose = false;//arrivalStop.getId().toString().equals("1_29430");
    if (verbose)
      System.out.println("here!");

    long startTime = 0;
    int totalPathCount = 0;

    boolean atLeastOneArrivalWithProperOrigins = false;

    for (TPOfflineBlockArrivalVertex arrival : arrivals) {

      if (verbose)
        System.out.println(arrival);

      Collection<SPTVertex> sptVertices = pruneSptVertices(spt.getSPTVerticesForVertex(arrival));

      for (SPTVertex sptVertex : sptVertices) {

        if (verbose)
          System.out.println("  " + sptVertex);

        StopEntry actualOriginStop = getActualOriginStop(sptVertex,
            actualOriginStops);
        boolean properOrigins = originStop == actualOriginStop;

        if (verbose)
          System.out.println("  origins=" + properOrigins);

        State state = sptVertex.state;
        boolean cut = state.getStartTime() <= startTime;

        if (!cut) {

          if (properOrigins)
            atLeastOneArrivalWithProperOrigins = true;

          startTime = state.getStartTime();
          totalPathCount++;

          List<Pair<StopEntry>> path = constructTransferPattern(arrival,
              sptVertex, parentsBySPTVertex);

          pathCounts.increment(path);
          paths.get(path).add(sptVertex);

          m.get(state.getTime()).add(sptVertex);
          pathsByVertex.put(sptVertex, path);

          if (verbose)
            System.out.println("  path=" + path);
        }
      }
    }

    if (!atLeastOneArrivalWithProperOrigins)
      return;

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

      if (path.get(0).getFirst() == originStop)
        toKeep.add(path);
    }

    Counter<List<Pair<StopEntry>>> counts = pathCountsByStop.get(arrivalStop);
    for (List<Pair<StopEntry>> path : toKeep) {
      int count = pathCounts.getCount(path);
      counts.increment(path, count);
      if (verbose)
        System.out.println(count + "\t" + path);
    }
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

  private StopEntry getActualOriginStop(SPTVertex sptVertex,
      Map<SPTVertex, StopEntry> actualOriginStops) {

    StopEntry actual = actualOriginStops.get(sptVertex);
    if (actual == null) {
      Vertex v = sptVertex.mirror;
      if (v instanceof TPOfflineNearbyStopsVertex) {
        TPOfflineNearbyStopsVertex nsv = (TPOfflineNearbyStopsVertex) v;
        actual = nsv.getStop();
      } else if (v instanceof TPOfflineOriginVertex) {
        TPOfflineOriginVertex originVertex = (TPOfflineOriginVertex) v;
        actual = originVertex.getStop();
      } else {
        actual = getActualOriginStop(sptVertex.incoming.fromv,
            actualOriginStops);
      }
      actualOriginStops.put(sptVertex, actual);
    }

    return actual;
  }

  private List<Pair<StopEntry>> constructTransferPattern(
      TPOfflineBlockArrivalVertex arrival, SPTVertex sptVertex,
      Map<SPTVertex, List<StopEntry>> parentsBySPTVertex) {

    List<StopEntry> path = computeParentsForSPTVertex(sptVertex,
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
      Map<SPTVertex, List<StopEntry>> parentsBySPTVertex2) {

    List<StopEntry> parent = parentsBySPTVertex2.get(sptVertex);

    if (parent != null)
      return parent;

    Vertex v = sptVertex.mirror;

    if (v instanceof TPOfflineNearbyStopsVertex) {
      TPOfflineNearbyStopsVertex nsv = (TPOfflineNearbyStopsVertex) v;
      return Arrays.asList(nsv.getStop());
    } else if (v instanceof TPOfflineOriginVertex) {
      TPOfflineOriginVertex originVertex = (TPOfflineOriginVertex) v;
      return Arrays.asList(originVertex.getStop());
    }

    SPTEdge sptEdge = sptVertex.incoming;
    Edge payload = sptEdge.payload;

    if (payload instanceof TPOfflineTransferEdge) {

      TPOfflineBlockArrivalVertex fromV = (TPOfflineBlockArrivalVertex) sptEdge.narrative.getFromVertex();
      TPOfflineTransferVertex toV = (TPOfflineTransferVertex) sptEdge.narrative.getToVertex();

      List<StopEntry> incomingPattern = computeParentsForSPTVertex(
          sptEdge.fromv, parentsBySPTVertex2);
      ArrayList<StopEntry> extendedPattern = new ArrayList<StopEntry>(
          incomingPattern);

      extendedPattern.add(fromV.getStop());
      extendedPattern.add(toV.getStop());

      parent = extendedPattern;

    } else {
      parent = computeParentsForSPTVertex(sptEdge.fromv, parentsBySPTVertex2);
    }

    parentsBySPTVertex2.put(sptVertex, parent);

    return parent;
  }

  private static int getSPTVertexDuration(SPTVertex v) {
    State state = v.state;
    return (int) (Math.abs(state.getTime() - state.getStartTime()) / 1000);
  }

  private static class SkipVertexImpl implements SkipTraverseResultStrategy {

    private final Set<StopEntry> _stops = new HashSet<StopEntry>();

    private final StopEntry _originStop;

    private final long _serviceDate;

    public SkipVertexImpl(StopEntry originStop, long serviceDate) {
      _originStop = originStop;
      _serviceDate = serviceDate;
    }
    

    @Override
    public boolean shouldSkipTraversalResult(Vertex origin, Vertex target,
        SPTVertex parent, TraverseResult traverseResult, ShortestPathTree spt,
        TraverseOptions traverseOptions) {
      
      EdgeNarrative narrative = traverseResult.getEdgeNarrative();
      Vertex vertex = narrative.getToVertex();

      /**
       * We prune any arrivals that loop back to the origin stop
       */
      if (vertex instanceof TPOfflineBlockArrivalVertex) {
        TPOfflineBlockArrivalVertex bav = (TPOfflineBlockArrivalVertex) vertex;
        StopTimeInstance instance = bav.getInstance();
        if (instance.getStop() == _originStop)
          return true;
      }

      /**
       * Skip a vertex that has moved on to the next service date
       */
      if (vertex instanceof HasStopTimeInstanceTransitVertex) {
        HasStopTimeInstanceTransitVertex v = (HasStopTimeInstanceTransitVertex) vertex;
        StopTimeInstance instance = v.getInstance();
        if (instance.getServiceDate() > _serviceDate + 12 * 60 * 60 * 1000)
          return true;
      }

      /**
       * Print the visited stop count as a show of progress
       */
      if (vertex instanceof HasStopTransitVertex) {
        HasStopTransitVertex v = (HasStopTransitVertex) vertex;
        StopEntry stop = v.getStop();
        if (_stops.add(stop) && _stops.size() % 100 == 0) {
          System.out.println("stops=" + _stops.size());
          if (_stops.size() == 13900)
            System.out.println("here");
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
