/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.realtime.api.EVehicleType;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Vehicle position information for a particular block.
 * 
 * @author bdferris
 */
public class BlockLocation {

  /**
   * The time for which the block location was generated
   */
  private long time;

  /****
   * These are fields that we can supply from schedule data
   ****/

  private BlockInstance blockInstance;

  private int blockStartTime;

  private BlockTripEntry activeTrip;

  private boolean inService;

  private double scheduledDistanceAlongBlock = Double.NaN;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private double distanceAlongBlock = Double.NaN;

  /**
   * Time, in seconds
   */
  private int effectiveScheduleTime;

  private CoordinatePoint location;

  private double orientation = Double.NaN;

  private BlockStopTimeEntry closestStop;

  private int closestStopTimeOffset;

  private BlockStopTimeEntry nextStop;

  private int nextStopTimeOffset;
  
  private BlockStopTimeEntry previousStop;

  private int previousStopTimeOffset;
  
  private EVehiclePhase phase;

  private EVehicleType vehicleType;

  private String status;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private boolean predicted;

  private long lastUpdateTime;

  private long lastLocationUpdateTime;

  private double lastKnownDistanceAlongBlock = Double.NaN;

  private CoordinatePoint lastKnownLocation;

  private double lastKnownOrientation = Double.NaN;

  private double scheduleDeviation = Double.NaN;

  private ScheduleDeviationSamples scheduleDeviations = null;

  private AgencyAndId vehicleId;
  
  private List<TimepointPredictionRecord> timepointPredictions;

  public BlockLocation() {

  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public void setBlockInstance(BlockInstance instance) {
    this.blockInstance = instance;
  }

  public int getBlockStartTime() {
    return blockStartTime;
  }

  public void setBlockStartTime(int blockStartTime) {
    this.blockStartTime = blockStartTime;
  }

  /**
   * @return the active trip for the block
   */
  public BlockTripEntry getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(BlockTripEntry activeTrip) {
    this.activeTrip = activeTrip;
  }

  /**
   * 
   * @return the active trip instance for the block location
   */
  public BlockTripInstance getActiveTripInstance() {
    if (activeTrip == null)
      return null;
    return new BlockTripInstance(activeTrip, blockInstance.getState());
  }

  /**
   * @return true if the block trip is actively in service
   */
  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }

  public boolean isScheduledDistanceAlongBlockSet() {
    return !Double.isNaN(scheduledDistanceAlongBlock);
  }

  /**
   * If the trip is not in service (see {@link #isInService()}), this value will
   * be {@link Double#NaN}.
   * 
   * @return the scheduled distance traveled along the shape of the block, in
   *         meters
   */
  public double getScheduledDistanceAlongBlock() {
    return scheduledDistanceAlongBlock;
  }

  public void setScheduledDistanceAlongBlock(double scheduledDistanceAlongBlock) {
    this.scheduledDistanceAlongBlock = scheduledDistanceAlongBlock;
  }

  /**
   * The effective schedule time measures the progress of the transit vehicle in
   * serving the underlying schedule.
   * 
   * effectiveScheduleTime = currentTime - scheduleDeviation
   * 
   * @return time, in seconds
   */
  public int getEffectiveScheduleTime() {
    return effectiveScheduleTime;
  }

  public void setEffectiveScheduleTime(int effectiveScheduleTime) {
    this.effectiveScheduleTime = effectiveScheduleTime;
  }

  /**
   * If the trip is non in service (see {@link #isInService()}, this value with
   * be false.
   * 
   * @return
   */
  public boolean isDistanceAlongBlockSet() {
    return !Double.isNaN(distanceAlongBlock);
  }

  /**
   * If the trip is not in service (see {@link #isInService()}), this value will
   * be {@link Double#NaN}. See {@link #isDistanceAlongBlockSet()}.
   * 
   * @return the distance traveled along the shape of the block, in meters
   */
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  /**
   * @return the block position
   */
  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public boolean isOrientationSet() {
    return !Double.isNaN(orientation);
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

  /**
   * The closest stop to the current position of the transit vehicle among the
   * stop times of the current trip.
   * 
   * @return the closest stop time entry
   */
  public BlockStopTimeEntry getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(BlockStopTimeEntry closestStop) {
    this.closestStop = closestStop;
  }

  /**
   * The time offset, in seconds, from the closest stop to the current position
   * of the transit vehicle among the stop times of the current trip. If the
   * number is positive, the stop is coming up. If negative, the stop has
   * already been passed.
   * 
   * @return time, in seconds
   */
  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  /**
   * See description in {@link #getClosestStopTimeOffset()}.
   * 
   * @param closestStopTimeOffset the time offset from the closest stop, in
   *          seconds
   */
  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }

  /**
   * The next upcoming stop to the current position of the transit vehicle among
   * the stop times of the current trip.
   * 
   * @return the next stop time entry
   */
  public BlockStopTimeEntry getNextStop() {
    return nextStop;
  }

  public void setNextStop(BlockStopTimeEntry nextStop) {
    this.nextStop = nextStop;
  }

  /**
   * The time offset, in seconds, from the next stop to the current position of
   * the transit vehicle.
   * 
   * @return time, in seconds
   */
  public int getNextStopTimeOffset() {
    return nextStopTimeOffset;
  }

  /**
   * See {@link #getNextStopTimeOffset()}
   * 
   * @param nextStopTimeOffset
   */
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

  public EVehiclePhase getPhase() {
    return phase;
  }

  public void setPhase(EVehiclePhase phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public EVehicleType getVehicleType() { return vehicleType; }

  public void setVehicleType(EVehicleType vehicleType) { this.vehicleType = vehicleType; }

  /**
   * If real-time data is available in any form (schedule deviation,
   * distanceAlongBlock, last known location) for this vehicle
   * 
   * @return true if real-time is available
   */
  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  /**
   * @return the time we last heard from the bus (Unix-time)
   */
  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  /**
   * @return the time we last heard a location update from the bus (Unix-time)
   */
  public long getLastLocationUpdateTime() {
    return lastLocationUpdateTime;
  }

  public void setLastLocationUpdateTime(long lastLocationUpdateTime) {
    this.lastLocationUpdateTime = lastLocationUpdateTime;
  }

  public boolean isLastKnownDistanceAlongBlockSet() {
    return !Double.isNaN(lastKnownDistanceAlongBlock);
  }

  public double getLastKnownDistanceAlongBlock() {
    return lastKnownDistanceAlongBlock;
  }

  public void setLastKnownDistanceAlongBlock(double lastKnownDistanceAlongBlock) {
    this.lastKnownDistanceAlongBlock = lastKnownDistanceAlongBlock;
  }

  public CoordinatePoint getLastKnownLocation() {
    return lastKnownLocation;
  }

  public void setLastKnownLocation(CoordinatePoint lastKnownLocation) {
    this.lastKnownLocation = lastKnownLocation;
  }

  public boolean isLastKnownOrientationSet() {
    return !Double.isNaN(lastKnownOrientation);
  }

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  public double getLastKnownOrientation() {
    return lastKnownOrientation;
  }

  public void setLastKnownOrientation(double lastKnownOrientation) {
    this.lastKnownOrientation = lastKnownOrientation;
  }

  /**
   * @return true if we have schedule deviation data
   */
  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  /**
   * If no schedule deviation data is available, this value with be
   * {@link Double#NaN}.
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation schedule deviation, in seconds, (+deviation is
   *          late, -deviation is early)
   */
  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean areScheduleDeviationsSet() {
    return scheduleDeviations != null && !scheduleDeviations.isEmpty();
  }

  public ScheduleDeviationSamples getScheduleDeviations() {
    return scheduleDeviations;
  }

  public void setScheduleDeviations(ScheduleDeviationSamples scheduleDeviations) {
    this.scheduleDeviations = scheduleDeviations;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  public List<TimepointPredictionRecord> getTimepointPredictions() {
      return this.timepointPredictions;
  }
  
  public void setTimepointPredictions(List<TimepointPredictionRecord> timepointPredictions) {
      this.timepointPredictions = timepointPredictions;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("BlockLocation(");
    if (blockInstance != null && blockInstance.getBlock() != null)
      b.append("block=").append(blockInstance.getBlock().getBlock().getId()).append(
          ",");
    if (phase != null)
      b.append("phase=").append(phase).append(",");
    if (status != null)
      b.append("status=").append(status).append(",");
    if (isScheduleDeviationSet())
      b.append("scheduleDeviation=").append(scheduleDeviation).append(",");
    if (predicted)
      b.append("predicted=true,");
    if (isDistanceAlongBlockSet())
      b.append("distanceAlongBlock=").append(distanceAlongBlock).append(",");
    if (vehicleId != null)
      b.append("vehicleId=").append(vehicleId).append(",");
    b.append(")");
    return b.toString();
  }
}
