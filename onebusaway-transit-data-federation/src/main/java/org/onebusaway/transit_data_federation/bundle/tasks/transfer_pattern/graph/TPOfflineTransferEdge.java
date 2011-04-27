package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData.OBAEditor;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
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
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Check if we've reached our transfer limit
     */
    StateData data = s0.getData();
    if (data.getNumBoardings() > options.maxTransfers)
      return null;

    int transferTime = computeTransferTime(options);

    State s1 = s0.incrementTimeInSeconds(transferTime + options.minTransferTime);

    /**
     * We're using options.boardCost as a transfer penalty
     */
    double transferWeight = transferTime * options.walkReluctance
        + options.minTransferTime * options.waitReluctance;

    if (data.getNumBoardings() > 0)
      transferWeight += options.boardCost;

    StopTimeService stopTimeService = _context.getStopTimeService();

    List<StopTimeInstance> instances = stopTimeService.getNextBlockSequenceDeparturesForStop(
        _transfer.getStop(), s1.getTime());

    TraverseResult results = null;

    for (StopTimeInstance instance : instances) {
      TraverseResult r = getDepartureAsTraverseResult(instance, s1, options,
          transferWeight);
      results = r.addToExistingResultChain(results);
    }

    return results;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    throw new UnsupportedOperationException();
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

  private TraverseResult getDepartureAsTraverseResult(
      StopTimeInstance instance, State s0, TraverseOptions options,
      double transferWeight) {

    long time = s0.getTime();
    StateData data = s0.getData();

    long departureTime = instance.getDepartureTime();

    TPOfflineTransferVertex toVertex = new TPOfflineTransferVertex(_context,
        instance);
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(_fromVertex, toVertex);

    OBAEditor edit = (OBAEditor) s0.edit();
    edit.setTime(departureTime);

    int dwellTime = (int) ((departureTime - time) / 1000);
    double w = transferWeight
        + ItineraryWeightingLibrary.computeWeightForWait(options, dwellTime, s0);

    if (data.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    edit.appendTripSequence(instance.getStopTime().getTrip());

    return new TraverseResult(w, edit.createState(), narrative);
  }
}
