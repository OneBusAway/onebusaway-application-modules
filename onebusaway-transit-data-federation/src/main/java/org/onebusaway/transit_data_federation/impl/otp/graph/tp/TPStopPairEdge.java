package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.Date;
import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.ItinerariesService;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData.Editor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TPStopPairEdge extends AbstractEdge {

  private static Logger _log = LoggerFactory.getLogger(TPStopPairEdge.class);

  private TPState _pathState;

  public TPStopPairEdge(GraphContext context, TPState pathState) {
    super(context);
    _pathState = pathState;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    ArrivalAndDepartureService adService = _context.getArrivalAndDepartureService();
    ItinerariesService itinService = _context.getItinerariesService();

    Pair<StopEntry> stopPair = _pathState.getCurrentStopPair();

    long offset = 20 * 60 * 1000;
    TargetTime targetTime = new TargetTime();

    long maxScheduledDeparture = s0.getTime() + offset;

    List<Pair<ArrivalAndDepartureInstance>> instances = adService.getNextDeparturesAndArrivalsForStopPair(
        stopPair.getFirst(), stopPair.getSecond(), targetTime, s0.getTime(),
        maxScheduledDeparture);

    TraverseResult results = null;

    for (Pair<ArrivalAndDepartureInstance> pair : instances) {

      ArrivalAndDepartureInstance departure = pair.getFirst();
      maxScheduledDeparture = Math.max(maxScheduledDeparture,
          departure.getScheduledDepartureTime());

      int dwellTime = computeWaitTime(s0, pair);
      int transitTime = computeTransitTime(pair);

      double w = ItineraryWeightingLibrary.computeWeightForWait(options,
          dwellTime, s0) + transitTime;

      Editor s1 = s0.edit();
      s1.incrementTimeInSeconds(dwellTime + transitTime);
      s1.incrementNumBoardings();
      s1.setEverBoarded(true);

      /**
       * If we have a next stop pair, we need to add a walking transfer leg
       */
      if (_pathState.hasNextStopPair()) {

        Pair<StopEntry> nextPath = _pathState.getNextStopPair();
        StopEntry from = stopPair.getSecond();
        StopEntry to = nextPath.getFirst();

        /**
         * But only if we're not transferring at the same stop
         */
        if (from != to) {

          GraphPath path = itinService.getWalkingItineraryBetweenStops(from,
              to, new Date(s1.getTime()), options);

          /**
           * No path found, even though we expected one
           */
          if (path == null) {
            _log.warn("expected walking path between stops " + from.getId()
                + " and " + to.getId());
            continue;
          }

          SPTVertex sptFrom = path.getFirstVertex();
          SPTVertex sptTo = path.getLastVertex();

          w += (sptTo.weightSum - sptFrom.weightSum);
          s1.incrementWithStateDelta(sptFrom.state, sptTo.state);
        }
      }

      TPState pathState = _pathState.extend(pair);

      Vertex fromV = new TPPathVertex(_context, _pathState);
      Vertex toV = new TPPathVertex(_context, pathState);

      EdgeNarrative narrative = new EdgeNarrativeImpl(fromV, toV);

      TraverseResult r = new TraverseResult(w, s1.createState(), narrative);
      results = r.addToExistingResultChain(results);
    }

    if (!instances.isEmpty()) {

      int dwellTime = (int) ((maxScheduledDeparture - s0.getTime()) / 1000);
      Editor s1 = s0.edit();
      s1.incrementTimeInSeconds(dwellTime);

      double w = ItineraryWeightingLibrary.computeWeightForWait(options,
          dwellTime, s0);

      Vertex fromV = new TPPathVertex(_context, _pathState);
      Vertex toV = new TPPathVertex(_context, _pathState);

      EdgeNarrative narrative = new EdgeNarrativeImpl(fromV, toV);

      TraverseResult r = new TraverseResult(w, s1.createState(), narrative);
      results = r.addToExistingResultChain(results);
    }

    return results;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    throw new UnsupportedOperationException();
  }

  private int computeWaitTime(State s0, Pair<ArrivalAndDepartureInstance> pair) {
    return (int) ((pair.getFirst().getBestDepartureTime() - s0.getTime()) / 1000);
  }

  private int computeTransitTime(Pair<ArrivalAndDepartureInstance> pair) {
    ArrivalAndDepartureInstance from = pair.getFirst();
    ArrivalAndDepartureInstance to = pair.getSecond();
    return (int) ((to.getBestArrivalTime() - from.getBestDepartureTime()) / 1000);
  }
}
