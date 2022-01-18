/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "avl_stop_updates")
@org.hibernate.annotations.Entity(mutable = false)

public class StopUpdate {

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  @JsonIgnore
  private long id;

  @Column(nullable = true, name = "stop_id")
  @JsonProperty("StopId")
  private String stopId;

  @Column(nullable = true, name = "station_name")
  @JsonProperty("StationName")
  private String stationName;

  @Column(nullable = true, name = "frequency")
  @JsonProperty("Frequency")
  private String frequency;

  @Column(nullable = true, name = "time_actual")
  @JsonIgnore
  private String timeActual;

  @Column(nullable = true, name = "time_scheduled")
  @JsonIgnore
  private String timeScheduled;

  @Column(nullable = true, name = "time_estimated")
  @JsonIgnore
  private String timeEstimated;

  @ManyToOne
  @JoinColumn(name = "trip_info_id", nullable = false)
  @JsonIgnore 
  private TripInfo tripInfo; 

  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getStopId() {
    return stopId;
  }
  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
  public String getStationName() {
    return stationName;
  }
  public void setStationName(String stationName) {
    this.stationName = stationName;
  }
  public String getFrequency() {
    return frequency;
  }
  public void setFrequency(String frequency) {
    this.frequency = frequency;
  }
  public String getTimeActual() {
    return timeActual;
  }
  public void setTimeActual(String timeActual) {
    this.timeActual = timeActual;
  }
  public String getTimeScheduled() {
    return timeScheduled;
  }
  public void setTimeScheduled(String timeScheduled) {
    this.timeScheduled = timeScheduled;
  }
  public String getTimeEstimated() {
    return timeEstimated;
  }
  public void setTimeEstimated(String timeEstimated) {
    this.timeEstimated = timeEstimated;
  }
  @JsonProperty("ArrivalTime")
  public void setArrivalTime(ArrivalTime arrivalTime) {
    if (arrivalTime != null) {
      this.timeActual = arrivalTime.getActual();
      this.timeScheduled = arrivalTime.getScheduled();
      this.timeEstimated = arrivalTime.getEstimated();
    }
  }
  public TripInfo getTripInfo() {
    return tripInfo;
  }
  public void setTripInfo(TripInfo tripInfo) {
    this.tripInfo = tripInfo;
  }
}
