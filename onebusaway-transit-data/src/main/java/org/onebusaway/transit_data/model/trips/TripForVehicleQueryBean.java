package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;
import java.util.Date;

import org.onebusaway.transit_data.model.QueryBean;

@QueryBean
public final class TripForVehicleQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String vehicleId;

  private Date time;

  private TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
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
