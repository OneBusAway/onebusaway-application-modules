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

public class TripStopTimeV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int arrivalTime;

  private int departureTime;

  private String stopId;

  private String stopHeadsign;

  private double distanceAlongTrip;

  private String historicalOccupancy;

  public long getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    this.departureTime = departureTime;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public void setStopHeadsign(String stopHeadsign) {
    this.stopHeadsign = stopHeadsign;
  }

  public double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  public String getHistoricalOccupancy() { return historicalOccupancy; }

  public void setHistoricalOccupancy(String historicalOccupancy) {this.historicalOccupancy = historicalOccupancy; }

  public void setHistoricalOccupancy(OccupancyStatus historicalOccupancy) { if(historicalOccupancy != null) this.historicalOccupancy = historicalOccupancy.toString(); }
}
