package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.Counter;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.opentripplanner.routing.algorithm.GenericDijkstra;
import org.opentripplanner.routing.algorithm.strategies.WeightLimitSearchTerminationStrategy;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.pqueue.PriorityQueueImpl;
import org.opentripplanner.routing.spt.SPTEdge;
import org.opentripplanner.routing.spt.SPTVertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractHubTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(AbstractHubTask.class);

  protected TransitGraphDao _transitGraphDao;

  protected StopHopService _stopHopService;

  protected FederatedTransitDataBundle _bundle;

  protected double _stopSubsetRatio = 0.1;

  protected int _stopSubsetCount = 0;

  protected int _maxWeight = 30 * 60;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setStopHopService(StopHopService stopHopService) {
    _stopHopService = stopHopService;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void setStopSubsetRatio(double stopSubsetRatio) {
    _stopSubsetRatio = stopSubsetRatio;
  }

  public void setStopSubsetCount(int stopSubsetCount) {
    _stopSubsetCount = stopSubsetCount;
  }
  
  public void setMaxWeight(int maxWeight) {
    _maxWeight = maxWeight;
  }

  protected Counter<StopEntry> countStops() {

    List<StopEntry> sampled = getRandomSamplingOfSourceStops();

    TraverseOptions options = new TraverseOptions();
    options.priorityQueueFactory = PriorityQueueImpl.FACTORY;

    Graph graph = new Graph();

    GraphContext context = new GraphContext();
    context.setTransitGraphDao(_transitGraphDao);
    context.setStopHopService(_stopHopService);

    Counter<StopEntry> counts = new Counter<StopEntry>();

    int index = 0;

    for (StopEntry stop : sampled) {

      _log.info("index=" + index + "/" + sampled.size());
      index++;

      HubVertex v = new HubVertex(context, stop);

      GenericDijkstra d = new GenericDijkstra(graph, options);
      d.setSearchTerminationStrategy(new WeightLimitSearchTerminationStrategy(
          _maxWeight));

      ShortestPathTree spt = d.getShortestPathTree(v, new State());

      for (SPTVertex sptVertex : spt.getVertices()) {
        while (sptVertex != null) {
          Vertex vertex = sptVertex.mirror;
          HubVertex hubVertex = (HubVertex) vertex;
          StopEntry vStop = hubVertex.getStop();
          counts.increment(vStop);
          SPTEdge edge = sptVertex.incoming;
          if (edge != null)
            sptVertex = edge.fromv;
          else
            sptVertex = null;
        }
      }
    }
    return counts;
  }

  protected void writeCountsToPath(Counter<StopEntry> counts, File path) {
    try {

      PrintWriter writer = new PrintWriter(new FileWriter(path));

      List<StopEntry> keys = counts.getSortedKeys();

      for (StopEntry key : keys) {
        int count = counts.getCount(key);
        writer.println(count + "\t" + key.getId());
      }

      writer.close();

    } catch (IOException ex) {
      throw new IllegalStateException("error writing hub stop info", ex);
    }
  }

  protected List<StopEntry> getRandomSamplingOfSourceStops() {

    List<StopEntry> allStops = _transitGraphDao.getAllStops();

    List<StopEntry> sampled = new ArrayList<StopEntry>(
        (int) (allStops.size() * _stopSubsetRatio));

    for (StopEntry stop : allStops) {

      if (Math.random() <= _stopSubsetRatio)
        sampled.add(stop);

      if (_stopSubsetCount > 0 && sampled.size() >= _stopSubsetCount)
        break;
    }

    return sampled;
  }

}
