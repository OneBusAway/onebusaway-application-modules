package org.onebusaway.tripplanner.web.common.client.model;

import org.onebusaway.common.web.common.client.model.StopBean;

import java.util.Date;

public class ArrivalSegmentBean extends TripSegmentBean {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private Date time;

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

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }
}
