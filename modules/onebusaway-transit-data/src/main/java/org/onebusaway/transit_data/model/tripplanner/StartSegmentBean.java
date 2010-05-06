package org.onebusaway.transit_data.model.tripplanner;

public class StartSegmentBean extends LocationSegmentBean {

  private static final long serialVersionUID = 1L;

  public StartSegmentBean() {

  }

  public StartSegmentBean(long time, double lat, double lon) {
    super(time, lat, lon);
  }

}
