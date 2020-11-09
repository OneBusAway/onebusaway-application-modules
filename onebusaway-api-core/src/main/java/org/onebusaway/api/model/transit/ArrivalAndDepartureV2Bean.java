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

import org.onebusaway.realtime.api.OccupancyStatus;

import java.io.Serializable;
import java.util.List;

public class ArrivalAndDepartureV2Bean implements Serializable {

  private static final long serialVersionUID = 4L;

  private String routeId;

  private String tripId;

  private long serviceDate;

  private String vehicleId;

  private String stopId;

  private int stopSequence;

  private int blockTripSequence = -1;

  private String routeShortName;

  private String routeLongName;

  private String tripHeadsign;
  
  private boolean departureEnabled;

  private long scheduledDepartureTime;

  private TimeIntervalV2 scheduledDepartureInterval;

  private long predictedDepartureTime;

  private String occupancyStatus;

  private String historicalOccupancy;

  private String predictedOccupancy;

  private TimeIntervalV2 predictedDepartureInterval;
  
  private boolean arrivalEnabled;

  private long scheduledArrivalTime;

  private TimeIntervalV2 scheduledArrivalInterval;

  private FrequencyV2Bean frequency;

  private String status;

  private boolean predicted = false;

  private Long lastUpdateTime;

  private long predictedArrivalTime;

  private TimeIntervalV2 predictedArrivalInterval;

  private double distanceFromStop;

  private int numberOfStopsAway;

  private TripStatusV2Bean tripStatus;

  private List<String> situationIds;
  
  private int totalStopsInTrip;

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getRouteLongName() {
    return routeLongName;
  }

  public void setRouteLongName(String routeLongName) {
    this.routeLongName = routeLongName;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    this.stopSequence = stopSequence;
  }

  public int getBlockTripSequence() {
    return blockTripSequence;
  }

  public void setBlockTripSequence(int blockTripSequence) {
    this.blockTripSequence = blockTripSequence;
  }

  public boolean isArrivalEnabled() {
    return arrivalEnabled;
  }

  public void setArrivalEnabled(boolean arrivalEnabled) {
    this.arrivalEnabled = arrivalEnabled;
  }

  public long getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(long scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  public TimeIntervalV2 getScheduledArrivalInterval() {
    return scheduledArrivalInterval;
  }

  public void setScheduledArrivalInterval(
      TimeIntervalV2 scheduledArrivalInterval) {
    this.scheduledArrivalInterval = scheduledArrivalInterval;
  }

  public boolean isDepartureEnabled() {
    return departureEnabled;
  }

  public void setDepartureEnabled(boolean departureEnabled) {
    this.departureEnabled = departureEnabled;
  }

  public long getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(long scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public TimeIntervalV2 getScheduledDepartureInterval() {
    return scheduledDepartureInterval;
  }

  public void setScheduledDepartureInterval(
      TimeIntervalV2 scheduledDepartureInterval) {
    this.scheduledDepartureInterval = scheduledDepartureInterval;
  }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
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

  public double getDistanceFromStop() {
    return distanceFromStop;
  }

  public void setDistanceFromStop(double distanceFromStop) {
    this.distanceFromStop = distanceFromStop;
  }

  public int getNumberOfStopsAway() {
    return numberOfStopsAway;
  }

  public void setNumberOfStopsAway(int numberOfStopsAway) {
    this.numberOfStopsAway = numberOfStopsAway;
  }

  public TripStatusV2Bean getTripStatus() {
    return tripStatus;
  }

  public void setTripStatus(TripStatusV2Bean tripStatus) {
    this.tripStatus = tripStatus;
  }

  public List<String> getSituationIds() {
    return situationIds;
  }

  public void setSituationIds(List<String> situationIds) {
    this.situationIds = situationIds;
  }

  public boolean hasPredictedArrivalTime() {
    return this.predictedArrivalTime > 0;
  }

  public long getPredictedArrivalTime() {
    return predictedArrivalTime;
  }

  public void setPredictedArrivalTime(long predictedArrivalTime) {
    this.predictedArrivalTime = predictedArrivalTime;
  }

  public TimeIntervalV2 getPredictedArrivalInterval() {
    return predictedArrivalInterval;
  }

  public void setPredictedArrivalInterval(
      TimeIntervalV2 predictedArrivalInterval) {
    this.predictedArrivalInterval = predictedArrivalInterval;
  }

  public boolean hasPredictedDepartureTime() {
    return this.predictedArrivalTime > 0;
  }

  public long getPredictedDepartureTime() {
    return predictedDepartureTime;
  }

  public void setPredictedDepartureTime(long predictedDepartureTime) {
    this.predictedDepartureTime = predictedDepartureTime;
  }

  public String getOccupancyStatus() { return occupancyStatus; }

  public void setOccupancyStatus(OccupancyStatus occupancyStatus) { if (occupancyStatus != null) this.occupancyStatus = occupancyStatus.name(); }

  public String getHistoricalOccupancy() { return historicalOccupancy; }

  public void setHistoricalOccupancy(OccupancyStatus historicalOccupancy) { if(historicalOccupancy != null) this.historicalOccupancy = historicalOccupancy.toString(); }

  public String getPredictedOccupancy() { return predictedOccupancy; }

  public void setPredictedOccupancy(OccupancyStatus predOccupancy) { this.predictedOccupancy = predOccupancy.toString(); }

  public TimeIntervalV2 getPredictedDepartureInterval() {
    return predictedDepartureInterval;
  }

  public void setPredictedDepartureInterval(
      TimeIntervalV2 predictedDepartureInterval) {
    this.predictedDepartureInterval = predictedDepartureInterval;
  }

  public int getTotalStopsInTrip() {
	return totalStopsInTrip;
}

public void setTotalStopsInTrip(int totalStopsInTrip) {
	this.totalStopsInTrip = totalStopsInTrip;
}

public long computeBestArrivalTime() {
    return hasPredictedArrivalTime() ? getPredictedArrivalTime()
        : getScheduledArrivalTime();
  }

  public long computeBestDepartureTime() {
    return hasPredictedDepartureTime() ? getPredictedDepartureTime()
        : getScheduledDepartureTime();
  }

  @Override
  public String toString() {
    return "route=" + routeShortName + " scheduled=" + scheduledArrivalTime
        + " predicted=" + predictedArrivalTime;
  }
}
