package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData.OBAEditor;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class ArrivalReverseEdge extends AbstractEdge {

  private final StopEntry _stop;

  public ArrivalReverseEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
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
    Vertex fromVertex = null;
    Vertex toVertex = new ArrivalVertex(_context, _stop, s0.getTime());
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TraverseResult result = null;

    long time = s0.getTime();
    StateData data = s0.getData();

    /**
     * Look for arrivals in the previous X minutes
     */
    long timeFrom = SupportLibrary.getPreviousTimeWindow(_context, time);
    long timeTo = time;

    List<ArrivalAndDepartureInstance> arrivals = getArrivalsInTimeRange(time,
        timeFrom, timeTo, options);

    for (ArrivalAndDepartureInstance instance : arrivals) {

      long arrivalTime = instance.getBestArrivalTime();

      // Prune anything that doesn't have an arrival time in the proper range,
      // since the stopTimeService method will also return instances that depart
      // in the target interval as well
      if (arrivalTime < timeFrom || time <= arrivalTime)
        continue;

      int dwellTime = (int) ((time - arrivalTime) / 1000);
      OBAEditor edit = (OBAEditor) s0.edit();
      edit.setTime(arrivalTime);
      edit.incrementNumBoardings();
      edit.setEverBoarded(true);

      if (data.getNumBoardings() == 0)
        edit.incrementInitialWaitTime(dwellTime * 1000);

      double w = ItineraryWeightingLibrary.computeWeightForWait(options,
          dwellTime, s0);

      Vertex fromVertex = new BlockArrivalVertex(_context, instance);
      Vertex toVertex = new ArrivalVertex(_context, _stop, s0.getTime());
      EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

      TraverseResult r = new TraverseResult(w, edit.createState(), narrative);
      result = r.addToExistingResultChain(result);
    }

    // In addition to all the departures, we can just remain waiting at the stop
    int dwellTime = (int) ((time - timeFrom) / 1000);
    double w = ItineraryWeightingLibrary.computeWeightForWait(options,
        dwellTime, s0);

    OBAEditor edit = (OBAEditor) s0.edit();

    if (data.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    Vertex fromVertex = new ArrivalVertex(_context, _stop, timeFrom);
    Vertex toVertex = new ArrivalVertex(_context, _stop, s0.getTime());
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    TraverseResult r = new TraverseResult(w, edit.createState(), narrative);
    result = r.addToExistingResultChain(result);

    return result;
  }

  /****
   * Private Methods
   ****/

  private List<ArrivalAndDepartureInstance> getArrivalsInTimeRange(long time,
      long timeFrom, long timeTo, TraverseOptions options) {

    boolean useRealTime = false;
    OBATraverseOptions config = options.getExtension(OBATraverseOptions.class);
    if (config != null)
      useRealTime = config.useRealtime;

    ArrivalAndDepartureService service = _context.getArrivalAndDepartureService();

    if (useRealTime) {
      /**
       * TODO : If we want to simulate real-time trip planning with the system
       * in some past state, we'll need a way to adjust NOW here
       */
      TargetTime target = new TargetTime(time, System.currentTimeMillis());
      return service.getArrivalsAndDeparturesForStopInTimeRange(_stop, target,
          timeFrom, timeTo);
    } else {
      return service.getScheduledArrivalsAndDeparturesForStopInTimeRange(_stop,
          time, timeFrom, timeTo);
    }
  }
}
