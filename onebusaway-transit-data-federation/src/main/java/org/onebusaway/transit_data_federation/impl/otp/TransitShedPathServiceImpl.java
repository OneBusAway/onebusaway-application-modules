package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.services.otp.TransitShedPathService;
import org.opentripplanner.routing.algorithm.ExtraEdgesStrategy;
import org.opentripplanner.routing.algorithm.GraphLibrary;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.contraction.ContractionHierarchySet;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.pqueue.FibHeap;
import org.opentripplanner.routing.spt.BasicShortestPathTree;
import org.opentripplanner.routing.spt.SPTVertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TransitShedPathServiceImpl implements TransitShedPathService {

  private SearchTerminationStrategy _terminationStrategy = new TransitShedSearchTerminationStrategy();

  private VertexSkipStrategy _vertexSkipStrategy = new TransitShetVertexSkipStrategy();

  private ContractionHierarchySet _hierarchies;

  @Autowired
  public void setHierarchies(ContractionHierarchySet hierarchies) {
    _hierarchies = hierarchies;
  }

  @Override
  public BasicShortestPathTree getTransitShed(Vertex origin, State originState,
      TraverseOptions options) {

    BasicShortestPathTree spt = new BasicShortestPathTree();

    Graph graph = _hierarchies.getGraph();

    FibHeap<SPTVertex> queue = new FibHeap<SPTVertex>(
        graph.getVertices().size());

    SPTVertex sptOrigin = spt.addVertex(origin, originState, 0, options);
    queue.insert(sptOrigin, sptOrigin.weightSum);

    HashSet<Vertex> closed = new HashSet<Vertex>();

    ExtraEdgesStrategy extraEdgesStrategy = options.extraEdgesStrategy;

    Map<Vertex, List<Edge>> extraEdges = new HashMap<Vertex, List<Edge>>();
    extraEdgesStrategy.addOutgoingEdgesForOrigin(extraEdges, origin);
    if (extraEdges.isEmpty())
      extraEdges = Collections.emptyMap();

    while (!queue.empty()) {

      SPTVertex spt_u = queue.peek_min();

      if (_terminationStrategy.terminateSearch(origin, originState, spt_u,
          options)) {
        return spt;
      }

      if (_vertexSkipStrategy.isVertexSkippedInFowardSearch(spt_u))
        continue;

      Vertex fromVertex = spt_u.mirror;

      queue.extract_min();

      closed.add(fromVertex);

      Iterable<Edge> outgoing = GraphLibrary.getOutgoingEdges(graph,
          fromVertex, extraEdges);

      for (Edge edge : outgoing) {
        State state = spt_u.state;

        TraverseResult wr = edge.traverse(state, options);
        // When an edge leads nowhere (as indicated by returning NULL), the
        // iteration is
        // over.
        if (wr == null) {
          continue;
        }

        if (wr.weight < 0) {
          throw new NegativeWeightException(String.valueOf(wr.weight));
        }

        EdgeNarrative er = wr.getEdgeNarrative();
        Vertex toVertex = er.getToVertex();
        if (closed.contains(toVertex)) {
          continue;
        }

        double new_w = spt_u.weightSum + wr.weight;

        SPTVertex spt_v = spt.addVertex(toVertex, wr.state, new_w, options,
            spt_u.hops + 1);

        if (spt_v != null) {
          spt_v.setParent(spt_u, edge, er);
          queue.insert_or_dec_key(spt_v, new_w);
        }
      }
    }
    return spt;
  }

}
