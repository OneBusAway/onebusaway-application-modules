package org.onebusaway.tripplanner.web.common.client.model;

import org.onebusaway.common.web.common.client.model.PathBean;

public class WalkSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private PathBean path;

  private double length;

  public WalkSegmentBean() {

  }

  public WalkSegmentBean(long time, PathBean path, double length) {
    super(time);
    this.path = path;
    this.length = length;
  }

  public PathBean getPath() {
    return path;
  }

  public void setPath(PathBean path) {
    this.path = path;
  }

  public double getLength() {
    return length;
  }

  public void setLength(double length) {
    this.length = length;
  }
}
