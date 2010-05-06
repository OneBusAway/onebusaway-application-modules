package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class SCHRouteIden {
  
  @XStreamAlias("route-id")
  private String routeId;
  
  // Optional
  @XStreamAlias("agency-id")
  private String agencyId;
  
  // Optional
  private String routeDesignator;
  
  // Optional
  private String routeName;

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getRouteDesignator() {
    return routeDesignator;
  }

  public void setRouteDesignator(String routeDesignator) {
    this.routeDesignator = routeDesignator;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String routeName) {
    this.routeName = routeName;
  }

}
