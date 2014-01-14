/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.impl.otp.graph;

import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.SupportLibrary;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPArrivalVertex;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPQueryData;
import org.onebusaway.transit_data_federation.impl.otp.graph.tp.TPState;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferNode;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public class WaitingEndsAtStopEdge extends AbstractEdge {

  private final StopEntry _stop;

  private final boolean _isReverseEdge;

  public WaitingEndsAtStopEdge(GraphContext context, StopEntry stop,
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
    EdgeNarrative narrative = createNarrative(s0);
    return s0.edit(this, narrative).makeState();
  }

  private State traverseReverse(State s0) {

    TraverseOptions options = s0.getOptions();

    /**
     * Only allow transition to a transit stop if transit is enabled
     */
    if (!SupportLibrary.isTransitEnabled(options))
      return null;

    /**
     * If we've already boarded a transit vehicle, we only allow additional
     * boardings from a direct transfer. Note that we only apply this rule when
     * doing reverse traversal of the graph. In a forward traversal, this edge
     * traversal will be called in the optimization step where the number of
     * boardings is greater than zero. However, we still want the traversal to
     * proceed.
     */
    if (_isReverseEdge && s0.getNumBoardings() > 0)
      return null;

    TransferPatternService tpService = _context.getTransferPatternService();
    if (tpService.isEnabled())
      return traverseBackTransferPatterns(s0, options);

    EdgeNarrative narrative = createNarrative(s0);
    return s0.edit(this, narrative).makeState();
  }

  @Override
  public String toString() {
    return "WaitingEndsAtStopEdge(stop=" + _stop.getId() + ")";
  }

  private EdgeNarrative createNarrative(State s0) {

    ArrivalVertex fromVertex = new ArrivalVertex(_context, _stop, s0.getTime());
    WalkFromStopVertex toVertex = new WalkFromStopVertex(_context, _stop);
    return narrative(s0, fromVertex, toVertex);
  }

  private State traverseBackTransferPatterns(State s0, TraverseOptions options) {

    if (!_isReverseEdge) {
      Vertex fromVertex = null;
      Vertex toVertex = new WalkFromStopVertex(_context, _stop);
      EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);
      return s0.edit(this, narrative).makeState();
    }

    TransferPatternService tpService = _context.getTransferPatternService();

    TPQueryData queryData = options.getExtension(TPQueryData.class);

    List<StopEntry> sourceStops = queryData.getSourceStops();

    State results = null;

    Collection<TransferNode> trees = tpService.getReverseTransferPatternsForStops(
        queryData.getTransferPatternData(), sourceStops, _stop);

    for (TransferNode tree : trees) {

      TPState pathState = TPState.end(queryData, tree);

      Vertex fromVertex = new TPArrivalVertex(_context, pathState);
      Vertex toVertex = new WalkFromStopVertex(_context, _stop);

      EdgeNarrative narrative = narrative(s0, fromVertex, toVertex);
      State r = s0.edit(this, narrative).makeState();
      results = r.addToExistingResultChain(results);
    }

    return results;
  }
}
