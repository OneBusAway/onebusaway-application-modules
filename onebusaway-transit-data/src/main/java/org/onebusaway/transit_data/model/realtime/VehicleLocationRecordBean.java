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
package org.onebusaway.transit_data.model.realtime;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class VehicleLocationRecordBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private String blockId;

  private String tripId;

  private String vehicleId;

  private long timeOfRecord;

  private long timeOfLocationUpdate;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
  private double scheduleDeviation = Double.NaN;

  private double distanceAlongBlock = Double.NaN;

  private CoordinatePoint currentLocation;

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  private double currentOrientation = Double.NaN;

  private String phase;

  private String status;

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public long getTimeOfRecord() {
    return timeOfRecord;
  }

  public void setTimeOfRecord(long timeOfRecord) {
    this.timeOfRecord = timeOfRecord;
  }

  public long getTimeOfLocationUpdate() {
    return timeOfLocationUpdate;
  }

  public void setTimeOfLocationUpdate(long timeOfLocationUpdate) {
    this.timeOfLocationUpdate = timeOfLocationUpdate;
  }

  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean isDistanceAlongBlockSet() {
    return !Double.isNaN(distanceAlongBlock);
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public CoordinatePoint getCurrentLocation() {
    return currentLocation;
  }

  public void setCurrentLocation(CoordinatePoint currentLocation) {
    this.currentLocation = currentLocation;
  }

  public boolean isCurrentOrientationSet() {
    return !Double.isNaN(currentOrientation);
  }

  public double getCurrentOrientation() {
    return currentOrientation;
  }

  public void setCurrentOrientation(double currentOrientation) {
    this.currentOrientation = currentOrientation;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * All field values will be copied from the specified bean into our own bean.
   * 
   * @param bean values copied from this bean
   */
  public void copyFrom(VehicleLocationRecordBean bean) {
    this.blockId = bean.blockId;
    this.currentLocation = bean.currentLocation;
    this.currentOrientation = bean.currentOrientation;
    this.distanceAlongBlock = bean.distanceAlongBlock;
    this.phase = bean.phase;
    this.scheduleDeviation = bean.scheduleDeviation;
    this.serviceDate = bean.serviceDate;
    this.status = bean.status;
    this.timeOfLocationUpdate = bean.timeOfLocationUpdate;
    this.timeOfRecord = bean.timeOfRecord;
    this.tripId = bean.vehicleId;
    this.vehicleId = bean.vehicleId;
  }
}
