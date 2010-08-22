package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.transit_data.model.RouteBean;

public final class TripBean implements Serializable {

  private static final long serialVersionUID = 2L;

  private String id;

  private RouteBean route;
  
  private String routeShortName;

  private String tripShortName;

  private String tripHeadsign;

  private String serviceId;

  private String shapeId;

  private String directionId;

  private String blockId;

  public TripBean() {

  }

  public TripBean(TripBean trip) {
    this.id = trip.id;
    this.route = trip.route;
    this.routeShortName = trip.routeShortName;
    this.tripShortName = trip.tripShortName;
    this.tripHeadsign = trip.tripHeadsign;
    this.serviceId = trip.serviceId;
    this.shapeId = trip.shapeId;
    this.directionId = trip.directionId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
  }
  
  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
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

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getShapeId() {
    return shapeId;
  }

  public void setShapeId(String shapeId) {
    this.shapeId = shapeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }
  
  public String getBlockId() {
    return blockId;
  }
}
