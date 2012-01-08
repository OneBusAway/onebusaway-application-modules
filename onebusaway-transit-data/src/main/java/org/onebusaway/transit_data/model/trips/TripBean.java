/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.transit_data.model.RouteBean;

public final class TripBean implements Serializable {

  private static final long serialVersionUID = 3L;

  private String id;

  private RouteBean route;
  
  private String routeShortName;

  private String tripShortName;

  private String tripHeadsign;

  private String serviceId;

  private String shapeId;

  private String directionId;

  private String blockId;
  
  private double totalTripDistance;

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
    this.totalTripDistance = trip.totalTripDistance;
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

  public double getTotalTripDistance() {
    return totalTripDistance;
  }

  public void setTotalTripDistance(double totalTripDistance) {
    this.totalTripDistance = totalTripDistance;
  }
}
