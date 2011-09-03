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
package org.onebusaway.api.model.transit;

import java.io.Serializable;

import org.onebusaway.csv_entities.schema.FlattenFieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.geospatial.model.CoordinatePoint;

public class VehicleLocationRecordV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  @CsvField(optional = true)
  private String blockId;

  @CsvField(optional = true)
  private String tripId;

  @CsvField(optional = true)
  private String vehicleId;

  private long timeOfRecord;

  private long timeOfLocationUpdate;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
  @CsvField(optional = true)
  private Double scheduleDeviation;

  @CsvField(optional = true)
  private Double distanceAlongBlock;

  @CsvField(optional = true, mapping = FlattenFieldMappingFactory.class)
  private CoordinatePoint currentLocation;

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  @CsvField(optional = true)
  private Double currentOrientation;

  @CsvField(optional = true)
  private String phase;

  @CsvField(optional = true)
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

  public Double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public Double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(Double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public CoordinatePoint getCurrentLocation() {
    return currentLocation;
  }

  public void setCurrentLocation(CoordinatePoint currentLocation) {
    this.currentLocation = currentLocation;
  }

  public Double getCurrentOrientation() {
    return currentOrientation;
  }

  public void setCurrentOrientation(Double currentOrientation) {
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
}
