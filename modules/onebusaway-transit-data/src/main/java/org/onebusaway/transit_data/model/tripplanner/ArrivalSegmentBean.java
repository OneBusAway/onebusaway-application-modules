package org.onebusaway.transit_data.model.tripplanner;

import org.onebusaway.transit_data.model.StopBean;

public class ArrivalSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  public ArrivalSegmentBean() {

  }

  public ArrivalSegmentBean(long time, StopBean stop) {
    super(time);
    this.stop = stop;
  }

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }
}
