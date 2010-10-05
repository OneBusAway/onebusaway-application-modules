package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

import org.onebusaway.transit_data_federation.impl.tripplanner.offline.ServiceIdActivation;

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

  /**
   * @return distance, in meters
   */
  public double getTotalBlockDistance();
}
