package org.onebusaway.transit_data_federation.services;

import org.opentripplanner.routing.core.Graph;

public interface OpenTripPlannerService {
  public Graph getGraph();
}
