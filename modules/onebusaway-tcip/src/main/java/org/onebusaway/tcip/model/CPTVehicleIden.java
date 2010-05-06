package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class CPTVehicleIden {
  
  @XStreamAlias("vehicle-id")
  private String vehicleId;
  
  @XStreamAlias("agency-id")
  private String agencyId;
  
  private String vin;

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getVin() {
    return vin;
  }

  public void setVin(String vin) {
    this.vin = vin;
  }

}
