package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.SPTVertex;

public interface VertexSkipStrategy {

  public boolean isVertexSkippedInFowardSearch(Vertex origin,
      State originState, SPTVertex vertex, TraverseOptions options);

  public boolean isVertexSkippedInReverseSearch(Vertex target,
      State targetState, SPTVertex vertex, TraverseOptions options);
}
