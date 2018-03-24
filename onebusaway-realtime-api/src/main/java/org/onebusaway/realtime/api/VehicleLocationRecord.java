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
package org.onebusaway.realtime.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Vehicle location records are the key data structure for passing real-time
 * location data about a transit vehicle from an external data source into
 * OneBusAway. It tries to capture a variety of fields that might be present in
 * an AVL stream:
 * 
 * <ul>
 * <li>block id</li>
 * <li>trip id</li>
 * <li>vehicle id</li>
 * <li>location</li>
 * <li>schedule adherence</li>
 * <li>arrival data relative to a timepoint</li>
 * </ul>
 * 
 * Not all of these fields will necessarily be set by the AVL source, so we may
 * have to be flexible in how we process the data.
 * 
 * @author bdferris
 * @see VehicleLocationListener
 */
public class VehicleLocationRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private AgencyAndId blockId;

  private int blockStartTime;

  private AgencyAndId tripId;

  private AgencyAndId vehicleId;

  private long timeOfRecord;

  private long timeOfLocationUpdate;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
  private double scheduleDeviation = Double.NaN;

  private double distanceAlongBlock = Double.NaN;

  private double currentLocationLat = Double.NaN;

  private double currentLocationLon = Double.NaN;

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  private double currentOrientation = Double.NaN;

  private List<TimepointPredictionRecord> timepointPredictions;

  private EVehiclePhase phase;

  private String status;

  public VehicleLocationRecord() {

  }

  public VehicleLocationRecord(VehicleLocationRecord r) {
    this.blockId = r.blockId;
    this.currentLocationLat = r.currentLocationLat;
    this.currentLocationLon = r.currentLocationLon;
    this.currentOrientation = r.currentOrientation;
    this.timeOfRecord = r.timeOfRecord;
    this.timeOfLocationUpdate = r.timeOfLocationUpdate;
    this.distanceAlongBlock = r.distanceAlongBlock;
    this.scheduleDeviation = r.scheduleDeviation;
    this.serviceDate = r.serviceDate;
    this.tripId = r.tripId;
    this.vehicleId = r.vehicleId;

    List<TimepointPredictionRecord> timepointPredictions = r.getTimepointPredictions();
    if (timepointPredictions != null) {
      List<TimepointPredictionRecord> dup = new ArrayList<TimepointPredictionRecord>(
          timepointPredictions.size());
      for (TimepointPredictionRecord tpr : timepointPredictions)
        dup.add(new TimepointPredictionRecord(tpr));
      this.timepointPredictions = dup;
    }
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public AgencyAndId getBlockId() {
    return blockId;
  }

  public void setBlockId(AgencyAndId blockId) {
    this.blockId = blockId;
  }

  public int getBlockStartTime() {
    return blockStartTime;
  }

  public void setBlockStartTime(int blockStartTime) {
    this.blockStartTime = blockStartTime;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  /**
   * 
   * @return time when the vehicle location record was made, in unix-time (ms)
   */
  public long getTimeOfRecord() {
    return timeOfRecord;
  }

  /**
   * 
   * @param timeOfRecord time when the vehicle location record was made, in
   *          unix-time (ms)
   */
  public void setTimeOfRecord(long timeOfRecord) {
    this.timeOfRecord = timeOfRecord;
  }

  /**
   * 
   * @return time when the last vehicle location update made, in unix-time (ms)
   */
  public long getTimeOfLocationUpdate() {
    return timeOfLocationUpdate;
  }

  public void setTimeOfLocationUpdate(long timeOfLocationUpdate) {
    this.timeOfLocationUpdate = timeOfLocationUpdate;
  }

  /**
   * @return true if schedule deviation information has been provided
   */
  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  /**
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation - in seconds (+deviation is late, -deviation is
   *          early)
   */
  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean isDistanceAlongBlockSet() {
    return !Double.isNaN(distanceAlongBlock);
  }

  /**
   * 
   * @return the distance traveled along the block in meters, or NaN if not set
   */
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  /**
   * 
   * @param distanceAlongBlock distance traveled along the block in meters, or
   *          NaN if not set
   */
  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public boolean isCurrentLocationSet() {
    return !(Double.isNaN(currentLocationLat) || Double.isNaN(currentLocationLon));
  }

  public double getCurrentLocationLat() {
    return currentLocationLat;
  }

  public void setCurrentLocationLat(double currentLocationLat) {
    this.currentLocationLat = currentLocationLat;
  }

  public double getCurrentLocationLon() {
    return currentLocationLon;
  }

  public void setCurrentLocationLon(double currentLocationLon) {
    this.currentLocationLon = currentLocationLon;
  }

  public boolean isCurrentOrientationSet() {
    return !Double.isNaN(currentOrientation);
  }

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  public double getCurrentOrientation() {
    return currentOrientation;
  }

  public void setCurrentOrientation(double currentOrientation) {
    this.currentOrientation = currentOrientation;
  }

  public List<TimepointPredictionRecord> getTimepointPredictions() {
    return timepointPredictions;
  }

  public void setTimepointPredictions(
      List<TimepointPredictionRecord> timepointPredictions) {
    this.timepointPredictions = timepointPredictions;
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

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("VehicleLocationRecord:");
    if (blockId != null)
      b.append(" blockId=").append(blockId);
    if (tripId != null)
      b.append(" tripId").append(tripId);
    if (serviceDate != 0)
      b.append(" serviceDate=").append(serviceDate);
    if (timeOfRecord != 0)
      b.append(" time=").append(new java.util.Date(timeOfRecord));
    if (vehicleId != null)
      b.append(" vehicleId=").append(vehicleId);
    if (isScheduleDeviationSet())
      b.append(" scheduleDeviation=").append(scheduleDeviation);
    if (isDistanceAlongBlockSet())
      b.append(" distanceAlongBlock=").append(distanceAlongBlock);
    if (isCurrentLocationSet())
      b.append(" currentLocation=").append(currentLocationLat).append(" ").append(
          currentLocationLon);
    if (isCurrentOrientationSet())
      b.append(" currentOrientation=").append(currentOrientation);
    if (phase != null)
      b.append(" phase=").append(phase);
    if (status != null)
      b.append(" status=").append(status);
    return b.toString();
  }
}
