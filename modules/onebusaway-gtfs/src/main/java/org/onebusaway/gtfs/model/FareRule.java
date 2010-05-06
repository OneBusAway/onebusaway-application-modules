package org.onebusaway.gtfs.model;

public final class FareRule extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  private int id;

  private FareAttribute fare;

  private Route route;

  private String originId;

  private String destinationId;

  private String containsId;

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public FareAttribute getFare() {
    return fare;
  }

  public void setFare(FareAttribute fare) {
    this.fare = fare;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public String getOriginId() {
    return originId;
  }

  public void setOriginId(String originId) {
    this.originId = originId;
  }

  public String getDestinationId() {
    return destinationId;
  }

  public void setDestinationId(String destinationId) {
    this.destinationId = destinationId;
  }

  public String getContainsId() {
    return containsId;
  }

  public void setContainsId(String containsId) {
    this.containsId = containsId;
  }

  public String toString() {
    return "<FareRule " + getId() + ">";
  }
}
