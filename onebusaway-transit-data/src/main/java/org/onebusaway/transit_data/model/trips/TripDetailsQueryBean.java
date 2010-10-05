package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripDetailsQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String tripId;
  
  private long serviceDate;
  
  private String vehicleId;
  
  private long time;
  
  private TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();
  
  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public TripDetailsInclusionBean getInclusion() {
    return inclusion;
  }

  public void setInclusion(TripDetailsInclusionBean inclusion) {
    this.inclusion = inclusion;
  }
}
