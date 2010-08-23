package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class ReportProblemWithTripBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time;

  private String tripId;

  private long serviceDate;

  private String stopId;

  private String data;

  private String userComment;

  private boolean userOnVehicle;

  private String userVehicleNumber;

  private double userLat;

  private double userLon;

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
}
