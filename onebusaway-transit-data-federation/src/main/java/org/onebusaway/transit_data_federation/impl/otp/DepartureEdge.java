package org.onebusaway.transit_data_federation.impl.otp;

import java.util.List;

import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class DepartureEdge extends AbstractEdge {

  private final StopEntry _stop;

  public DepartureEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public Vertex getFromVertex() {
    return new StopVertex(_context, _stop);
  }

  @Override
  public Vertex getToVertex() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult result = null;

    StopTimeService stopTimeService = _context.getStopTimeService();
    long time = s0.getTime();
    List<StopTimeInstance> instances = stopTimeService.getNextStopTimeDeparture(
        _stop, time);

    for (StopTimeInstance instance : instances) {

      int dwellTime = (int) ((instance.getDepartureTime() - time) / 1000);
      State s1 = new State(instance.getDepartureTime());
      TraverseResult r = new TraverseResult(dwellTime, s1);
      r.setVertex(new BlockDepartureVertex(_context, instance));

      if (result == null) {
        result = r;
      } else {
        result.setNextResult(r);
        result = r;
      }
    }

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    State s1 = s0.clone();
    return new TraverseResult(0, s1);
  }

  @Override
  public double getDistance() {
    return 0;
  }
}
