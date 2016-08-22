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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs_realtime.interfaces.HasRouteId;
import org.onebusaway.gtfs_realtime.interfaces.HasStopId;
import org.onebusaway.gtfs_realtime.interfaces.HasTripId;

@Entity
@Table(name = "entity_selector")
@org.hibernate.annotations.Table(appliesTo = "entity_selector", indexes = {
    @Index(name = "es_id_idx", columnNames = {"id"}),
    @Index(name = "es_agency_id_idx", columnNames = {"agency_id"}),
    @Index(name = "es_route_id_idx", columnNames = {"route_id"}),
    @Index(name = "es_stop_id_idx", columnNames = {"stop_id"}),
    @Index(name = "es_trip_id_idx", columnNames = {"trip_id"}),
    @Index(name = "es_trip_route_id_idx", columnNames = {"trip_route_id"}),
    @Index(name = "es_alert_id_idx", columnNames = {"alert_id"})})
@org.hibernate.annotations.Entity(mutable = false)
public class EntitySelectorModel implements HasTripId, HasRouteId, HasStopId {

  /* Sound Transit constants */

  private static final int AGENCY_ID_LENGTH = 15;
  private static final int ROUTE_ID_LENGTH = 20;
  private static final int STOP_ID_LENGTH = 20;
  private static final int TRIP_ID_LENGTH = 20;
  private static final int TRIP_ROUTE_ID_LENGTH = 20;
  private static final int TRIP_START_TIME_LENGTH = 8;
  private static final int TRIP_START_DATE_LENGTH = 10;

  /* Test constants */
  /*
   * private static final int AGENCY_ID_LENGTH = 15; private static final int
   * ROUTE_ID_LENGTH = 50; private static final int STOP_ID_LENGTH = 50; private
   * static final int TRIP_ID_LENGTH = 100; private static final int
   * TRIP_ROUTE_ID_LENGTH = 50; private static final int TRIP_START_TIME_LENGTH
   * = 8; private static final int TRIP_START_DATE_LENGTH = 10;
   */

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  private long id;
  @Column(nullable = true, name = "agency_id", length = AGENCY_ID_LENGTH)
  private String agencyId;
  @Column(nullable = true, name = "route_id", length = ROUTE_ID_LENGTH)
  private String routeId;
  @Column(nullable = true, name = "route_type")
  private long routeType;
  @Column(nullable = true, name = "stop_id", length = STOP_ID_LENGTH)
  private String stopId;
  @Column(nullable = true, name = "trip_id", length = TRIP_ID_LENGTH)
  private String tripId;
  @Column(nullable = true, name = "trip_route_id", length = TRIP_ROUTE_ID_LENGTH)
  private String tripRouteId;
  @Column(nullable = true, name = "trip_start_time", length = TRIP_START_TIME_LENGTH)
  private String tripStartTime;
  @Column(nullable = true, name = "trip_start_date", length = TRIP_START_DATE_LENGTH)
  private String tripStartDate;

  @ManyToOne
  @JoinColumn(nullable = false, name = "alert_id")
  private AlertModel alert;

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

  public String getTripStartTime() {
    return tripStartTime;
  }

  public void setTripStartTime(String tripStartTime) {
    this.tripStartTime = tripStartTime;
  }

  public String getTripStartDate() {
    return tripStartDate;
  }

  public void setTripStartDate(String tripStartDate) {
    this.tripStartDate = tripStartDate;
  }

  public AlertModel getAlert() {
    return alert;
  }

  public void setAlert(AlertModel alert) {
    this.alert = alert;
  }

}
