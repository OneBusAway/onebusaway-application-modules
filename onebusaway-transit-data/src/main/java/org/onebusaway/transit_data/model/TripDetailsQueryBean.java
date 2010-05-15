package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.Date;

@QueryBean
public final class TripDetailsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String tripId;
  
  private Date serviceDate;
  
  private Date time;
  
  private boolean includeTripBean = true;

  private boolean includeTripSchedule = true;
  
  private boolean includeTripStatus = true;

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public Date getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(Date serviceDate) {
    this.serviceDate = serviceDate;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public boolean isIncludeTripBean() {
    return includeTripBean;
  }

  public void setIncludeTripBean(boolean includeTripBeans) {
    this.includeTripBean = includeTripBeans;
  }

  public boolean isIncludeTripSchedule() {
    return includeTripSchedule;
  }

  public void setIncludeTripSchedule(boolean includeTripSchedule) {
    this.includeTripSchedule = includeTripSchedule;
  }

  public boolean isIncludeTripStatus() {
    return includeTripStatus;
  }

  public void setIncludeTripStatus(boolean includeTripStatus) {
    this.includeTripStatus = includeTripStatus;
  }
}
