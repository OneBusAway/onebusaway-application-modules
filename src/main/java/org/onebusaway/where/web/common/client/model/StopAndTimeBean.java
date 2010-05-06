package org.onebusaway.where.web.common.client.model;

import org.onebusaway.common.web.common.client.model.StopBean;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class StopAndTimeBean implements Serializable, IsSerializable {

  private static final long serialVersionUID = 1L;

  private StopBean stop;

  private StopTimeBean stopTime;

  public StopBean getStop() {
    return stop;
  }

  public void setStop(StopBean stop) {
    this.stop = stop;
  }

  public StopTimeBean getStopTime() {
    return stopTime;
  }

  public void setStopTime(StopTimeBean stopTime) {
    this.stopTime = stopTime;
  }
}
