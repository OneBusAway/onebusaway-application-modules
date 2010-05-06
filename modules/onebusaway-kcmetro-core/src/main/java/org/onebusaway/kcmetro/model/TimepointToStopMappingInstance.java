package org.onebusaway.kcmetro.model;

import java.util.Date;

public class TimepointToStopMappingInstance {

  private TimepointToStopMapping mapping;

  private Date serviceDate;

  public TimepointToStopMappingInstance(TimepointToStopMapping mapping,
      Date serviceDate) {
    this.mapping = mapping;
    this.serviceDate = serviceDate;
  }

  public TimepointToStopMapping getMapping() {
    return mapping;
  }

  public void setMapping(TimepointToStopMapping mapping) {
    this.mapping = mapping;
  }

  public Date getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(Date serviceDate) {
    this.serviceDate = serviceDate;
  }
}
