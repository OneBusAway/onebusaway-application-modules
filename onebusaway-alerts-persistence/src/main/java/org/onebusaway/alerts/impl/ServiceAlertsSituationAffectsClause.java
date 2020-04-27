/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.alerts.impl;

import javax.persistence.*;

import org.apache.commons.lang.StringUtils;

@Entity
@Table(name = "transit_data_service_alerts_situation_affects")
public class ServiceAlertsSituationAffectsClause {

  @Id
  @GeneratedValue
  private int id = 0;

  private String agencyId = null;

  @Column(nullable = true, name="route_id")
  private String routeId = null;

  @Column(nullable = true, name="direction_id")
  private String directionId = null;
  
  @Column(nullable = true, name="trip_id")
  private String tripId = null;

  @Column(nullable = true, name="stop_id")
  private String stopId = null;

  @Column(nullable = true, name="application_id")
  private String applicationId = null;

  @ManyToOne
  private ServiceAlertRecord serviceAlertRecord;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getAgencyId() {
    if (StringUtils.isBlank(agencyId)) return null;
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getRouteId() {
    if (StringUtils.isBlank(routeId)) return null;
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getDirectionId() {
    if (StringUtils.isBlank(directionId)) return null;
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getTripId() {
    if (StringUtils.isBlank(tripId)) return null;
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getStopId() {
    if (StringUtils.isBlank(stopId)) return null;
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getApplicationId() {
    if (StringUtils.isBlank(applicationId)) return null;
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public ServiceAlertRecord getServiceAlertRecord() {
    return serviceAlertRecord;
  }

  public void setServiceAlertRecord(ServiceAlertRecord serviceAlertRecord) {
    this.serviceAlertRecord = serviceAlertRecord;
  }
}
