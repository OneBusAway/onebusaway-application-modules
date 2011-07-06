package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.List;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferParent;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPDepartureVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPQueryData;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPState;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseOptions;
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
  public State traverse(State s0) {
    TraverseOptions options = s0.getOptions();
    if (options.isArriveBy())
      return traverseReverse(s0);
    else
      return traverseForward(s0);
  }

  private State traverseForward(State s0) {

    TraverseOptions options = s0.getOptions();

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
    if (!_isReverseEdge && s0.getNumBoardings() > 0)
      return null;

    TransferPatternService tpService = _context.getTransferPatternService();
    if (tpService.isEnabled())
      return traverseTransferPatterns(s0, options);

    EdgeNarrative narrative = createNarrative(s0);
    return s0.edit(this, narrative).makeState();
  }

  private State traverseReverse(State s0) {

    TraverseOptions options = s0.getOptions();

    EdgeNarrative narrative = createNarrative(s0);
    StateEditor edit = s0.edit(this, narrative);
    edit.incrementTimeInSeconds(options.minTransferTime);
    double w = options.minTransferTime * options.waitAtBeginningFactor;
    edit.incrementWeight(w);
    return edit.makeState();
  }

  @Override
  public String toString() {
    return "WaitingBeginsAtStopEdge(stop=" + _stop.getId() + ")";
  }

  /****
   * Private Methods
   ****/

  private EdgeNarrative createNarrative(State s0) {

    WalkToStopVertex fromVertex = new WalkToStopVertex(_context, _stop);
    DepartureVertex toVertex = new DepartureVertex(_context, _stop,
        s0.getTime());

    return narrative(s0, fromVertex, toVertex);
  }

  private State traverseTransferPatterns(State s0, TraverseOptions options) {

    if (_isReverseEdge) {
      EdgeNarrative narrative = createNarrative(s0);
      return s0.edit(this, narrative).makeState();
    }

    TransferPatternService tpService = _context.getTransferPatternService();

    TPQueryData queryData = options.getExtension(TPQueryData.class);

    List<StopEntry> destStops = queryData.getDestStops();

    State results = null;

    TransferParent transfers = tpService.getTransferPatternsForStops(
        queryData.getTransferPatternData(), _stop, destStops);

    for (TransferNode tree : transfers.getTransfers()) {

      TPState pathState = TPState.start(queryData, tree);

      Vertex fromVertex = new WalkToStopVertex(_context, _stop);
      Vertex toVertex = new TPDepartureVertex(_context, pathState);
      EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);

      StateEditor edit = s0.edit(this, narrative);
      edit.incrementTimeInSeconds(options.minTransferTime);

      double w = options.minTransferTime * options.waitAtBeginningFactor;
      edit.incrementWeight(w);

      State r = edit.makeState();
      results = r.addToExistingResultChain(results);
    }

    return results;
  }
}
