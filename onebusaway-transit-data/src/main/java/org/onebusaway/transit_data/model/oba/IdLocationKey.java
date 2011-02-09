package org.onebusaway.transit_data.model.oba;

public class IdLocationKey {
  private String id;
  private double lat;
  private double lon;

  public IdLocationKey() {

  }

  public IdLocationKey(String id, double lat, double lon) {
    this.id = id;
    this.lat = lat;
    this.lon = lon;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

}
