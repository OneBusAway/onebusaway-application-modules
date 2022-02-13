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
package org.onebusaway.transit_data.model.service_alerts;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.util.AgencyAndIdLibrary;

import java.io.Serializable;

public final class SituationAffectsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String routeId;

  private String directionId;

  private String tripId;

  private String stopId;

  private String applicationId;

  private String routePartRouteId;
  private String agencyPartRouteId;
  private String agencyPartStopId;
  private String stopPartStopId;

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

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public void setAgencyPartRouteId(String agencyPartRouteId) {
    this.agencyPartRouteId = agencyPartRouteId;
  }

  public void setRoutePartRouteId(String routePartRouteId) {
    this.routePartRouteId = routePartRouteId;
  }

  public String getAgencyPartRouteId() {
    if (StringUtils.isBlank(this.routeId)) return null;
    AgencyAndId result = AgencyAndIdLibrary.convertFromString(this.routeId);
    return result.getAgencyId();
  }

  public String getRoutePartRouteId() {
    if (StringUtils.isBlank(this.routeId)) return null;
    AgencyAndId result = AgencyAndIdLibrary.convertFromString(this.routeId);
    return result.getId();
  }

  public void setAgencyPartStopId(String agencyPartStopId) {
    this.agencyPartStopId = agencyPartStopId;
  }

  public void setStopPartStopId(String stopPartStopId) {
    this.stopPartStopId = stopPartStopId;
  }

  public String getAgencyPartStopId() {
    if (StringUtils.isBlank(this.stopId)) return null;
    AgencyAndId result = AgencyAndIdLibrary.convertFromString(this.stopId);
    return result.getAgencyId();
  }

  public String getStopPartStopId() {
    if (StringUtils.isBlank(this.stopId)) return null;
    AgencyAndId result = AgencyAndIdLibrary.convertFromString(this.stopId);
    return result.getId();
  }

  /**
   * Checks partial id fields and combines them to make an AgencyAndId that will go to the routeId and/or stopId field
   */
  public void combineIds() {
    if (StringUtils.isNotBlank(this.agencyPartRouteId) && StringUtils.isNotBlank(this.routePartRouteId)) {//we need to combine to create routeId
      AgencyAndId routeResult = new AgencyAndId(this.agencyPartRouteId, this.routePartRouteId);
      setRouteId(AgencyAndIdLibrary.convertToString(routeResult));
    }
    if (StringUtils.isNotBlank(this.agencyPartStopId) && StringUtils.isNotBlank(this.stopPartStopId)) {//we need to combine to create stopId
      AgencyAndId stopResult = new AgencyAndId(this.agencyPartStopId, this.stopPartStopId);
      setStopId(AgencyAndIdLibrary.convertToString(stopResult));
    }
  }

}
