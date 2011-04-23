package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData.Editor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
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
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transitTime = computeTransitTime();

    Editor s1 = s0.edit();
    s1.incrementTimeInSeconds(transitTime);
    s1.incrementNumBoardings();
    s1.setEverBoarded(true);

    EdgeNarrative narrative = createNarrative();

    return new TraverseResult(transitTime, s1.createState(), narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transitTime = computeTransitTime();

    Editor s1 = s0.edit();
    s1.incrementTimeInSeconds(-transitTime);
    s1.incrementNumBoardings();
    s1.setEverBoarded(true);

    EdgeNarrative narrative = createNarrative();

    return new TraverseResult(transitTime, s1.createState(), narrative);
  }

  /****
   * 
   ****/

  private int computeTransitTime() {
    long departure = _departure.getBestDepartureTime();
    long arrival = _arrival.getBestArrivalTime();
    return (int) ((arrival - departure) / 1000);
  }

  private EdgeNarrative createNarrative() {
    Vertex fromV = new TPBlockDepartureVertex(_context, _pathState, _departure,
        _arrival);
    Vertex toV = new TPBlockArrivalVertex(_context, _pathState, _departure,
        _arrival);
    return new EdgeNarrativeImpl(fromV, toV);
  }
}
