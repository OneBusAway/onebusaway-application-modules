package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.spt.SPTVertex;

public interface VertexSkipStrategy {

  public boolean isVertexSkippedInFowardSearch(SPTVertex vertex);

  public boolean isVertexSkippedInReverseSearch(SPTVertex vertex);
}
