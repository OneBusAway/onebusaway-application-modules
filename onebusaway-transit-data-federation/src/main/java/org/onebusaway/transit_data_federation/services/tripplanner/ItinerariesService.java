package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.Date;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.spt.GraphPath;

public interface ItinerariesService {

  public GraphPath getWalkingItineraryBetweenStops(StopEntry fromStop,
      StopEntry toStop, Date time, TraverseOptions options);
}
