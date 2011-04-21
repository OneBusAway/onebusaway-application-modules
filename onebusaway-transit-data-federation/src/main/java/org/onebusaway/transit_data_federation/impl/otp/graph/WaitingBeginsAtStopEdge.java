package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.List;
import java.util.Set;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPPathVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPQueryData;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPState;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateData;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class WaitingBeginsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  private final boolean _isReverseEdge;

  public WaitingBeginsAtStopEdge(GraphContext context, StopEntry stop,
      boolean isReverseEdge) {
    super(context);
    _stop = stop;
    _isReverseEdge = isReverseEdge;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    /**
     * Only allow transition to a transit stop if transit is enabled
     */
    if (!SupportLibrary.isTransitEnabled(options))
      return null;

    /**
     * If we've already boarded a transit vehicle, we only allow additional
     * boardings from a direct transfer. Note that we only apply this rule when
     * doing forward traversal of the graph. In a backwards traversal, this edge
     * traversal will be called in the optimization step where the number of
     * boardings is greater than zero. However, we still want the traversal to
     * proceed.
     * 
     */
    StateData data = s0.getData();
    if (!_isReverseEdge && data.getNumBoardings() > 0)
      return null;

    TransferPatternService tpService = _context.getTransferPatternService();
    if (tpService.isEnabled())
      return traverseTransferPatterns(s0, options);

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());

    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    EdgeNarrativeImpl narrative = createNarrative(s0.getTime());
    return new TraverseResult(0, s0, narrative);
  }

  @Override
  public String toString() {
    return "WaitingBeginsAtStopEdge(stop=" + _stop.getId() + ")";
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrativeImpl createNarrative(long time) {

    WalkToStopVertex fromVertex = new WalkToStopVertex(_context, _stop);
    DepartureVertex toVertex = new DepartureVertex(_context, _stop, time);

    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }

  private TraverseResult traverseTransferPatterns(State s0,
      TraverseOptions options) {

    TransferPatternService tpService = _context.getTransferPatternService();

    TPQueryData queryData = options.getExtension(TPQueryData.class);

    Set<StopEntry> destStops = queryData.getDestStops2();

    TraverseResult results = null;

    for (StopEntry toStop : destStops) {

      List<List<Pair<StopEntry>>> paths = tpService.getTransferPatternForStops(
          _stop, toStop);

      for (List<Pair<StopEntry>> path : paths) {

        TPState pathState = TPState.start(queryData, path);

        Vertex fromVertex = new WalkToStopVertex(_context, _stop);
        Vertex toVertex = new TPPathVertex(_context, pathState);
        EdgeNarrative narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

        TraverseResult r = new TraverseResult(0, s0, narrative);
        results = r.addToExistingResultChain(results);
      }
    }
    return results;
  }
}
