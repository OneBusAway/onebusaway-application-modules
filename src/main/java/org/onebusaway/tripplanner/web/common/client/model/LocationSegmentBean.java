package org.onebusaway.tripplanner.web.common.client.model;

public class LocationSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private double lat;

  private double lon;

  public LocationSegmentBean() {

  }

  public LocationSegmentBean(long time, double lat, double lon) {
    super(time);
    this.lat = lat;
    this.lon = lon;
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
