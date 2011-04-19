package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData.OBAEditor;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

/**
 * A transit vehicle's journey between departure at one stop and arrival at the
 * next. This version represents a set of such journeys specified by a
 * TripPattern.
 */
public class BlockForwardHopEdge extends AbstractEdge {

  private static final long serialVersionUID = 1L;

  private final ArrivalAndDepartureInstance _from;

  public BlockForwardHopEdge(GraphContext context,
      ArrivalAndDepartureInstance from) {
    super(context);

    if (from == null)
      throw new IllegalArgumentException("from cannot be null");

    _from = from;
  }

  @Override
  public TraverseResult traverse(State state0, TraverseOptions wo) {

    OBATraverseOptions obaOpts = (OBATraverseOptions) wo;
    if (obaOpts.extraSpecialMode)
      return extraSpecialMode(state0, obaOpts);

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();

    TraverseResult r = null;

    OBAStateData data = (OBAStateData) state0.getData();

    int maxBlockSequence = data.getMaxBlockSequence();

    if (maxBlockSequence < 0) {
      ArrivalAndDepartureInstance nextTransferStop = service.getNextTransferStopArrivalAndDeparture(_from);
      if (nextTransferStop != null) {

        long departure = _from.getBestDepartureTime();
        long arrival = nextTransferStop.getBestArrivalTime();
        int runningTime = (int) ((arrival - departure) / 1000);

        State state1 = state0.incrementTimeInSeconds(runningTime);

        Vertex fromVertex = new BlockDepartureVertex(_context, _from);
        Vertex toVertex = new BlockArrivalVertex(_context, nextTransferStop);
        EdgeNarrative narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

        TraverseResult tr = new TraverseResult(runningTime, state1, narrative);
        r = tr.addToExistingResultChain(r);

        maxBlockSequence = nextTransferStop.getBlockStopTime().getBlockSequence();
      } else {
        maxBlockSequence = Integer.MAX_VALUE;
      }
    }

    ArrivalAndDepartureInstance nextStop = service.getNextStopArrivalAndDeparture(_from);

    if (nextStop != null
        && nextStop.getBlockStopTime().getBlockSequence() < maxBlockSequence) {

      long departure = _from.getBestDepartureTime();
      long arrival = nextStop.getBestArrivalTime();
      int runningTime = (int) ((arrival - departure) / 1000);

      State s1 = null;
      if (data.getMaxBlockSequence() >= 0) {
        s1 = state0.incrementTimeInSeconds(runningTime);
      } else {
        OBAEditor editor = (OBAEditor) state0.edit();
        editor.incrementTimeInSeconds(runningTime);
        editor.setMaxBlockSequence(maxBlockSequence);
        s1 = editor.createState();
      }

      Vertex fromVertex = new BlockDepartureVertex(_context, _from);
      Vertex toVertex = new BlockArrivalVertex(_context, nextStop);
      EdgeNarrative narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

      TraverseResult tr = new TraverseResult(runningTime, s1, narrative);
      r = tr.addToExistingResultChain(r);
    }

    return r;
  }

  private TraverseResult extraSpecialMode(State state0,
      OBATraverseOptions obaOpts) {

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();
    ArrivalAndDepartureInstance nextStop = service.getNextStopArrivalAndDeparture(_from);
    
    if( nextStop == null)
      return null;

    long departure = _from.getBestDepartureTime();
    long arrival = nextStop.getBestArrivalTime();
    int runningTime = (int) ((arrival - departure) / 1000);

    State state1 = state0.incrementTimeInSeconds(runningTime);

    Vertex fromVertex = new BlockDepartureVertex(_context, _from);
    Vertex toVertex = new BlockArrivalVertex(_context, nextStop);
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(runningTime, state1, narrative);
  }

  @Override
  public TraverseResult traverseBack(State state0, TraverseOptions wo) {

    int runningTime = (int) ((state0.getTime() - _from.getBestDepartureTime()) / 1000);
    State state1 = state0.setTime(_from.getBestDepartureTime());

    Vertex fromVertex = new BlockDepartureVertex(_context, _from);
    Vertex toVertex = null;
    EdgeNarrative narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(runningTime, state1, narrative);
  }
}
