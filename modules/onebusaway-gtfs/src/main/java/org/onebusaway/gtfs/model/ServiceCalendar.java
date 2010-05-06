package org.onebusaway.gtfs.model;

import java.util.Date;

/**
 * Note that I decided to call this class ServiceCalendar instead of Calendar,
 * so as to avoid confusion with java.util.Calendar
 * 
 * @author bdferris
 * 
 */
public final class ServiceCalendar extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private int id;

  private AgencyAndId serviceId;

  private int monday;

  private int tuesday;

  private int wednesday;

  private int thursday;

  private int friday;

  private int saturday;

  private int sunday;

  private Date startDate;

  private Date endDate;

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

  public int getMonday() {
    return monday;
  }

  public void setMonday(int monday) {
    this.monday = monday;
  }

  public int getTuesday() {
    return tuesday;
  }

  public void setTuesday(int tuesday) {
    this.tuesday = tuesday;
  }

  public int getWednesday() {
    return wednesday;
  }

  public void setWednesday(int wednesday) {
    this.wednesday = wednesday;
  }

  public int getThursday() {
    return thursday;
  }

  public void setThursday(int thursday) {
    this.thursday = thursday;
  }

  public int getFriday() {
    return friday;
  }

  public void setFriday(int friday) {
    this.friday = friday;
  }

  public int getSaturday() {
    return saturday;
  }

  public void setSaturday(int saturday) {
    this.saturday = saturday;
  }

  public int getSunday() {
    return sunday;
  }

  public void setSunday(int sunday) {
    this.sunday = sunday;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String toString() {
    return "<ServiceCalendar " + this.serviceId + " [" + this.monday
        + this.tuesday + this.wednesday + this.thursday + this.friday
        + this.saturday + this.sunday + "]>";
  }
}
