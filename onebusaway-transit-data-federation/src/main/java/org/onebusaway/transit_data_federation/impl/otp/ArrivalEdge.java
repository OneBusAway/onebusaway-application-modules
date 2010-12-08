package org.onebusaway.transit_data_federation.impl.otp;

import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerPreferences;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class ArrivalEdge extends AbstractEdge {

  private final StopEntry _stop;

  public ArrivalEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public Vertex getFromVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vertex getToVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * We can leave this stop, walking somewhere else
     */
    TraverseResult result = new TraverseResult(0, s0, this);
    //result.setVertex(new WalkFromStopVertex(_context, _stop));

    /**
     * We can make a transit transfer at this stop
     */
    StopTransferService stopTransferService = _context.getStopTransferService();
    List<StopTransfer> transfers = stopTransferService.getTransfersForStop(_stop);

    for (StopTransfer transfer : transfers) {

      State s1 = s0.clone();
      s1.incrementTimeInSeconds(computeTransferTime(transfer));

      TraverseResult r = new TraverseResult(0, s0, this);
      /*
      r.setVertex(new WaitingAtStopVertex(_context, transfer.getStop(),
          s1.getTime()));
          */

      result = r.addToExistingResultChain(result);
    }

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult result = null;

    StopTimeService stopTimeService = _context.getStopTimeService();
    long time = s0.getTime();

    /**
     * Look for arrivals in the previous X minutes
     */
    Date timeFrom = new Date(SupportLibrary.getPreviousTimeWindow(_context,
        time));
    Date timeTo = new Date(time);

    List<StopTimeInstance> instances = stopTimeService.getStopTimeInstancesInRange(
        timeFrom, timeTo, _stop);

    for (StopTimeInstance instance : instances) {

      long arrivalTime = instance.getArrivalTime();

      // Prune anything that doesn't have an arrival time in the proper range,
      // since the stopTimeService method will also return instances that depart
      // in the target interval as well
      if (arrivalTime < timeFrom.getTime() || timeTo.getTime() <= arrivalTime)
        continue;

      int dwellTime = (int) ((time - arrivalTime) / 1000);
      State s1 = new State(arrivalTime);
      TraverseResult r = new TraverseResult(dwellTime, s1, this);
      //r.setVertex(new BlockDepartureVertex(_context, instance));

      result = r.addToExistingResultChain(result);
    }

    // In addition to all the departures, we can just remain waiting at the stop
    int dwellTime = (int) ((timeTo.getTime() - timeFrom.getTime()) / 1000);
    State s1 = new State(timeFrom.getTime());
    TraverseResult r = new TraverseResult(dwellTime, s1, this);
    //r.setVertex(new WaitingAtStopVertex(_context, _stop, timeFrom.getTime()));

    result = r.addToExistingResultChain(result);

    return result;
  }

  @Override
  public double getDistance() {
    return 0;
  }

  /****
   * Private Methods
   ****/

  private int computeTransferTime(StopTransfer transfer) {

    int transferTime = transfer.getMinTransferTime();
    if (transferTime > 0)
      return transferTime;

    TripPlannerPreferences preferences = _context.getPreferences();
    double walkingVelocity = preferences.getWalkingVelocity();
    double distance = transfer.getDistance();

    // time to walk = meters / (meters/sec) = sec
    int t = (int) (distance / walkingVelocity);

    // transfer time = time to walk + min transfer buffer time
    return t + preferences.getMinTransferBufferTime();
  }

}
