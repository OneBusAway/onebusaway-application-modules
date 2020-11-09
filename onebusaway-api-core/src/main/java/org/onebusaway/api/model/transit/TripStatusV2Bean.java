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
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.realtime.api.OccupancyStatus;

public final class TripStatusV2Bean implements Serializable {

  private static final long serialVersionUID = 3L;

  /****
   * These are fields that we can supply from schedule data
   ****/

  private String activeTripId;

  private int blockTripSequence = -1;

  private long serviceDate;

//  private String realtimeOccupancy;

  private FrequencyV2Bean frequency;

  private Double scheduledDistanceAlongTrip;

  private Double totalDistanceAlongTrip;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private CoordinatePoint position;

  private Double orientation;

  private String closestStop;

  private int closestStopTimeOffset;

  private String nextStop;

  private int nextStopTimeOffset;

  private String phase;

  private String status;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private String occupancyStatus;

  private Integer occupancyCount;

  private Integer occupancyCapacity;

  private boolean predicted = false;

  private Long lastUpdateTime;

  private Long lastLocationUpdateTime;

  private Double lastKnownDistanceAlongTrip;

  private CoordinatePoint lastKnownLocation;

  private Double lastKnownOrientation;

  private Integer scheduleDeviation;

  private Double distanceAlongTrip;

  private String vehicleId;

  private List<String> situationIds;

  public String getActiveTripId() {
    return activeTripId;
  }

  public void setActiveTripId(String activeTripId) {
    this.activeTripId = activeTripId;
  }

  public int getBlockTripSequence() {
    return blockTripSequence;
  }

  public void setBlockTripSequence(int blockTripSequence) {
    this.blockTripSequence = blockTripSequence;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

//  public String getRealtimeOccupancy() { return realtimeOccupancy; }
//
//  public void setRealtimeOccupancy(String realtimeOccupancy) { this.realtimeOccupancy = realtimeOccupancy; }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
  }

  public Double getScheduledDistanceAlongTrip() {
    return scheduledDistanceAlongTrip;
  }

  public void setScheduledDistanceAlongTrip(Double scheduledDistanceAlongTrip) {
    this.scheduledDistanceAlongTrip = scheduledDistanceAlongTrip;
  }

  public Double getTotalDistanceAlongTrip() {
    return totalDistanceAlongTrip;
  }

  public void setTotalDistanceAlongTrip(Double totalDistanceAlongTrip) {
    this.totalDistanceAlongTrip = totalDistanceAlongTrip;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }

  public Double getOrientation() {
    return orientation;
  }

  public void setOrientation(Double orientation) {
    this.orientation = orientation;
  }

  public String getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(String closestStop) {
    this.closestStop = closestStop;
  }

  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }

  public String getNextStop() {
    return nextStop;
  }

  public void setNextStop(String nextStop) {
    this.nextStop = nextStop;
  }

  public int getNextStopTimeOffset() {
    return nextStopTimeOffset;
  }

  public void setNextStopTimeOffset(int nextStopTimeOffset) {
    this.nextStopTimeOffset = nextStopTimeOffset;
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

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public Long getLastLocationUpdateTime() {
    return lastLocationUpdateTime;
  }

  public void setLastLocationUpdateTime(Long lastLocationUpdateTime) {
    this.lastLocationUpdateTime = lastLocationUpdateTime;
  }

  public CoordinatePoint getLastKnownLocation() {
    return lastKnownLocation;
  }

  public void setLastKnownLocation(CoordinatePoint lastKnownLocation) {
    this.lastKnownLocation = lastKnownLocation;
  }

  public Double getLastKnownDistanceAlongTrip() {
    return lastKnownDistanceAlongTrip;
  }

  public void setLastKnownDistanceAlongTrip(Double lastKnownDistanceAlongTrip) {
    this.lastKnownDistanceAlongTrip = lastKnownDistanceAlongTrip;
  }

  public Double getLastKnownOrientation() {
    return lastKnownOrientation;
  }

  public void setLastKnownOrientation(Double lastKnownOrientation) {
    this.lastKnownOrientation = lastKnownOrientation;
  }

  public Integer getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Integer scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public Double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(Double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public List<String> getSituationIds() {
    return situationIds;
  }

  public void setSituationIds(List<String> situationIds) {
    this.situationIds = situationIds;
  }

  public String getOccupancyStatus() { return occupancyStatus; }

  public void setOccupancyStatus(OccupancyStatus occupancyStatus) {
    if (occupancyStatus != null)
      this.occupancyStatus = occupancyStatus.name();
  }

  public Integer getOccupancyCount() {
    return occupancyCount;
  }

  public void setOccupancyCount(Integer occupancyCount) {
    this.occupancyCount = occupancyCount;
  }

  public Integer getOccupancyCapacity() {
    return occupancyCapacity;
  }

  public void setOccupancyCapacity(Integer occupancyCapacity) {
    this.occupancyCapacity = occupancyCapacity;
  }
}
