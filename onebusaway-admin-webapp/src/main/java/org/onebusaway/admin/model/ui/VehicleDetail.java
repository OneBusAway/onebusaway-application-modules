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
package org.onebusaway.admin.model.ui;

public class VehicleDetail {

  private String vehicleId;
  private String headSign;
  private String inferredHeadSign;
  private String agency;
  private String depot;
  private String operatorId;
  private String serviceDate;
  private long scheduleDeviation;
  private String routeString;
  private String tripId;
  private String observedRunId;
  private String utsRunId;
  private String inferredRunId;
  private String location;
  private Double direction;

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getHeadSign() {
    return headSign;
  }

  public void setHeadSign(String headSign) {
    this.headSign = headSign;
  }

  public String getInferredHeadSign() {
    return inferredHeadSign;
  }

  public void setInferredHeadSign(String inferredHeadSign) {
    this.inferredHeadSign = inferredHeadSign;
  }

  public String getAgency() {
    return agency;
  }

  public void setAgency(String agency) {
    this.agency = agency;
  }

  public String getDepot() {
    return depot;
  }

  public void setDepot(String depot) {
    this.depot = depot;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(String serviceDate) {
    this.serviceDate = serviceDate;
  }

  public long getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(long scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public String getRouteString() {
    return routeString;
  }

  public void setRouteString(String routeString) {
    this.routeString = routeString;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getObservedRunId() {
    return observedRunId;
  }

  public void setObservedRunId(String observedRunId) {
    this.observedRunId = observedRunId;
  }

  public String getUtsRunId() {
    return utsRunId;
  }

  public void setUtsRunId(String utsRunId) {
    this.utsRunId = utsRunId;
  }

  public String getInferredRunId() {
    return inferredRunId;
  }

  public void setInferredRunId(String inferredRunId) {
    this.inferredRunId = inferredRunId;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Double getDirection() {
    return direction;
  }

  public void setDirection(Double direction) {
    this.direction = direction;
  }
  
  public String toString() {
    return "VehicleDetail[" + "vehicleId=" + vehicleId + ", depot=" + depot + "]";
  }
}
