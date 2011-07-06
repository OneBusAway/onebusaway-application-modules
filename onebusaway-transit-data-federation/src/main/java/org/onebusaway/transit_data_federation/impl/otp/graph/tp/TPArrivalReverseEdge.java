package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateEditor;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDeparturePairQuery;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class TPArrivalReverseEdge extends AbstractEdge {

  private final TPState _pathState;

  public TPArrivalReverseEdge(GraphContext context, TPState pathState) {
    super(context);
    _pathState = pathState;
  }

  @Override
  public State traverse(State s0) {
    TraverseOptions options = s0.getOptions();
    if (options.isArriveBy())
      return traverseReverse(s0);
    else
      return traverseForward(s0);
  }

  private State traverseForward(State s0) {
    /**
     * We alight from our current vehicle to the stop. However, we don't
     * actually know which vehicle. Hopefully this method will only ever be
     * called in the GraphPath.optimize(), where the traverseBack() method has
     * previously been called.
     */
    TPDepartureVertex fromVertex = null;
    TPArrivalVertex toVertex = new TPArrivalVertex(_context, _pathState);
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);
    return s0.edit(this, narrative).makeState();
  }

  private State traverseReverse(State s0) {

    OBATraverseOptions obaOpts = (OBATraverseOptions) s0.getOptions();

    ArrivalAndDepartureService adService = _context.getArrivalAndDepartureService();

    Vertex toV = new TPArrivalVertex(_context, _pathState);

    Pair<StopEntry> stopPair = _pathState.getStops();

    TargetTime targetTime = new TargetTime(s0.getTime(), obaOpts.currentTime);

    ArrivalAndDeparturePairQuery query = new ArrivalAndDeparturePairQuery();
    query.setResultCount(obaOpts.numItineraries);
    query.setApplyRealTime(obaOpts.useRealtime);
    query.setIncludePrivateService(false);

    /**
     * Recall that the stopPair is in reverse order (toStop => fromStop)
     */
    List<Pair<ArrivalAndDepartureInstance>> instances = adService.getPreviousArrivalsForStopPair(
        stopPair.getSecond(), stopPair.getFirst(), targetTime, query);

    State results = null;

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

      EdgeNarrative narrative = narrative(s0, fromV, toV);

      OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);

      int dwellTime = computeWaitTime(s0, pair);
      edit.incrementTimeInSeconds(-dwellTime);

      double w = ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);
      edit.incrementWeight(w);

      if (arrival.getBlockSequence() != null)
        edit.appendTripSequence(arrival.getBlockSequence());
      else
        edit.appendTripSequence(arrival.getBlockTrip());

      State s1 = edit.makeState();
      results = s1.addToExistingResultChain(results);
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
