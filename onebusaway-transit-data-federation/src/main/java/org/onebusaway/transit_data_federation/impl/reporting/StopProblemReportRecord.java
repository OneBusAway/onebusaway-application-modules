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
@Table(name = "oba_stop_problem_reports")
public class StopProblemReportRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private long id;

  private long time;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "stopId_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "stopId_id"))})
  private AgencyAndId stopId;

  private String data;

  private String userComment;
  
  @Column(nullable = true)
  private Double userLat;

  @Column(nullable = true)
  private Double userLon;

  @Column(nullable = true)
  private Double userLocationAccuracy;

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
}
