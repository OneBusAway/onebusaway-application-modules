package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;
import java.util.Date;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripDetailsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String tripId;
  
  private Date serviceDate;
  
  private Date time;
  
  private TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();
  
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

  public TripDetailsInclusionBean getInclusion() {
    return inclusion;
  }

  public void setInclusion(TripDetailsInclusionBean inclusion) {
    this.inclusion = inclusion;
  }
}
