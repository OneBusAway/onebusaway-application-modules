package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.Vertex;

public class TPBlockHopEdge extends AbstractEdge {

  private final TPState _pathState;

  private final ArrivalAndDepartureInstance _departure;

  private final ArrivalAndDepartureInstance _arrival;

  public TPBlockHopEdge(GraphContext context, TPState pathState,
      ArrivalAndDepartureInstance departure, ArrivalAndDepartureInstance arrival) {
    super(context);
    _pathState = pathState;
    _departure = departure;
    _arrival = arrival;
  }

  @Override
  public State traverse(State s0) {

    int transitTime = computeTransitTime();

    EdgeNarrative narrative = createNarrative(s0);
    StateEditor edit = s0.edit(this, narrative);

    edit.incrementTimeInSeconds(transitTime);
    edit.incrementNumBoardings();
    edit.setEverBoarded(true);
    edit.incrementWeight(transitTime);

    return edit.makeState();
  }

  /****
   * 
   ****/

  private int computeTransitTime() {
    long departure = _departure.getBestDepartureTime();
    long arrival = _arrival.getBestArrivalTime();
    return (int) ((arrival - departure) / 1000);
  }

  private EdgeNarrative createNarrative(State s0) {
    Vertex fromV = new TPBlockDepartureVertex(_context, _pathState, _departure,
        _arrival);
    Vertex toV = new TPBlockArrivalVertex(_context, _pathState, _departure,
        _arrival);
    return narrative(s0, fromV, toV);
  }
}
