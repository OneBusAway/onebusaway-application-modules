package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.HasIndexedBlockStopTimes;

/**
 * A block configuration i
 * 
 * @author bdferris
 * 
 */
public interface BlockConfigurationEntry extends HasIndexedBlockStopTimes {

  public BlockEntry getBlock();

  public ServiceIdActivation getServiceIds();

  public List<BlockTripEntry> getTrips();

  public List<FrequencyEntry> getFrequencies();

  /**
   * @return distance, in meters
   */
  public double getTotalBlockDistance();

  public double getDistanceAlongBlockForIndex(int blockSequence);
}
