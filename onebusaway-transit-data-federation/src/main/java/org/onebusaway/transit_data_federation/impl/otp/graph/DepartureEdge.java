package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OTPConfiguration;
import org.onebusaway.transit_data_federation.impl.otp.OTPState;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.StateData.Editor;
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
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult result = null;

    long time = s0.getTime();
    StateData data = s0.getData();

    /**
     * Look for departures in the next X minutes
     */
    long fromTime = time;
    long toTime = SupportLibrary.getNextTimeWindow(_context, time);

    List<ArrivalAndDepartureInstance> departures = getDeparturesInTimeRange(s0,
        fromTime, toTime, options);

    for (ArrivalAndDepartureInstance instance : departures) {

      long departureTime = instance.getBestDepartureTime();

      /**
       * Prune anything that doesn't have a departure in the proper range, since
       * the arrivals and departures method will also return instances that
       * arrive in the target interval as well
       */
      if (departureTime < time || toTime <= departureTime)
        continue;

      // If this is the last stop time in the block, don't continue
      if (!SupportLibrary.hasNextStopTime(instance))
        continue;

      DepartureVertex fromVertex = new DepartureVertex(_context, _stop, time);
      BlockDepartureVertex toVertex = new BlockDepartureVertex(_context,
          instance);
      EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

      Editor edit = s0.edit();
      edit.setTime(departureTime);
      edit.incrementNumBoardings();
      edit.setEverBoarded(true);

      int dwellTime = (int) ((departureTime - time) / 1000);
      double w = ItineraryWeightingLibrary.computeWeightForWait(options, dwellTime, s0);
      
      if (data.getNumBoardings() == 0)
        OTPState.incrementInitialWaitTime(edit, dwellTime * 1000);
      
      OTPState.appendTripSequence(edit, instance.getBlockTrip());

      TraverseResult r = new TraverseResult(w, edit.createState(), narrative);
      result = r.addToExistingResultChain(result);
    }

    // In addition to all the departures, we can just remain waiting at the stop

    Editor edit = s0.edit();
    edit.setTime(toTime);
    
    DepartureVertex fromVertex = new DepartureVertex(_context, _stop,
        s0.getTime());
    DepartureVertex toVertex = new DepartureVertex(_context, _stop, toTime);
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    int dwellTime = (int) ((toTime - time) / 1000);
    double w = ItineraryWeightingLibrary.computeWeightForWait(options, dwellTime, s0);
    
    if (data.getNumBoardings() == 0)
      OTPState.incrementInitialWaitTime(edit, dwellTime * 1000);
    
    TraverseResult r = new TraverseResult(w, edit.createState(), narrative);
    result = r.addToExistingResultChain(result);

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    DepartureVertex fromVertex = new DepartureVertex(_context, _stop,
        s0.getTime());
    Vertex toVertex = null;
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(0, s0, narrative);
  }

  /****
   * Private Methods
   ****/

  private List<ArrivalAndDepartureInstance> getDeparturesInTimeRange(State s0,
      long fromTime, long toTime, TraverseOptions options) {

    boolean useRealtime = false;
    OTPConfiguration config = options.getExtension(OTPConfiguration.class);
    if (config != null)
      useRealtime = config.useRealtime;

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();

    if (useRealtime) {
      /**
       * TODO : If we want to simulate real-time trip planning with the system
       * in some past state, we'll need a way to adjust NOW here
       */
      TargetTime time = new TargetTime(System.currentTimeMillis(), s0.getTime());
      return service.getArrivalsAndDeparturesForStopInTimeRange(_stop, time,
          fromTime, toTime);
    } else {
      return service.getScheduledArrivalsAndDeparturesForStopInTimeRange(_stop,
          s0.getTime(), fromTime, toTime);
    }
  }
}
