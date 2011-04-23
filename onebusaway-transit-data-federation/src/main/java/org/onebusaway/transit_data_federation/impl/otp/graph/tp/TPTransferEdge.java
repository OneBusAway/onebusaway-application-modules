package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData.Editor;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TPTransferEdge extends AbstractEdge {

  private static Logger _log = LoggerFactory.getLogger(TPTransferEdge.class);

  private final TPState _fromState;

  private final TPState _toState;

  private final ArrivalAndDepartureInstance _departure;

  private final ArrivalAndDepartureInstance _arrival;

  private final boolean _reverse;

  public TPTransferEdge(GraphContext context, TPState fromState,
      TPState toState, ArrivalAndDepartureInstance departure,
      ArrivalAndDepartureInstance arrival, boolean reverse) {
    super(context);
    _fromState = fromState;
    _toState = toState;
    _departure = departure;
    _arrival = arrival;
    _reverse = reverse;
  }

  public StopEntry getFromStop() {
    Pair<StopEntry> stops = _fromState.getStops();
    return stops.getSecond();
  }

  public StopEntry getToStop() {
    Pair<StopEntry> stops = _toState.getStops();
    return stops.getFirst();
  }

  /****
   * {@link Edge} Interface
   ****/

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(options);

    Editor s1 = s0.edit();
    s1.incrementTimeInSeconds(transferTime + options.minTransferTime);
    double w = ItineraryWeightingLibrary.computeTransferWeight(transferTime,
        options);

    EdgeNarrative narrative = computeNarrative();

    return new TraverseResult(w, s1.createState(), narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    int transferTime = computeTransferTime(options);

    Editor s1 = s0.edit();
    s1.incrementTimeInSeconds(-(transferTime + options.minTransferTime));
    double w = ItineraryWeightingLibrary.computeTransferWeight(transferTime,
        options);

    EdgeNarrative narrative = computeNarrative();

    return new TraverseResult(w, s1.createState(), narrative);
  }

  /****
   * 
   ****/

  private EdgeNarrative computeNarrative() {

    if (_reverse) {

      Vertex fromV = new TPArrivalVertex(_context, _fromState);
      Vertex toV = new TPBlockDepartureVertex(_context, _toState, _departure,
          _arrival);
      return new EdgeNarrativeImpl(fromV, toV);

    } else {

      Vertex fromV = new TPBlockArrivalVertex(_context, _fromState, _departure,
          _arrival);
      Vertex toV = new TPDepartureVertex(_context, _toState);
      return new EdgeNarrativeImpl(fromV, toV);
    }
  }

  private int computeTransferTime(TraverseOptions options) {

    StopEntry from = getFromStop();
    StopEntry to = getToStop();

    if (from == to)
      return 0;

    StopTransfer transfer = _reverse ? findReverseTransfer(from, to)
        : findForwardTransfer(from, to);

    /**
     * No transfer found, even though we expected one
     */
    if (transfer == null) {
      _log.warn("expected transfer path between stops " + from.getId()
          + " and " + to.getId());
      return 0;
    }

    return ItineraryWeightingLibrary.computeTransferTime(transfer, options);
  }

  private StopTransfer findForwardTransfer(StopEntry fromStop, StopEntry toStop) {
    StopTransferService stService = _context.getStopTransferService();
    List<StopTransfer> transfers = stService.getTransfersFromStop(fromStop);
    for (StopTransfer transfer : transfers) {
      if (transfer.getStop() == toStop)
        return transfer;
    }
    return null;
  }

  private StopTransfer findReverseTransfer(StopEntry fromStop, StopEntry toStop) {
    StopTransferService stService = _context.getStopTransferService();
    List<StopTransfer> transfers = stService.getTransfersToStop(toStop);
    for (StopTransfer transfer : transfers) {
      if (transfer.getStop() == fromStop)
        return transfer;
    }
    return null;
  }
}
