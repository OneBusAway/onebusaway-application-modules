package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;

public class TPOfflineBlockDwellEdge extends AbstractEdge {

  private final StopTimeInstance _instance;

  public TPOfflineBlockDwellEdge(GraphContext context, StopTimeInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public State traverse(State s0) {

    BlockStopTimeEntry bst = _instance.getStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    EdgeNarrative narrative = createNarrative(s0);
    StateEditor edit = s0.edit(this, narrative);
    edit.incrementTimeInSeconds(dwellTime);
    edit.incrementWeight(dwellTime);
    return edit.makeState();
  }

  private EdgeNarrative createNarrative(State s0) {
    TPOfflineBlockArrivalVertex fromVertex = new TPOfflineBlockArrivalVertex(
        _context, _instance);
    TPOfflineBlockDepartureVertex toVertex = new TPOfflineBlockDepartureVertex(
        _context, _instance);
    return narrative(s0, fromVertex, toVertex);
  }
}
