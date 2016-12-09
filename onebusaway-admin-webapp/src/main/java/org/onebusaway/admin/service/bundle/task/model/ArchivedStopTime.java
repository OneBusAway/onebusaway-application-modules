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

public class ArchivedStopTime implements Serializable {

  private static final long serialVersionUID =2L;

  public static final int MISSING_VALUE = -999;

  private int id;

  private String stop_agencyId;
  
  private String stop_id;

  private String trip_agencyId;
  
  private String trip_id;

  private int arrivalTime = MISSING_VALUE;

  private int departureTime = MISSING_VALUE;
  
  private int timepoint = MISSING_VALUE;

  private int stopSequence;

  private String stopHeadsign;

  private String routeShortName;

  private int pickupType;

  private int dropOffType;

  private double shapeDistTraveled = MISSING_VALUE;

  private String farePeriodId;

  private Integer gtfsBundleInfoId;

  public ArchivedStopTime() {

  }
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getStop_agencyId() {
    return stop_agencyId;
  }

  public void setStop_agencyId(String stop_agencyId) {
    this.stop_agencyId = stop_agencyId;
  }

  public String getStop_id() {
    return stop_id;
  }

  public void setStop_id(String stop_id) {
    this.stop_id = stop_id;
  }

  public String getTrip_agencyId() {
    return trip_agencyId;
  }

  public void setTrip_agencyId(String trip_agencyId) {
    this.trip_agencyId = trip_agencyId;
  }

  public String getTrip_id() {
    return trip_id;
  }

  public void setTrip_id(String trip_id) {
    this.trip_id = trip_id;
  }

  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    this.departureTime = departureTime;
  }

  public int getTimepoint() {
    return timepoint;
  }

  public void setTimepoint(int timepoint) {
    this.timepoint = timepoint;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    this.stopSequence = stopSequence;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public void setStopHeadsign(String stopHeadsign) {
    this.stopHeadsign = stopHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public int getPickupType() {
    return pickupType;
  }

  public void setPickupType(int pickupType) {
    this.pickupType = pickupType;
  }

  public int getDropOffType() {
    return dropOffType;
  }

  public void setDropOffType(int dropOffType) {
    this.dropOffType = dropOffType;
  }

  public double getShapeDistTraveled() {
    return shapeDistTraveled;
  }

  public void setShapeDistTraveled(double shapeDistTraveled) {
    this.shapeDistTraveled = shapeDistTraveled;
  }

  public String getFarePeriodId() {
    return farePeriodId;
  }

  public void setFarePeriodId(String farePeriodId) {
    this.farePeriodId = farePeriodId;
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
    result = prime * result + id;
    result = prime * result + stop_agencyId.hashCode();
    result = prime * result + stop_id.hashCode();
    result = prime * result + trip_agencyId.hashCode();
    result = prime * result + trip_id.hashCode();
    result = prime * result + arrivalTime;
    result = prime * result + departureTime;
    result = prime * result + timepoint;
    result = prime * result + stopSequence;
    result = prime * result + stopHeadsign.hashCode();
    result = prime * result + routeShortName.hashCode();
    result = prime * result + pickupType;
    result = prime * result + dropOffType;
    long bits = Double.doubleToLongBits(shapeDistTraveled);
    int shapeDistTraveled_bits = (int)(bits ^ (bits >>> 32));
    result = prime * result + shapeDistTraveled_bits;
    result = prime * result + farePeriodId.hashCode();
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
    if (!(obj instanceof ArchivedStopTime)) {
      return false;
    }
    ArchivedStopTime other = (ArchivedStopTime) obj;
    if (gtfsBundleInfoId != other.gtfsBundleInfoId) {
      return false;
    } else if (id != other.id) {
      return false;
    } else if (!stop_agencyId.equals(other.stop_agencyId)) {
      return false;
    } else if (!stop_id.equals(other.stop_id)) {
      return false;
    } else if (!trip_agencyId.equals(other.trip_agencyId)) {
      return false;
    } else if (!trip_id.equals(other.trip_id)) {
      return false;
    } else if (arrivalTime != other.arrivalTime) {
      return false;
    } else if (departureTime != other.departureTime) {
      return false;
    } else if (timepoint != other.timepoint) {
      return false;
    } else if (stopSequence != other.stopSequence) {
      return false;
    } else if (!stopHeadsign.equals(other.stopHeadsign)) {
      return false;
    } else if (!routeShortName.equals(other.routeShortName)) {
      return false;
    } else if (pickupType != other.pickupType) {
      return false;
    } else if (dropOffType != other.dropOffType) {
      return false;
    } else if (Double.doubleToLongBits(shapeDistTraveled) != Double.doubleToLongBits(other.shapeDistTraveled)) {
      return false;
    } else if (!farePeriodId.equals(other.farePeriodId)) {
      return false;
    }

    return true;
  }

}
