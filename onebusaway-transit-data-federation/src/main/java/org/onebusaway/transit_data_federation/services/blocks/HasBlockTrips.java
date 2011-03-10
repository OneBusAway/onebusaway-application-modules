package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public interface HasBlockTrips {
  
  public List<BlockTripEntry> getTrips();

  public ServiceIdActivation getServiceIds();
}
