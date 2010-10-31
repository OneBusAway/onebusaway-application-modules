package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;


/**
 * A block configuration i
 * 
 * @author bdferris
 *
 */
public interface BlockConfigurationEntry {
  
  public BlockEntry getBlock();
  
  public ServiceIdActivation getServiceIds();

  public List<BlockTripEntry> getTrips();

  public List<BlockStopTimeEntry> getStopTimes();
  
  public List<FrequencyEntry> getFrequencies();

  /**
   * @return distance, in meters
   */
  public double getTotalBlockDistance();
}
