/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data.model.problems;

import java.io.Serializable;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.trips.TripBean;

public class TripProblemReportBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long id;

  private long time;

  private String tripId;

  private long serviceDate;

  private String vehicleId;

  private String stopId;

  @Deprecated
  private String data;

  /**
   * A string code identifying the type of problem. There is flexibility here,
   * but for a list of known codes, see
   */
  private String code;

  private String userComment;

  private boolean userOnVehicle = false;

  private String userVehicleNumber;

  private Double userLat;

  private Double userLon;

  private Double userLocationAccuracy;

  private Double vehicleLat;

  private Double vehicleLon;

  private boolean predicted = false;

  private Double distanceAlongBlock;

  private Double scheduleDeviation;

  private EProblemReportStatus status;

  private String label;

  private TripBean trip;

  private StopBean stop;

  private StopTimeInstanceBean stopTime;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  @Deprecated
  public String getData() {
    return data;
  }

  @Deprecated
  public void setData(String data) {
    this.data = data;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getUserComment() {
    return userComment;
  }

  public void setUserComment(String userComment) {
    this.userComment = userComment;
  }

  public boolean isUserOnVehicle() {
    return userOnVehicle;
  }

  public void setUserOnVehicle(boolean userOnVehicle) {
    this.userOnVehicle = userOnVehicle;
  }

  public String getUserVehicleNumber() {
    return userVehicleNumber;
  }

  public void setUserVehicleNumber(String userVehicleNumber) {
    this.userVehicleNumber = userVehicleNumber;
  }

  public Double getUserLat() {
    return userLat;
  }

  public void setUserLat(Double userLat) {
    this.userLat = userLat;
  }

  public Double getUserLon() {
    return userLon;
  }

  public void setUserLon(Double userLon) {
    this.userLon = userLon;
  }

  public Double getUserLocationAccuracy() {
    return userLocationAccuracy;
  }

  public void setUserLocationAccuracy(Double userLocationAccuracy) {
    this.userLocationAccuracy = userLocationAccuracy;
  }

  public Double getVehicleLat() {
    return vehicleLat;
  }

  public void setVehicleLat(Double vehicleLat) {
    this.vehicleLat = vehicleLat;
  }

  public Double getVehicleLon() {
    return vehicleLon;
  }

  public void setVehicleLon(Double vehicleLon) {
    this.vehicleLon = vehicleLon;
  }

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public Double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(Double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public Double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public EProblemReportStatus getStatus() {
    return status;
  }

  public void setStatus(EProblemReportStatus status) {
    this.status = status;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public TripBean getTrip() {
    return trip;
  }

  public void setTrip(TripBean trip) {
    this.trip = trip;
  }

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public StopTimeInstanceBean getStopTime() {
    return stopTime;
  }

  public void setStopTime(StopTimeInstanceBean stopTime) {
    this.stopTime = stopTime;
  }
}
