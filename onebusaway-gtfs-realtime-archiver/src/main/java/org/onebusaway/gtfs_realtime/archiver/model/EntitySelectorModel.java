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
package org.onebusaway.gtfs_realtime.archiver.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

@Entity
@Table(name="entity")
@org.hibernate.annotations.Table(appliesTo ="entity", indexes = {
    @Index(name = "entity_idx", columnNames = {
        "id","agency_id","route_id","route_type","stop_id","trip_id","trip_route_id","trip_start","alert_id"
    })})
@org.hibernate.annotations.Entity(mutable = false)
public class EntityUpdate {

  public EntityUpdate() {
    
  }
  
  @Id
  @GeneratedValue
  private long id;
  @Column(nullable = true, name="agency_id", length = 15)
  private String agencyId;
  @Column(nullable = true, name="route_id", length = 10)
  private String routeId;
  @Column(nullable = true, name="route_type")
  private long routeType;
  @Column(nullable = true, name="stop_id", length = 10)
  private String stopId;
  @Column(nullable = true, name="trip_id", length = 10)
  private String tripId;
  @Column(nullable = true, name="trip_route_id", length = 10)
  private String tripRouteId;
  @Column(nullable = true, name="agency_id", length = 15)
  private Date tripStart;
  @Column(nullable = true, name="alert_id")
  private long alertId;
  
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getAgencyId() {
    return agencyId;
  }
  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }
  public String getRouteId() {
    return routeId;
  }
  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }
  public long getRouteType() {
    return routeType;
  }
  public void setRouteType(long routeType) {
    this.routeType = routeType;
  }
  public String getStopId() {
    return stopId;
  }
  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
  public String getTripId() {
    return tripId;
  }
  public void setTripId(String tripId) {
    this.tripId = tripId;
  }
  public String getTripRouteId() {
    return tripRouteId;
  }
  public void setTripRouteId(String tripRouteId) {
    this.tripRouteId = tripRouteId;
  }
  public Date getTripStart() {
    return tripStart;
  }
  public void setTripStart(Date tripStart) {
    this.tripStart = tripStart;
  }
  public long getAlertId() {
    return alertId;
  }
  public void setAlertId(long alertId) {
    this.alertId = alertId;
  }
  
}
