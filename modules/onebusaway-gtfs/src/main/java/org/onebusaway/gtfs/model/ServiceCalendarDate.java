package org.onebusaway.gtfs.model;

import java.util.Date;

public final class ServiceCalendarDate extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  public static final int EXCEPTION_TYPE_ADD = 1;

  public static final int EXCEPTION_TYPE_REMOVE = 2;

  private int id;

  private AgencyAndId serviceId;

  private Date date;

  private int exceptionType;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public int getExceptionType() {
    return exceptionType;
  }

  public void setExceptionType(int exceptionType) {
    this.exceptionType = exceptionType;
  }

  @Override
  public String toString() {
    return "<CalendarDate serviceId=" + this.serviceId + " date=" + this.date
        + " exception=" + this.exceptionType + ">";
  }
}
