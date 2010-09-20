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

import org.onebusaway.gtfs.model.AgencyAndId;

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

  private String data;

  private String userComment;

  private boolean userOnVehicle;

  private String userVehicleNumber;

  private double userLat;

  private double userLon;

  private double userLocationAccuracy;

  private double vehicleLat;

  private double vehicleLon;

  private boolean predicted = false;

  private double distanceAlongBlock;

  private double scheduleDeviation;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "matchedVehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "matchedVehicle_id"))})
  private AgencyAndId matchedVehicleId;

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

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
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

  public double getUserLat() {
    return userLat;
  }

  public void setUserLat(double userLat) {
    this.userLat = userLat;
  }

  public double getUserLon() {
    return userLon;
  }

  public void setUserLon(double userLon) {
    this.userLon = userLon;
  }

  public double getUserLocationAccuracy() {
    return userLocationAccuracy;
  }

  public void setUserLocationAccuracy(double userLocationAccuracy) {
    this.userLocationAccuracy = userLocationAccuracy;
  }

  public double getVehicleLat() {
    return vehicleLat;
  }

  public void setVehicleLat(double vehicleLat) {
    this.vehicleLat = vehicleLat;
  }

  public double getVehicleLon() {
    return vehicleLon;
  }

  public void setVehicleLon(double vehicleLon) {
    this.vehicleLon = vehicleLon;
  }

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public AgencyAndId getMatchedVehicleId() {
    return matchedVehicleId;
  }

  public void setMatchedVehicleId(AgencyAndId matchedVehicleId) {
    this.matchedVehicleId = matchedVehicleId;
  }
}
