package org.onebusaway.transit_data.model.tripplanner;

import org.onebusaway.geospatial.model.EncodedPolylineBean;

public class RideSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private EncodedPolylineBean path;

  public RideSegmentBean() {

  }

  public RideSegmentBean(long time, EncodedPolylineBean path) {
    super(time);
    this.path = path;
  }

  public EncodedPolylineBean getPath() {
    return path;
  }

  public void setPath(EncodedPolylineBean path) {
    this.path = path;
  }
}
