package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.graph;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.graph.AbstractEdge;
import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHop;
import org.onebusaway.transit_data_federation.services.tripplanner.StopHopService;
import org.opentripplanner.routing.algorithm.NegativeWeightException;
import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;

public class HubEdge extends AbstractEdge {

  private final StopEntry _stop;
  private final boolean _isTransfer;

  public HubEdge(GraphContext context, StopEntry stop, boolean isTransfer) {
    super(context);
    _stop = stop;
    _isTransfer = isTransfer;
  }

  @Override
  public TraverseResult traverse(State s0, TraverseOptions options)
      throws NegativeWeightException {

    StopHopService stopHopService = _context.getStopHopService();
    TransitGraphDao transitGraphDao = _context.getTransitGraphDao();

    List<StopHop> hops = stopHopService.getHopsFromStop(_stop);

    TraverseResult result = null;

    for (StopHop hop : hops) {

      int travelTime = hop.getMinTravelTime();
      State s1 = s0.incrementTimeInSeconds(travelTime);

      EdgeNarrative narrative = getEdgeNarrative(_stop, hop.getStop(), false);

      TraverseResult tr = new TraverseResult(travelTime, s1, narrative);
      result = tr.addToExistingResultChain(result);
    }

    /**
     * We can only transfer if we're not already coming from a transfer
     */
    if (!_isTransfer) {
      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
          _stop.getStopLat(), _stop.getStopLon(), 1000);
      List<StopEntry> stops = transitGraphDao.getStopsByLocation(bounds);

      for (StopEntry stop : stops) {

        double d = SphericalGeometryLibrary.distance(_stop.getStopLat(),
            _stop.getStopLon(), stop.getStopLat(), stop.getStopLon());
        int travelTime = (int) (d / options.speed);
        State s1 = s0.incrementTimeInSeconds(travelTime);

        EdgeNarrative narrative = getEdgeNarrative(_stop, stop, true);

        TraverseResult tr = new TraverseResult(travelTime, s1, narrative);
        result = tr.addToExistingResultChain(result);
      }
    }

    return result;
  }

  @Override
  public TraverseResult traverseBack(State s0, TraverseOptions options)
      throws NegativeWeightException {

    throw new UnsupportedOperationException();
  }

  private EdgeNarrative getEdgeNarrative(StopEntry from, StopEntry to,
      boolean isTransfer) {
    Vertex fromVertex = new HubVertex(_context, from, _isTransfer);
    Vertex toVertex = new HubVertex(_context, to, isTransfer);
    return new EdgeNarrativeImpl(fromVertex, toVertex);
  }
}
