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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPArrivalAndTransferEdge;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPArrivalVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPBlockArrivalVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPBlockDepartureVertex;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph.TPDepartureVertex;
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

  private TransitGraphDao _transitGraphDao;

  private GraphService _graphService;

  private OTPConfigurationService _otpConfigurationService;

  private StopScheduleService _stopScheduleService;

  private StopTimeService _stopTimeService;

  private File _stopsFile;

  private int _serviceDateCount = 4;

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

  public void setStopsFile(File stopsFile) {
    _stopsFile = stopsFile;
  }

  public void setServiceDateCount(int serviceDateCount) {
    _serviceDateCount = serviceDateCount;
  }

  @Override
  public void run() {
    /**
     * 1) Load hub stops
     * 
     * 2) Compute all unique service day combos
     * 
     * 3) For each stop:
     * 
     * 2a) Dijkstra - departures in time window (service day combos) to all
     * other stops
     * 
     * 2b) Look for transfer stops in shortest paths chains
     */

    List<StopEntry> stops = loadHubStops();

    Graph graph = _graphService.getGraph();
    GraphContext context = _otpConfigurationService.createGraphContext();

    for (StopEntry stop : stops) {

      System.out.println("stop=" + stop.getId());

      TransferPattern pattern = new TransferPattern(stop);

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

        int instanceIndex = 0;
        long tTotal = 0;

        System.out.println("      instances=" + instances.size());

        for (StopTimeInstance instance : instances) {

          long tProcStart = System.currentTimeMillis();

          OBATraverseOptions options = _otpConfigurationService.createTraverseOptions();

          options.maxComputationTime = -1;
          options.shortestPathTreeFactory = MultiShortestPathTree.FACTORY;
          options.priorityQueueFactory = PriorityQueueImpl.FACTORY;
          options.waitAtBeginningFactor = 1.0;
          options.extraSpecialMode = true;

          GenericDijkstra dijkstra = new GenericDijkstra(graph, options);
          dijkstra.setSkipVertexStrategy(new VertexCounter(stop));

          TPBlockDepartureVertex origin = new TPBlockDepartureVertex(context,
              instance);
          State state = new State(instance.getDepartureTime(),
              new OBAStateData());

          MultiShortestPathTree spt = (MultiShortestPathTree) dijkstra.getShortestPathTree(
              origin, state);

          processTree(spt, pattern, stop);

          long tProcEnd = System.currentTimeMillis();

          instanceIndex++;
          tTotal += (tProcEnd - tProcStart);

          long tPer = tTotal / instanceIndex;
          int tRemaining = (int) (((instances.size() - instanceIndex) * tPer) / (60 * 1000));

          System.out.println("        instance=" + instanceIndex + " tPer="
              + tPer + " tRemaining=" + tRemaining + " mins");

          if (instanceIndex == 10)
            break;
        }

        break;
      }

      avgPaths(pattern, stop);
      dumpTransferPattern(pattern);
    }
  }

  private void avgPaths(TransferPattern pattern, StopEntry origin) {
    DoubleArrayList values = new DoubleArrayList();
    for (StopEntry stop : pattern.getStops()) {
      List<List<Pair<StopEntry>>> paths = pattern.getPathsForStop(stop);
      values.add(paths.size());
    }
    values.sort();
    System.out.println("    mu=" + Descriptive.mean(values));
    System.out.println("median=" + Descriptive.median(values));
    System.out.println("   max=" + Descriptive.max(values));
  }

  private List<StopEntry> loadHubStops() {

    List<StopEntry> stops = new ArrayList<StopEntry>();

    try {
      BufferedReader reader = new BufferedReader(new FileReader(_stopsFile));
      String line = null;
      while ((line = reader.readLine()) != null) {
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(line);
        StopEntry stop = _transitGraphDao.getStopEntryForId(stopId);
        if (stop == null)
          throw new NoSuchStopServiceException(line);
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

  private void processTree(MultiShortestPathTree spt, TransferPattern pattern,
      StopEntry originStop) {

    Map<SPTVertex, List<StopEntry>> parentsBySPTVertex = new HashMap<SPTVertex, List<StopEntry>>();
    
    DoubleArrayList counts = new DoubleArrayList();

    for (Vertex v : spt.getVertices()) {

      if (!(v instanceof TPArrivalVertex))
        continue;

      TPArrivalVertex av = (TPArrivalVertex) v;
      Collection<SPTVertex> sptVertices = spt.getSPTVerticesForVertex(v);
      
      counts.add(sptVertices.size());

      for (SPTVertex sptVertex : sptVertices) {

        List<StopEntry> path = computeParentForSPTVertex(sptVertex, spt,
            pattern, originStop, parentsBySPTVertex);
        path = new ArrayList<StopEntry>(path);
        path.add(av.getStop());
        if (path.size() % 2 != 0)
          throw new IllegalArgumentException();
        List<Pair<StopEntry>> pairs = new ArrayList<Pair<StopEntry>>();
        for (int i = 0; i < path.size(); i += 2) {
          pairs.add(Tuples.pair(path.get(i), path.get(i + 1)));
        }
        pattern.addPath(pairs);
      }
    }
    
    counts.sort();
    
    System.out.println("    mu=" + Descriptive.mean(counts));
    System.out.println("median=" + Descriptive.median(counts));
    System.out.println("   max=" + Descriptive.max(counts));
  }

  private List<StopEntry> computeParentForSPTVertex(SPTVertex sptVertex,
      MultiShortestPathTree spt, TransferPattern pattern, StopEntry originStop,
      Map<SPTVertex, List<StopEntry>> parentsBySPTVertex) {

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
        TPDepartureVertex toV = (TPDepartureVertex) sptEdge.narrative.getToVertex();

        List<StopEntry> incomingPattern = computeParentForSPTVertex(
            sptEdge.fromv, spt, pattern, originStop, parentsBySPTVertex);
        ArrayList<StopEntry> extendedPattern = new ArrayList<StopEntry>(
            incomingPattern);

        extendedPattern.add(fromV.getStop());
        extendedPattern.add(toV.getStop());

        parent = extendedPattern;

      } else {

        parent = computeParentForSPTVertex(sptEdge.fromv, spt, pattern,
            originStop, parentsBySPTVertex);
      }
    }

    parentsBySPTVertex.put(sptVertex, parent);

    return parent;
  }

  private void dumpTransferPattern(TransferPattern pattern) {
    try {
      PrintWriter out = new PrintWriter("/tmp/transfer-pattern.txt");

      out.close();
    } catch (IOException ex) {
      throw new IllegalStateException("error dumping transfer pattern", ex);
    }
  }

  private static class VertexCounter implements SkipVertexStrategy {

    private final StopEntry _stop;

    public VertexCounter(StopEntry stop) {
      _stop = stop;
    }

    @Override
    public boolean shouldSkipVertex(Vertex origin, Vertex target,
        SPTVertex parent, Vertex current, State state, ShortestPathTree spt,
        TraverseOptions traverseOptions) {

      /**
       * Let's only wait so long...
       */
      long t0 = parent.state.getTime();
      long t1 = state.getTime();
      if (Math.abs(t1 - t0) > 8 * 60 * 60 * 1000)
        return true;

      if (current instanceof HasStopTransitVertex) {
        HasStopTransitVertex v = (HasStopTransitVertex) current;
        StopEntry stop = v.getStop();
        if (stop == _stop)
          return true;
      }

      return false;
    }

  }
}
