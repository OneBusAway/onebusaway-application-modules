package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;

public class BlockDwellEdge extends AbstractEdge {

  private final ArrivalAndDepartureInstance _instance;

  public BlockDwellEdge(GraphContext context,
      ArrivalAndDepartureInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public State traverse(State s0) {

    BlockStopTimeEntry bst = _instance.getBlockStopTime();
    StopTimeEntry stopTime = bst.getStopTime();
    int dwellTime = stopTime.getSlackTime();

    EdgeNarrative narrative = createNarrative(s0);
    StateEditor edit = s0.edit(this, narrative);
    edit.incrementTimeInSeconds(dwellTime);
    edit.incrementWeight(dwellTime);

    return edit.makeState();
  }

  private EdgeNarrative createNarrative(State s0) {
    BlockArrivalVertex fromVertex = new BlockArrivalVertex(_context, _instance);
    BlockDepartureVertex toVertex = new BlockDepartureVertex(_context,
        _instance);
    return narrative(s0, fromVertex, toVertex);
  }
}
