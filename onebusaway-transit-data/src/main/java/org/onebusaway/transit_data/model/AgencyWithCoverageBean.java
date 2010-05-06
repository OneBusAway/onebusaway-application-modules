package org.onebusaway.transit_data.model;

import java.io.Serializable;

public class AgencyWithCoverageBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyBean agency;

  private double lat;

  private double lon;

  private double latSpan;
  
  private double lonSpan;
  
  public AgencyBean getAgency() {
    return agency;
  }

  public void setAgency(AgencyBean agency) {
    this.agency = agency;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getLatSpan() {
    return latSpan;
  }

  public void setLatSpan(double latSpan) {
    this.latSpan = latSpan;
  }

  public double getLonSpan() {
    return lonSpan;
  }

  public void setLonSpan(double lonSpan) {
    this.lonSpan = lonSpan;
  }
}
