package org.onebusaway.transit_data.model.tripplanner;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

public class WalkSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private EncodedPolylineBean path;

  private double distance;

  private long duration;

  public WalkSegmentBean() {

  }

  public WalkSegmentBean(long time, EncodedPolylineBean path, double distance,
      long duration) {
    super(time);
    this.path = path;
    this.distance = distance;
    this.duration = duration;
  }

  public EncodedPolylineBean getPath() {
    return path;
  }

  public void setPath(EncodedPolylineBean path) {
    this.path = path;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }
}
