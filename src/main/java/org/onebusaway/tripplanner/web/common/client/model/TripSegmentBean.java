package org.onebusaway.tripplanner.web.common.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.Date;

public class TripSegmentBean implements Serializable, IsSerializable {

  private static final long serialVersionUID = 1L;

  private Date time;

  public TripSegmentBean() {

  }

  public TripSegmentBean(long time) {
    this.time = new Date(time);
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }
}
