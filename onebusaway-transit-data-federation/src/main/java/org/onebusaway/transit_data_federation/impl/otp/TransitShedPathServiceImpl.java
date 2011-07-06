package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.services.otp.TransitShedPathService;
import org.opentripplanner.routing.algorithm.GraphLibrary;
import org.opentripplanner.routing.algorithm.strategies.ExtraEdgesStrategy;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.pqueue.FibHeap;
import org.opentripplanner.routing.services.GraphService;
import org.opentripplanner.routing.spt.BasicShortestPathTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TransitShedPathServiceImpl implements TransitShedPathService {

  private VertexSkipStrategy _vertexSkipStrategy = new TransitShedVertexSkipStrategy();

  private GraphService _graphService;

  @Autowired
  public void setGraphService(GraphService graphService) {
    _graphService = graphService;
  }

  @Override
  public BasicShortestPathTree getTransitShed(Vertex origin, State originState,
      TraverseOptions options) {

    BasicShortestPathTree spt = new BasicShortestPathTree();

    Graph graph = _graphService.getGraph();

    FibHeap<State> queue = new FibHeap<State>(graph.getVertices().size());

    spt.add(originState);
    queue.insert(originState, originState.getWeight());

    HashSet<Vertex> closed = new HashSet<Vertex>();

    ExtraEdgesStrategy extraEdgesStrategy = options.extraEdgesStrategy;

    Map<Vertex, List<Edge>> extraEdges = new HashMap<Vertex, List<Edge>>();
    extraEdgesStrategy.addOutgoingEdgesForOrigin(extraEdges, origin);
    if (extraEdges.isEmpty())
      extraEdges = Collections.emptyMap();

    while (!queue.empty()) {

      State state = queue.extract_min();

      Vertex fromVertex = state.getVertex();

      closed.add(fromVertex);

      if (_vertexSkipStrategy.isVertexSkippedInFowardSearch(origin,
          originState, state, options))
        continue;

      Iterable<Edge> outgoing = GraphLibrary.getOutgoingEdges(graph,
          fromVertex, extraEdges);

      for (Edge edge : outgoing) {

        for (State wr = edge.traverse(state); wr != null; wr = wr.getNextResult()) {

          EdgeNarrative er = wr.getBackEdgeNarrative();
          Vertex toVertex = er.getToVertex();

          if (!closed.contains(toVertex)) {

            if (spt.add(wr))
              queue.insert(wr, wr.getWeight());
          }
        }
      }
    }
    return spt;
  }

}
