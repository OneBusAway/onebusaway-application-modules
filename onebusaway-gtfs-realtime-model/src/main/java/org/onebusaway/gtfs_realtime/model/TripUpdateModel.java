/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs_realtime.interfaces.FeedEntityModel;
import org.onebusaway.gtfs_realtime.interfaces.HasRouteId;
import org.onebusaway.gtfs_realtime.interfaces.HasTripId;

@Entity
@Table(name = "trip_update")
@org.hibernate.annotations.Table(appliesTo = "trip_update", indexes = {
    @Index(name = "tu_id_idx", columnNames = {"id"}),
    @Index(name = "tu_trip_id_idx", columnNames = {"trip_id"}),
    @Index(name = "tu_route_id_idx", columnNames = {"route_id"}),
    @Index(name = "tu_vehicle_id_idx", columnNames = {"vehicle_id"}),
    @Index(name = "tu_timestamp_idx", columnNames = {"timestamp"})})
@org.hibernate.annotations.Entity(mutable = false)
/**
 * Inspired by https://github.com/mattwigway/gtfsrdb Represents an individual
 * trip update.
 *
 */
public class TripUpdateModel implements FeedEntityModel, HasTripId, HasRouteId {

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  private long id;
  @Column(nullable = true, name = "trip_id", length = 20)
  private String tripId;
  @Column(nullable = true, name = "route_id", length = 20)
  private String routeId;
  @Column(nullable = true, name = "trip_start")
  private Date tripStart;
  // see enum transit_realtime.TripDescriptor.ScheduleRelationship
  @Column(nullable = false, name = "schedule_relationship")
  private int scheduleRelationship;
  @Column(nullable = true, name = "vehicle_id", length = 20)
  private String vehicleId;
  @Column(nullable = true, name = "vehicle_label", length = 20)
  private String vehicleLabel;
  @Column(nullable = true, name = "vehicle_license_plate", length = 15)
  private String vehicleLicensePlate;
  @Column(nullable = true, name = "timestamp")
  private Date timestamp;
  @Column(nullable = true, name = "delay")
  private Integer delay;


  @OneToMany(cascade = {
      CascadeType.ALL}, mappedBy = "tripUpdate", fetch = FetchType.EAGER)
  private List<StopTimeUpdateModel> stopTimeUpdates = new ArrayList<StopTimeUpdateModel>();

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

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public Date getTripStart() {
    return tripStart;
  }

  public void setTripStart(Date tripStart) {
    this.tripStart = tripStart;
  }

  public int getScheduleRelationship() {
    return scheduleRelationship;
  }

  public void setScheduleRelationship(int scheduleRelationship) {
    this.scheduleRelationship = scheduleRelationship;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getVehicleLabel() {
    return vehicleLabel;
  }

  public void setVehicleLabel(String vehicleLabel) {
    this.vehicleLabel = vehicleLabel;
  }

  public String getVehicleLicensePlate() {
    return vehicleLicensePlate;
  }

  public void setVehicleLicensePlate(String vehicleLicensePlate) {
    this.vehicleLicensePlate = vehicleLicensePlate;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public List<StopTimeUpdateModel> getStopTimeUpdates() {
    return stopTimeUpdates;
  }

  public void setStopTimeUpdates(List<StopTimeUpdateModel> updates) {
    this.stopTimeUpdates = updates;
  }

  public void addStopTimeUpdateModel(StopTimeUpdateModel stopTimeUpdate) {
    if (stopTimeUpdate != null) {
      stopTimeUpdates.add(stopTimeUpdate);
    }
  }

  public Integer getDelay() {
    return delay;
  }
  public void setDelay(Integer delay) {
    this.delay = delay;
  }
}
