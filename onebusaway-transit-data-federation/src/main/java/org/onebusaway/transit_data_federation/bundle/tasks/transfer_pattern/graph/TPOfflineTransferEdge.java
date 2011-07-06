package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateEditor;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class TPOfflineTransferEdge extends AbstractEdge {

  private final Vertex _fromVertex;

  private final StopTransfer _transfer;

  public TPOfflineTransferEdge(GraphContext context, Vertex fromVertex,
      StopTransfer transfer) {
    super(context);
    _fromVertex = fromVertex;
    _transfer = transfer;
  }

  @Override
  public State traverse(State s0) {
    TraverseOptions options = s0.getOptions();
    if (options.isArriveBy())
      throw new UnsupportedOperationException();

    /**
     * Check if we've reached our transfer limit
     */
    if (s0.getNumBoardings() > options.maxTransfers)
      return null;

    int transferTime = computeTransferTime(options);

    /**
     * We're using options.boardCost as a transfer penalty
     */
    double transferWeight = transferTime * options.walkReluctance
        + options.minTransferTime * options.waitReluctance;

    if (s0.getNumBoardings() > 0)
      transferWeight += options.boardCost;

    StopTimeService stopTimeService = _context.getStopTimeService();

    long time = s0.getTime() + (transferTime + options.minTransferTime) * 1000;

    List<StopTimeInstance> instances = stopTimeService.getNextBlockSequenceDeparturesForStop(
        _transfer.getStop(), time, false);

    State results = null;

    for (StopTimeInstance instance : instances) {
      State r = getDepartureAsTraverseResult(instance, s0, transferWeight);
      results = r.addToExistingResultChain(results);
    }

    return results;
  }

  /****
   * Private Methods
   ****/

  private int computeTransferTime(TraverseOptions options) {

    int transferTime = _transfer.getMinTransferTime();
    if (transferTime > 0)
      return transferTime;

    double walkingVelocity = options.speed;
    double distance = _transfer.getDistance();

    // time to walk = meters / (meters/sec) = sec
    int t = (int) (distance / walkingVelocity);

    // transfer time = time to walk + min transfer buffer time
    return t;
  }

  private State getDepartureAsTraverseResult(StopTimeInstance instance,
      State s0, double transferWeight) {

    long time = s0.getTime();

    long departureTime = instance.getDepartureTime();

    TPOfflineTransferVertex toVertex = new TPOfflineTransferVertex(_context,
        instance);
    EdgeNarrative narrative = narrative(s0, _fromVertex, toVertex);

    OBAStateEditor edit = (OBAStateEditor) s0.edit(this, narrative);
    edit.setTime(departureTime);

    int dwellTime = (int) ((departureTime - time) / 1000);
    double w = transferWeight
        + ItineraryWeightingLibrary.computeWeightForWait(s0, dwellTime);
    edit.incrementWeight(w);

    if (s0.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    edit.appendTripSequence(instance.getStopTime().getTrip());

    return edit.makeState();
  }
}
