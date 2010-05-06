package org.onebusaway.where.web.common.client.model;

import java.util.Date;

public class StopTimeBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private Date departureTime;

  private String serviceId;

  public Date getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(Date departureTime) {
    this.departureTime = departureTime;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String scheduleId) {
    this.serviceId = scheduleId;
  }
}
