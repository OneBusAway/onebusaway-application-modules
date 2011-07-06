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

public class TPDepartureEdge extends AbstractEdge {

  private TPState _pathState;

  public TPDepartureEdge(GraphContext context, TPState pathState) {
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

    OBATraverseOptions obaOpts = (OBATraverseOptions) s0.getOptions();

    ArrivalAndDepartureService adService = _context.getArrivalAndDepartureService();

    Vertex fromV = new TPDepartureVertex(_context, _pathState);

    Pair<StopEntry> stopPair = _pathState.getStops();

    TargetTime targetTime = new TargetTime(s0.getTime(), obaOpts.currentTime);

    ArrivalAndDeparturePairQuery query = new ArrivalAndDeparturePairQuery();
    query.setResultCount(obaOpts.numItineraries);
    query.setApplyRealTime(obaOpts.useRealtime);
    query.setIncludePrivateService(false);
    if (s0.getNumBoardings() == 0)
      query.setLookaheadTime(obaOpts.lookaheadTime);

    List<Pair<ArrivalAndDepartureInstance>> instances = adService.getNextDeparturesForStopPair(
        stopPair.getFirst(), stopPair.getSecond(), targetTime, query);

    State results = null;

    for (Pair<ArrivalAndDepartureInstance> pair : instances) {

      ArrivalAndDepartureInstance departure = pair.getFirst();
      if (departure.getBestDepartureTime() < s0.getTime()
          - query.getLookaheadTime() * 1000)
        continue;

      Vertex toV = new TPBlockDepartureVertex(_context, _pathState, departure,
          pair.getSecond());

      int dwellTime = computeWaitTime(s0, pair);

      double w = ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);

      EdgeNarrative narrative = narrative(s0, fromV, toV);
      OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);
      edit.setTime(departure.getBestDepartureTime());
      edit.incrementWeight(w);

      /**
       * If the departure time is less than the starting state time, it must
       * mean the departure was included as determined by the lookahead
       * parameter. Thus, we indicate that we have a lookahead itinerary.
       */
      if (departure.getBestDepartureTime() < s0.getTime())
        edit.setLookaheadItinerary();

      if (departure.getBlockSequence() != null)
        edit.appendTripSequence(departure.getBlockSequence());
      else
        edit.appendTripSequence(departure.getBlockTrip());

      State s1 = edit.makeState();
      results = s1.addToExistingResultChain(results);
    }

    return results;
  }

  private State traverseReverse(State s0) {

    TPDepartureVertex fromVertex = new TPDepartureVertex(_context, _pathState);
    Vertex toVertex = null;
    EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

    return s0.edit(this, narrative).makeState();
  }

  /****
   * 
   ****/

  private int computeWaitTime(State s0, Pair<ArrivalAndDepartureInstance> pair) {
    int waitTime = (int) ((pair.getFirst().getBestDepartureTime() - s0.getTime()) / 1000);
    // If we have a lookahead departure, the wait time will actually be zero
    return Math.max(waitTime, 0);
  }
}
