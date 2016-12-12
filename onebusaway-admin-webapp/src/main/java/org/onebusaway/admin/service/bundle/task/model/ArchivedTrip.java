/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task.model;

import java.io.Serializable;

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.TripAgencyIdFieldMappingFactory;

public class ArchivedTrip implements Serializable {
  private static final long serialVersionUID = 2L;

  private String agencyId;
  
  private String id;

  private String route_agencyId;
  
  private String route_id;

  private String serviceId_agencyId;
  
  private String serviceId_id;

  private String tripShortName;

  private String tripHeadsign;

  private String routeShortName;

  private String directionId;

  private String blockId;

  private String shapeId_agencyId;
  
  private String shapeId_id;

  private int wheelchairAccessible = 0;

  private Integer gtfsBundleInfoId;

  //@Deprecated
  //private int tripBikesAllowed = 0;

  /**
   * 0 = unknown / unspecified, 1 = bikes allowed, 2 = bikes NOT allowed
   */
  //private int bikesAllowed = 0;

  // Custom extension for KCM to specify a fare per-trip
  //private String fareId;
  
  public ArchivedTrip() {

  }


  public String getAgencyId() {
    return agencyId;
  }


  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }


  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }


  public String getRoute_agencyId() {
    return route_agencyId;
  }


  public void setRoute_agencyId(String route_agencyId) {
    this.route_agencyId = route_agencyId;
  }


  public String getRoute_id() {
    return route_id;
  }


  public void setRoute_id(String route_id) {
    this.route_id = route_id;
  }


  public String getServiceId_agencyId() {
    return serviceId_agencyId;
  }


  public void setServiceId_agencyId(String serviceId_agencyId) {
    this.serviceId_agencyId = serviceId_agencyId;
  }


  public String getServiceId_id() {
    return serviceId_id;
  }


  public void setServiceId_id(String serviceId_id) {
    this.serviceId_id = serviceId_id;
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


  public String getShapeId_agencyId() {
    return shapeId_agencyId;
  }


  public void setShapeId_agencyId(String shapeId_agencyId) {
    this.shapeId_agencyId = shapeId_agencyId;
  }


  public String getShapeId_id() {
    return shapeId_id;
  }


  public void setShapeId_id(String shapeId_id) {
    this.shapeId_id = shapeId_id;
  }


  public int getWheelchairAccessible() {
    return wheelchairAccessible;
  }


  public void setWheelchairAccessible(int wheelchairAccessible) {
    this.wheelchairAccessible = wheelchairAccessible;
  }


  public Integer getGtfsBundleInfoId() {
    return gtfsBundleInfoId;
  }


  public void setGtfsBundleInfoId(Integer gtfsBundleInfoId) {
    this.gtfsBundleInfoId = gtfsBundleInfoId;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + agencyId.hashCode();
    result = prime * result + id.hashCode();
    result = prime * result + route_agencyId.hashCode();
    result = prime * result + route_id.hashCode();
    result = prime * result + serviceId_agencyId.hashCode();
    result = prime * result + serviceId_id.hashCode();
    result = prime * result + tripShortName.hashCode();
    result = prime * result + tripHeadsign.hashCode();
    result = prime * result + routeShortName.hashCode();
    result = prime * result + directionId.hashCode();
    result = prime * result + blockId.hashCode();
    result = prime * result + shapeId_agencyId.hashCode();
    result = prime * result + shapeId_id.hashCode();
    result = prime * result + wheelchairAccessible;
    result = prime * result + gtfsBundleInfoId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ArchivedTrip)) {
      return false;
    }
    ArchivedTrip other = (ArchivedTrip) obj;
    if (gtfsBundleInfoId != other.gtfsBundleInfoId) {
      return false;
    } else if (!agencyId.equals(other.agencyId)) {
      return false;
    } else if (!id.equals(other.id)) {
      return false;
    } else if (!route_agencyId.equals(other.route_agencyId)) {
      return false;
    } else if (!route_id.equals(other.route_id)) {
      return false;
    } else if (!serviceId_agencyId.equals(other.serviceId_agencyId)) {
      return false;
    } else if (!serviceId_id.equals(other.serviceId_id)) {
      return false;
    } else if (!tripShortName.equals(other.tripShortName)) {
      return false;
    } else if (!tripHeadsign.equals(other.tripHeadsign)) {
      return false;
    } else if (!routeShortName.equals(other.routeShortName)) {
      return false;
    } else if (!directionId.equals(other.directionId)) {
      return false;
    } else if (!blockId.equals(other.blockId)) {
      return false;
    } else if (!shapeId_agencyId.equals(other.shapeId_agencyId)) {
      return false;
    } else if (!shapeId_id.equals(other.shapeId_id)) {
      return false;
    } else if (wheelchairAccessible != other.wheelchairAccessible) {
      return false;
    }

    return true;
  }
}
