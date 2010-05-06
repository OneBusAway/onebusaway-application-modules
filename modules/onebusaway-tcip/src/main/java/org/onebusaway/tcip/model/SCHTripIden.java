package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class SCHTripIden {
  
  @XStreamAlias("trip-id")
  private String tripId;
  
  @XStreamAlias("agency-id")
  private String agencyId;
  
  private String designator;
  
  public SCHTripIden() {
    
  }
  
  public SCHTripIden(String agencyId, String tripId) {
    this.agencyId = agencyId;
    this.tripId = tripId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getDesignator() {
    return designator;
  }

  public void setDesignator(String designator) {
    this.designator = designator;
  }

}
