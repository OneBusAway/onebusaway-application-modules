package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;

public interface VertexSkipStrategy {

  public boolean isVertexSkippedInFowardSearch(Vertex origin,
      State originState, State state, TraverseOptions options);

  public boolean isVertexSkippedInReverseSearch(Vertex target,
      State targetState, State state, TraverseOptions options);
}
