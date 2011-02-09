package org.onebusaway.transit_data.model.tripplanner;

public class EndSegmentBean extends LocationSegmentBean {

  private static final long serialVersionUID = 1L;

  public EndSegmentBean() {

  }

  public EndSegmentBean(long time, double lat, double lon) {
    super(time, lat, lon);
  }
}
