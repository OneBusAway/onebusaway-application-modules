package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.impl.otp.graph.WalkFromStopVertex;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkToStopState;
import org.opentripplanner.routing.spt.SPTVertex;

/**
 * In our transit-shed calculation, we effectively don't allow you to exit the
 * transit network once you've entered, avoiding the overhead of computing the
 * walk-shed from every transit stop you might arrive at. We care only that you
 * made it to the stop.
 * 
 * @author bdferris
 */
public class TransitShetVertexSkipStrategy implements VertexSkipStrategy {

  @Override
  public boolean isVertexSkippedInFowardSearch(SPTVertex vertex) {
    return vertex.mirror instanceof WalkFromStopVertex;
  }

  @Override
  public boolean isVertexSkippedInReverseSearch(SPTVertex vertex) {
    return vertex.mirror instanceof WalkToStopState;
  }
}
