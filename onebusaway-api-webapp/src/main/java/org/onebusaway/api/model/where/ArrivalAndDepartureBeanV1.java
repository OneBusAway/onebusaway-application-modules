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
package org.onebusaway.api.model.where;

import org.onebusaway.realtime.api.OccupancyStatus;

import java.io.Serializable;

public class ArrivalAndDepartureBeanV1 implements Serializable {

  private static final long serialVersionUID = 2L;

  private String routeId;

  private String routeShortName;

  private String tripId;

  private String tripHeadsign;

  private String stopId;

  private long predictedArrivalTime;

  private long scheduledArrivalTime;

  private long predictedDepartureTime;

  private String occupancyStatus;

  private String historicalOccupancy;

  private String predictedOccupancy;

  private long scheduledDepartureTime;

  private String status;

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

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
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

  public long getPredictedArrivalTime() {
    return predictedArrivalTime;
  }

  public void setPredictedArrivalTime(long predictedArrivalTime) {
    this.predictedArrivalTime = predictedArrivalTime;
  }

  public long getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(long scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
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

  public void setPredictedOccupancy(OccupancyStatus predOccupancy) { if(predOccupancy!= null) this.predictedOccupancy = predOccupancy.toString(); }

  public long getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(long scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean hasPredictedArrivalTime() {
    return this.predictedArrivalTime > 0;
  }

  public boolean hasPredictedDepartureTime() {
    return this.predictedArrivalTime > 0;
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
