package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractStopVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.HasEdges;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;

public class TPOfflineOriginVertex extends AbstractStopVertex implements HasEdges {

  private List<StopTimeInstance> _instances;

  public TPOfflineOriginVertex(GraphContext context, StopEntry stop,
      List<StopTimeInstance> instances) {
    super(context, stop);
    _instances = instances;
  }

  @Override
  public int getDegreeIn() {
    return getIncoming().size();
  }

  @Override
  public int getDegreeOut() {
    return getOutgoing().size();
  }

  @Override
  public Collection<Edge> getIncoming() {
    return Collections.emptyList();
  }

  @Override
  public Collection<Edge> getOutgoing() {
    return Arrays.asList((Edge) new MyEdge(_context));
  }

  private class MyEdge extends AbstractEdge {

    public MyEdge(GraphContext context) {
      super(context);
    }

    @Override
    public TraverseResult traverse(State s0, TraverseOptions options)
        throws NegativeWeightException {

      TraverseResult results = null;

      for (StopTimeInstance instance : _instances) {

        State state = new State(instance.getDepartureTime(), new OBAStateData());

        TPOfflineTransferVertex vTransfer = new TPOfflineTransferVertex(_context, instance);
        EdgeNarrative nTransfer = new EdgeNarrativeImpl(TPOfflineOriginVertex.this,
            vTransfer);
        TraverseResult rTransfer = new TraverseResult(0, state, nTransfer);
        results = rTransfer.addToExistingResultChain(results);
      }

      return results;
    }

    @Override
    public TraverseResult traverseBack(State s0, TraverseOptions options)
        throws NegativeWeightException {
      return null;
    }
  }
}
