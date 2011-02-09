package org.onebusaway.transit_data_federation.services.blocks;

import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public interface HasBlocks {
  public List<BlockConfigurationEntry> getBlocks();
  public ServiceIdActivation getServiceIds();
}
