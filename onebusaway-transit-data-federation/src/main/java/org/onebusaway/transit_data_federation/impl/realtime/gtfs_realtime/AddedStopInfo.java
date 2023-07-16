/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

/**
 * Represent an added stop from GTFS-RT.
 */
public class AddedStopInfo {

  private String stopId;
  private long arrivalTime = -1;
  private long departureTime = -1;
  private String scheduledTrack = null;
  private String actualTrack = null;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  // in millis as long implies
  public long getArrivalTime() {
    return arrivalTime;
  }

  // in millis as long implies
  public void setArrivalTime(long arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  // in millis as long implies
  public long getDepartureTime() {
    return departureTime;
  }

  // in millis as long implies
  public void setDepartureTime(long departureTime) {
    this.departureTime = departureTime;
  }

  public String getScheduledTrack() {
    return scheduledTrack;
  }

  public void setScheduledTrack(String scheduledTrack) {
    this.scheduledTrack = scheduledTrack;
  }

  public String getActualTrack() {
    return actualTrack;
  }

  public void setActualTrack(String actualTrack) {
    this.actualTrack = actualTrack;
  }
}
