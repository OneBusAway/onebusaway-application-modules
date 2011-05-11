package org.onebusaway.transit_data.model.problems;

import java.io.Serializable;

public class PlannedTripProblemReportBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long id;

  private long reportTime = System.currentTimeMillis();

  private String data;

  private String userComment;

  private Double userLat;

  private Double userLon;

  private Double userLocationAccuracy;

  private EProblemReportStatus status;

  private String label;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getReportTime() {
    return reportTime;
  }

  public void setReportTime(long reportTime) {
    this.reportTime = reportTime;
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
