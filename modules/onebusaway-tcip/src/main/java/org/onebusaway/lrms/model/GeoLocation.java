package org.onebusaway.lrms.model;

public class GeoLocation {
  private double latitude;
  private double longitude;
  private String horizontalDatum;

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public String getHorizontalDatum() {
    return horizontalDatum;
  }

  public void setHorizontalDatum(String horizontalDatum) {
    this.horizontalDatum = horizontalDatum;
  }

}
