/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public class ScheduledBlockLocation {

  private BlockTripEntry activeTrip;

  private int scheduledTime;

  private CoordinatePoint location;

  private double orientation = Double.NaN;

  private double distanceAlongBlock = Double.NaN;

  private BlockStopTimeEntry closestStop;

  private int closestStopTimeOffset;

  private BlockStopTimeEntry nextStop;

  private int nextStopTimeOffset;
  
  private BlockStopTimeEntry previousStop;
  
  private int previousStopTimeOffset;

  private boolean inService;

  private int stopTimeIndex;

  /**
   * Should never be null, even if the trip is not in service. In the case of
   * out of service before the start of the block, the active trip will be the
   * first trip of the block. For out of service at the end of the block, it
   * will be the last trip of the block.
   * 
   * @return the currently active trip
   */
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

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  public double getOrientation() {
    return orientation;
  }

  public void setOrientation(double orientation) {
    this.orientation = orientation;
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

  public BlockStopTimeEntry getPreviousStop() {
	return previousStop;
}

public void setPreviousStop(BlockStopTimeEntry previousStop) {
	this.previousStop = previousStop;
}

public int getPreviousStopTimeOffset() {
	return previousStopTimeOffset;
}

public void setPreviousStopTimeOffset(int previousStopTimeOffset) {
	this.previousStopTimeOffset = previousStopTimeOffset;
}

/**
   * A scheduled block location can exist but not be considered "in service" in
   * the following cases:
   * 
   * 1) The scheduled block location is requested for a scheduled time that is
   * before the official start time of the block. If the scheduled time is after
   * the end time of the block, then the scheduled block location should not
   * exist.
   * 
   * 2) The scheduled block location is requested for a distance along block
   * that is beyond the last stop in the block, but not yet to the end of the
   * block's shape.
   * 
   * @return true if the vehicle is considered "in service" at this scheduled
   *         location
   */
  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }

  /**
   * @return the index into the block config's list of stop times that was used
   *         to find the scheduled block location
   */
  public int getStopTimeIndex() {
    return stopTimeIndex;
  }

  public void setStopTimeIndex(int stopTimeIndex) {
    this.stopTimeIndex = stopTimeIndex;
  }
  
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("ScheduledBlockLocation(");
    b.append("activeTrip=");
    if (activeTrip != null)
      b.append(activeTrip.getTrip().getId());
    else
      b.append("null");
    int mins = scheduledTime / 60;
    b.append(" scheduledTime=").append(mins / 60).append(':').append(mins % 60);
    b.append(" distanceAlongBlock=").append(distanceAlongBlock);
    b.append(")");
    return b.toString();
  }

}