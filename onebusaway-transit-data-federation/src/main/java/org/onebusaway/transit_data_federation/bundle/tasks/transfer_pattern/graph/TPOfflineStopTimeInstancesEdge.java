package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

class TPOfflineStopTimeInstancesEdge extends AbstractEdge {

  private final List<StopTimeInstance> _instances;

  private final Vertex _vFrom;

  private final int _transferTime;

  public TPOfflineStopTimeInstancesEdge(GraphContext context, Vertex vFrom,
      List<StopTimeInstance> instances, int transferTime) {
    super(context);
    _vFrom = vFrom;
    _instances = instances;
    _transferTime = transferTime;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult results = null;

    for (StopTimeInstance instance : _instances) {

      long t = instance.getDepartureTime();
      long startTime = t - _transferTime * 1000;
      State state = new State(startTime, t, new OBAStateData());

      double w = _transferTime * options.walkReluctance;

      TPOfflineTransferVertex vTransfer = new TPOfflineTransferVertex(_context,
          instance);
      EdgeNarrative nTransfer = new EdgeNarrativeImpl(_vFrom, vTransfer);
      TraverseResult rTransfer = new TraverseResult(w, state, nTransfer);
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