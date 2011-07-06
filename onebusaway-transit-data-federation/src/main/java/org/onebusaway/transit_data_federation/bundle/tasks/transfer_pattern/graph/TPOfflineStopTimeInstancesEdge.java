package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseOptions;
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
  public State traverse(State s0) {
    TraverseOptions options = s0.getOptions();

    if (options.isArriveBy())
      return null;

    State results = null;

    for (StopTimeInstance instance : _instances) {

      TPOfflineTransferVertex vTransfer = new TPOfflineTransferVertex(_context,
          instance);
      EdgeNarrative nTransfer = narrative(s0, _vFrom, vTransfer);
      StateEditor edit = s0.edit(this, nTransfer);

      long t = instance.getDepartureTime();
      edit.setTime(t);

      long startTime = t - _transferTime * 1000;
      edit.setStartTime(startTime);

      double w = _transferTime * options.walkReluctance;
      edit.incrementWeight(w);

      State rTransfer = edit.makeState();
      results = rTransfer.addToExistingResultChain(results);
    }

    return results;
  }
}