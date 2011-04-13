package org.onebusaway.transit_data_federation.impl.otp.graph;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface HasStopTransitVertex extends TransitVertex {
  public StopEntry getStop();
}
