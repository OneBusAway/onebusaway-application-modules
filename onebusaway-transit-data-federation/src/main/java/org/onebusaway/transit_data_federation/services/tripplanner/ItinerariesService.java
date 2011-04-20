package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.Date;
import java.util.List;

import org.onebusaway.transit_data.model.tripplanning.TransitLocationBean;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.spt.GraphPath;

public interface ItinerariesService {

  public List<GraphPath> getItinerariesBetween(TransitLocationBean from,
      TransitLocationBean to, TargetTime targetTime, OBATraverseOptions options);

  public GraphPath getWalkingItineraryBetweenStops(StopEntry fromStop,
      StopEntry toStop, Date time, TraverseOptions options);
}
