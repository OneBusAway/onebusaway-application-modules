package org.onebusaway.transit_data_federation.services.transit_graph;

public interface BlockStopTimeEntry {

  public StopTimeEntry getStopTime();
  
  public BlockTripEntry getTrip();

  public int getBlockSequence();

  /**
   * 
   * @return distance, in meters, from the start of the block
   */
  public double getDistanceAlongBlock();

  /**
   * The amount of accumulated slack time from the start of the block to the
   * arrival time at this stop. Slack time accumulates when there is scheduled
   * time between the arrival and departure of a vehicle at a stop that could
   * potentially be shortened if the vehicle is running late.
   * 
   * @return the accumulated slack time, in seconds
   */
  public int getAccumulatedSlackTime();
  
  public boolean hasPreviousStop();
  
  public boolean hasNextStop();
  
  public BlockStopTimeEntry getNextStop();
}
