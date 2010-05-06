package org.onebusaway.gtfs.model;

public final class Trip extends IdentityBean<AgencyAndId> {

  private static final long serialVersionUID = 1L;

  private AgencyAndId id;

  private Route route;

  private AgencyAndId serviceId;

  private String tripShortName;

  private String tripHeadsign;

  private String routeShortName;

  private String directionId;

  private String blockId;

  private int blockSequenceId = -1;

  private AgencyAndId shapeId;

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  public String getTripShortName() {
    return tripShortName;
  }

  public void setTripShortName(String tripShortName) {
    this.tripShortName = tripShortName;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public int getBlockSequenceId() {
    return blockSequenceId;
  }

  public void setBlockSequenceId(int blockSequenceId) {
    this.blockSequenceId = blockSequenceId;
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }
 
  public String toString() {
      return "<Trip "+getId()+">";
  }
}
