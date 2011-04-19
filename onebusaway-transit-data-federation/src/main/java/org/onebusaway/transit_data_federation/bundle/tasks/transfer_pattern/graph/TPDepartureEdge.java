package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.ItineraryWeightingLibrary;
import org.onebusaway.transit_data_federation.impl.otp.OBAStateData.OBAEditor;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.StopTimeService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class TPDepartureEdge extends AbstractEdge {

  private final StopEntry _stop;

  public TPDepartureEdge(GraphContext context, StopEntry stop) {
    super(context);
    _stop = stop;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    StopTimeService stopTimeService = _context.getStopTimeService();

    List<StopTimeInstance> instances = stopTimeService.getNextScheduledBlockTripDeparturesForStop(
        _stop, s0.getTime());

    TraverseResult results = null;

    for (StopTimeInstance instance : instances) {
      TraverseResult r = getDepartureAsTraverseResult(instance, s0, options);
      results = r.addToExistingResultChain(results);
    }

    return results;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TPDepartureVertex fromVertex = new TPDepartureVertex(_context, _stop);
    Vertex toVertex = null;
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    return new TraverseResult(0, s0, narrative);
  }

  /****
   * Private Methods
   ****/

  private TraverseResult getDepartureAsTraverseResult(
      StopTimeInstance instance, State s0, TraverseOptions options) {

    long time = s0.getTime();
    StateData data = s0.getData();

    long departureTime = instance.getDepartureTime();

    TPDepartureVertex fromVertex = new TPDepartureVertex(_context, _stop);
    TPBlockDepartureVertex toVertex = new TPBlockDepartureVertex(_context,
        instance);
    EdgeNarrativeImpl narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

    OBAEditor edit = (OBAEditor) s0.edit();
    edit.setTime(departureTime);
    edit.incrementNumBoardings();
    edit.setEverBoarded(true);

    int dwellTime = (int) ((departureTime - time) / 1000);
    double w = ItineraryWeightingLibrary.computeWeightForWait(options,
        dwellTime, s0);

    if (data.getNumBoardings() == 0)
      edit.incrementInitialWaitTime(dwellTime * 1000);

    edit.appendTripSequence(instance.getStopTime().getTrip());

    return new TraverseResult(w, edit.createState(), narrative);
  }
}
