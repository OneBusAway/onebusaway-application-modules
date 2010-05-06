package org.onebusaway.tripplanner.web.common.client.model;

public class StartSegmentBean extends LocationSegmentBean {

  private static final long serialVersionUID = 1L;

  public StartSegmentBean() {

  }

  public StartSegmentBean(long time, double lat, double lon) {
    super(time, lat, lon);
  }

}
