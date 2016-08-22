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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs_realtime.interfaces.FeedEntityModel;
import org.onebusaway.gtfs_realtime.interfaces.HasRouteId;
import org.onebusaway.gtfs_realtime.interfaces.HasStopId;
import org.onebusaway.gtfs_realtime.interfaces.HasTripId;

@Entity
@Table(name = "vehicle_position")
@org.hibernate.annotations.Table(appliesTo = "vehicle_position", indexes = {
    @Index(name = "vp_id_idx", columnNames = {"id"}),
    @Index(name = "vp_trip_id_idx", columnNames = {"trip_id"}),
    @Index(name = "vp_route_id_idx", columnNames = {"route_id"}),
    @Index(name = "vp_vehicle_id_idx", columnNames = {"vehicle_id"}),
    @Index(name = "vp_lat_idx", columnNames = {"lat"}),
    @Index(name = "vp_lon_idx", columnNames = {"lon"}),
    @Index(name = "vp_stop_id_idx", columnNames = {"stop_id"}),
    @Index(name = "vp_timestamp_idx", columnNames = {"timestamp"})})
@org.hibernate.annotations.Entity(mutable = false)

public class VehiclePositionModel implements FeedEntityModel, HasTripId, HasRouteId, HasStopId {

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
  @Column(nullable = true, name = "vehicle_id", length = 20)
  private String vehicleId;
  @Column(nullable = true, name = "vehicle_label", length = 20)
  private String vehicleLabel;
  @Column(nullable = true, name = "vehicle_license_plate", length = 15)
  private String vehicleLicensePlate;
  @Column(nullable = true, name = "lat")
  private Float lat;
  @Column(nullable = true, name = "lon")
  private Float lon;
  @Column(nullable = true, name = "bearing")
  private Float bearing;
  @Column(nullable = true, name = "speed")
  private Float speed;
  @Column(nullable = true, name = "stop_id", length = 20)
  private String stopId;
  @Column(nullable = true, name = "timestamp")
  private Date timestamp;

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

  public Float getLat() {
    return lat;
  }

  public void setLat(Float lat) {
    this.lat = lat;
  }

  public Float getLon() {
    return lon;
  }

  public void setLon(Float lon) {
    this.lon = lon;
  }

  public Float getBearing() {
    return bearing;
  }

  public void setBearing(Float bearing) {
    this.bearing = bearing;
  }

  public Float getSpeed() {
    return speed;
  }

  public void setSpeed(Float speed) {
    this.speed = speed;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String toString() {
    return "{vehicleId=" + vehicleId + " (" + lat + ", " + lon + ")" + " @"
        + timestamp + "}";
  }
}
