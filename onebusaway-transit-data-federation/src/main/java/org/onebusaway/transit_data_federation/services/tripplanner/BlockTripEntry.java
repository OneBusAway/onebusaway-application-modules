package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

public interface BlockTripEntry {
  
  public BlockConfigurationEntry getBlockConfiguration();

  public TripEntry getTrip();
  
  public int getAccumulatedStopTimeIndex();

  /**
   * The amount of accumulated slack time from the start of the block to the
   * start of the trip
   * 
   * @return accumulated slack time, in seconds
   */
  public int getAccumulatedSlackTime();

  /**
   * @return distance, in meters, of the start of the trip from the start of the
   *         block
   */
  public double getDistanceAlongBlock();
  
  public List<BlockStopTimeEntry> getStopTimes();

  public BlockTripEntry getPreviousTrip();

  public BlockTripEntry getNextTrip();
}
