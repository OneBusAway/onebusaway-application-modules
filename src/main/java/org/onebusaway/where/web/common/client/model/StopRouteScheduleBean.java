package org.onebusaway.where.web.common.client.model;

import java.util.ArrayList;
import java.util.List;

public class StopRouteScheduleBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private String name;

  private String description;

  private List<StopTimeBean> stopTimes = new ArrayList<StopTimeBean>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<StopTimeBean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<StopTimeBean> stopTimes) {
    this.stopTimes = stopTimes;
  }
}
