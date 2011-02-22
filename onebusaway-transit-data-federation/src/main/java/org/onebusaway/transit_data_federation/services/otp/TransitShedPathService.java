package org.onebusaway.transit_data_federation.services.otp;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.spt.BasicShortestPathTree;

public interface TransitShedPathService {
  public BasicShortestPathTree getTransitShed(Vertex origin, State originState,
      TraverseOptions options);
}
