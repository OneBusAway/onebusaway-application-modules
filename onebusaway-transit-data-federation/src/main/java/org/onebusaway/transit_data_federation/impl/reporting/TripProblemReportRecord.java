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
package org.onebusaway.transit_data_federation.impl.reporting;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;

@Entity
@Table(name = "oba_trip_problem_reports")
public class TripProblemReportRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private long id;

  private long time;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "block_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "block_id"))})
  private AgencyAndId blockId;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "trip_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "trip_id"))})
  private AgencyAndId tripId;

  private long serviceDate;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "vehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "vehicle_id"))})
  private AgencyAndId vehicleId;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "stop_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "stop_id"))})
  private AgencyAndId stopId;

  @Deprecated
  private String data;

  /**
   * 
   */
  private String code;

  private String userComment;

  private boolean userOnVehicle;

  private String userVehicleNumber;

  @Column(nullable = true)
  private Double userLat;

  @Column(nullable = true)
  private Double userLon;

  @Column(nullable = true)
  private Double userLocationAccuracy;

  @Column(nullable = true)
  private Double vehicleLat;

  @Column(nullable = true)
  private Double vehicleLon;

  private boolean predicted = false;

  @Column(nullable = true)
  private Double distanceAlongBlock;

  @Column(nullable = true)
  private Double scheduleDeviation;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "matchedVehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "matchedVehicle_id"))})
  private AgencyAndId matchedVehicleId;

  /**
   * Custom Hibernate mapping so that the vehicle phase enum gets mapped to a
   * string as opposed to an integer, allowing for safe expansion of the enum in
   * the future and more legibility in the raw SQL. Additionally, the phase
   * string can be a little shorter than the default length.
   */
  @Type(type = "org.onebusaway.container.hibernate.EnumUserType", parameters = {@Parameter(name = "enumClassName", value = "org.onebusaway.transit_data.model.problems.EProblemReportStatus")})
  @Column(length = 25)
  private EProblemReportStatus status;

  private String label;

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

  public AgencyAndId getBlockId() {
    return blockId;
  }

  public void setBlockId(AgencyAndId blockId) {
    this.blockId = blockId;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  public void setStopId(AgencyAndId stopId) {
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

  public AgencyAndId getMatchedVehicleId() {
    return matchedVehicleId;
  }

  public void setMatchedVehicleId(AgencyAndId matchedVehicleId) {
    this.matchedVehicleId = matchedVehicleId;
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
}
