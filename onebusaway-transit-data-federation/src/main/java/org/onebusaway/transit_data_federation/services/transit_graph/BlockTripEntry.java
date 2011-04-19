package org.onebusaway.transit_data_federation.services.transit_graph;

import java.util.List;

import org.onebusaway.transit_data_federation.services.blocks.AbstractBlockTripIndex;

public interface BlockTripEntry {
  
  public BlockConfigurationEntry getBlockConfiguration();

  public TripEntry getTrip();
  
  public short getSequence();
  
  public short getAccumulatedStopTimeIndex();

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
  
  /****
   * Stop Methods
   ****/
  
  public int getArrivalTimeForIndex(int index);
  
  public int getDepartureTimeForIndex(int index);

  public double getDistanceAlongBlockForIndex(int blockSequence);
  
  /****
   * Pattern Methods
   ****/
  
  public AbstractBlockTripIndex getPattern();
}
