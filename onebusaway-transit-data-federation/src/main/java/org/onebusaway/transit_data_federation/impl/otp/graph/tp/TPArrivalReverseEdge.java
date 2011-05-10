package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData.OBAEditor;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class TPArrivalReverseEdge extends AbstractEdge {

  private final TPState _pathState;

  public TPArrivalReverseEdge(GraphContext context, TPState pathState) {
    super(context);
    _pathState = pathState;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * We alight from our current vehicle to the stop. However, we don't
     * actually know which vehicle. Hopefully this method will only ever be
     * called in the GraphPath.optimize(), where the traverseBack() method has
     * previously been called.
     */
    TPDepartureVertex fromVertex = null;
    TPArrivalVertex toVertex = new TPArrivalVertex(_context, _pathState);
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);
    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    OBATraverseOptions obaOpts = (OBATraverseOptions) options;

    ArrivalAndDepartureService adService = _context.getArrivalAndDepartureService();

    Vertex toV = new TPArrivalVertex(_context, _pathState);

    Pair<StopEntry> stopPair = _pathState.getStops();

    TargetTime targetTime = new TargetTime(s0.getTime(), obaOpts.currentTime);

    /**
     * Recall that the stopPair is in reverse order (toStop => fromStop)
     */
    List<Pair<ArrivalAndDepartureInstance>> instances = adService.getPreviousArrivalsForStopPair(
        stopPair.getSecond(), stopPair.getFirst(), targetTime,
        options.numItineraries, obaOpts.useRealtime);

    TraverseResult results = null;

    for (Pair<ArrivalAndDepartureInstance> pair : instances) {

      /**
       * For now, we skip real-time arrival that might have been included that
       * are beyond our range (ex. vehicle running late)
       */
      ArrivalAndDepartureInstance arrival = pair.getSecond();
      if (arrival.getBestArrivalTime() > s0.getTime())
        break;

      Vertex fromV = new TPBlockArrivalVertex(_context, _pathState,
          pair.getFirst(), pair.getSecond());

      int dwellTime = computeWaitTime(s0, pair);

      double w = ItineraryWeightingLibrary.computeWeightForWait(options,
          dwellTime, s0);

      OBAEditor s1 = (OBAEditor) s0.edit();
      s1.incrementTimeInSeconds(-dwellTime);

      if (arrival.getBlockSequence() != null)
        s1.appendTripSequence(arrival.getBlockSequence());
      else
        s1.appendTripSequence(arrival.getBlockTrip());

      EdgeNarrative narrative = new EdgeNarrativeImpl(fromV, toV);
      TraverseResult r = new TraverseResult(w, s1.createState(), narrative);
      results = r.addToExistingResultChain(results);
    }

    return results;
  }

  /****
   * 
   ****/

  private int computeWaitTime(State s0, Pair<ArrivalAndDepartureInstance> pair) {
    return (int) ((s0.getTime() - pair.getSecond().getBestArrivalTime()) / 1000);
  }
}
