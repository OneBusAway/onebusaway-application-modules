package org.onebusaway.tripplanner.web.common.client.model;

import org.onebusaway.common.web.common.client.model.PathBean;

public class RideSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private PathBean path;

  public RideSegmentBean() {

  }

  public RideSegmentBean(long time, PathBean path) {
    super(time);
    this.path = path;
  }

  public PathBean getPath() {
    return path;
  }

  public void setPath(PathBean path) {
    this.path = path;
  }
}
