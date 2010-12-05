package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class TripProblemReportBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long id;

  private long time;

  private String tripId;

  private long serviceDate;

  private String vehicleId;

  private String stopId;

  private String data;

  private String userComment;

  private boolean userOnVehicle;

  private String userVehicleNumber;

  private double userLat = Double.NaN;

  private double userLon = Double.NaN;

  private double userLocationAccuracy = Double.NaN;

  private double vehicleLat = Double.NaN;

  private double vehicleLon = Double.NaN;

  private boolean predicted = false;

  private double distanceAlongBlock = Double.NaN;

  private double scheduleDeviation = Double.NaN;

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
}
