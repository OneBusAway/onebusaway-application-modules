package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.SPTVertex;

public class TPWalkFromSourceToStopEdge extends AbstractEdge {

  private final TPQueryData _queryData;
  private final StopEntry _sourceStop;
  private final GraphPath _sourcePath;

  public TPWalkFromSourceToStopEdge(GraphContext context,
      TPQueryData queryData, StopEntry sourceStop, GraphPath sourcePath) {
    super(context);
    _queryData = queryData;
    _sourceStop = sourceStop;
    _sourcePath = sourcePath;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    TransferPatternService tpService = _context.getTransferPatternService();

    TraverseResult results = null;

    Vector<SPTVertex> sptVertices = _sourcePath.vertices;
    SPTVertex walkSptVertex = sptVertices.get(sptVertices.size() - 1);

    Map<StopEntry, GraphPath> destStops = _queryData.getDestStops();

    for (Map.Entry<StopEntry, GraphPath> entry : destStops.entrySet()) {

      StopEntry destStop = entry.getKey();
      GraphPath walkToDest = entry.getValue();

      List<List<Pair<StopEntry>>> paths = tpService.getTransferPatternForStops(
          _sourceStop, destStop);

      for (List<Pair<StopEntry>> path : paths) {

        TPState pathState = TPState.start(_queryData, _sourcePath, path,
            walkToDest);
        TPSourceVertex fromVertex = new TPSourceVertex(_context, _queryData);
        TPPathVertex toVertex = new TPPathVertex(_context, pathState);
        EdgeNarrative narrative = new EdgeNarrativeImpl(fromVertex, toVertex);

        TraverseResult r = new TraverseResult(walkSptVertex.weightSum,
            walkSptVertex.state, narrative);
        results = r.addToExistingResultChain(results);
      }
    }

    return results;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {
    throw new UnsupportedOperationException();
  }
}
