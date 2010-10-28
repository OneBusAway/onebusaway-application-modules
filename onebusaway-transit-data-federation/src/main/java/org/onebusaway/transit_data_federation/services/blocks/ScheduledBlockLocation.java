package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class ScheduledBlockLocation {

  private BlockTripEntry activeTrip;

  private int scheduledTime;

  private CoordinatePoint location;

  private double distanceAlongBlock;

  private BlockStopTimeEntry closestStop;

  private int closestStopTimeOffset;

  private BlockStopTimeEntry nextStop;

  private int nextStopTimeOffset;

  public BlockTripEntry getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(BlockTripEntry activeTrip) {
    this.activeTrip = activeTrip;
  }

  /**
   * @return the scheduled time of the current trip in seconds
   */
  public int getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(int scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public BlockStopTimeEntry getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(BlockStopTimeEntry closestStop) {
    this.closestStop = closestStop;
  }

  /**
   * The time offset, in seconds, from the closest stop to the current position
   * of the transit vehicle among the stop times of the current block. If the
   * number is positive, the stop is coming up. If negative, the stop has
   * already been passed.
   * 
   * @return time, in seconds
   */
  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }

  public BlockStopTimeEntry getNextStop() {
    return nextStop;
  }

  public void setNextStop(BlockStopTimeEntry nextStop) {
    this.nextStop = nextStop;
  }

  public int getNextStopTimeOffset() {
    return nextStopTimeOffset;
  }

  public void setNextStopTimeOffset(int nextStopTimeOffset) {
    this.nextStopTimeOffset = nextStopTimeOffset;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("ScheduledBlockLocation(");
    b.append("activeTripe=");
    if (activeTrip != null)
      b.append(activeTrip.getTrip().getId());
    else
      b.append("null");
    b.append(" scheduledTime=").append(scheduledTime);
    b.append(" distanceAlongBlock=").append(distanceAlongBlock);
    b.append(")");
    return b.toString();
  }
}