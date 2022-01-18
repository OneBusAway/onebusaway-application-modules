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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "avl_trip_info")
@org.hibernate.annotations.Entity(mutable = false)

public class TripInfo {

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  @JsonIgnore
  private long id;

  @Column(nullable = true, name = "trip_id")
  @JsonProperty("TripId")
  private String tripId;

  @Column(nullable = true, name = "last_updated_date")
  @JsonProperty("LastUpdatedDate")
  private String lastUpdatedDate;

  @Column(nullable = true, name = "vehicle_id")
  @JsonProperty("VehicleId")
  private String vehicleId;

  @Column(nullable = true, name = "last_stop_name")
  @JsonProperty("LastStopName")
  private String lastStopName;

  @Column(nullable = true, name = "last_stop_id")
  @JsonProperty("LastStopId")
  private String lastStopId;

  @Column(nullable = true, name = "lat")
  @JsonProperty("Lat")
  private String lat;

  @Column(nullable = true, name = "lon")
  @JsonProperty("Lon")
  private String lon;

  @Column(nullable = true, name = "direction")
  @JsonProperty("Direction")
  private String direction;

  @Column(nullable = true, name = "train_id")
  @JsonProperty("TrainId")
  private String trainId;

  @Column(nullable = true, name = "start_date")
  @JsonProperty("StartDate")
  private String startDate;

  @OneToMany(cascade = {
      CascadeType.ALL}, mappedBy = "tripInfo", fetch = FetchType.EAGER)
  @Fetch(value = FetchMode.SUBSELECT)
  @JsonIgnore
  private List<StopUpdate> stopUpdates;

  @ManyToOne
  @JoinColumn(name = "link_avl_feed_id", nullable = false)
  @JsonIgnore
  private LinkAVLData linkAVLData;

  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getTripId() {
    return tripId;
  }
  public void setTripId(String tripId) {
    this.tripId = tripId;
  }
  public String getLastUpdatedDate() {
    return lastUpdatedDate;
  }
  public void setLastUpdatedDate(String lastUpdatedDate) {
    this.lastUpdatedDate = lastUpdatedDate;
  }
  public String getVehicleId() {
    return vehicleId;
  }
  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
  public String getLastStopName() {
    return lastStopName;
  }
  public void setLastStopName(String lastStopName) {
    this.lastStopName = lastStopName;
  }
  public String getLastStopId() {
    return lastStopId;
  }
  public void setLastStopId(String lastStopId) {
    this.lastStopId = lastStopId;
  }
  public String getLat() {
    return lat;
  }
  public void setLat(String lat) {
    this.lat = lat;
  }
  public String getLon() {
    return lon;
  }
  public void setLon(String lon) {
    this.lon = lon;
  }
  public String getDirection() {
    return direction;
  }
  public void setDirection(String direction) {
    this.direction = direction;
  }
  public String getTrainId() {
    return trainId;
  }
  public void setTrainId(String trainId) {
    this.trainId = trainId;
  }
  public String getStartDate() {
    return startDate;
  }
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }
  public List<StopUpdate> getStopUpdates() {
    return stopUpdates;
  }
  @JsonProperty("StopUpdates")
  public void setStopUpdates(StopUpdatesList stopUpdatesList) {
    if (stopUpdates == null) {
      stopUpdates = new ArrayList<StopUpdate>();
    }
    if (stopUpdatesList != null && stopUpdatesList.getUpdates() != null) {
      for (StopUpdate stopUpdate: stopUpdatesList.getUpdates()) {
        stopUpdates.add(stopUpdate);
      }
    }
  }
  public LinkAVLData getLinkAVLData() {
    return linkAVLData;
  }
  public void setLinkAVLData(LinkAVLData linkAVLData) {
    this.linkAVLData = linkAVLData;
  }
}
