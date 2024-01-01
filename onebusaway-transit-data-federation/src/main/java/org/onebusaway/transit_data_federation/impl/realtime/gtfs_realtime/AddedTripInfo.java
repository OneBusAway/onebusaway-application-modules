/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Represents and added trip from GTFS-RT.
 */
public class AddedTripInfo {
  private String agencyId;
  private int tripStartTime = 0;
  private long serviceDate = -1;
  private String tripId = null;
  private AgencyAndId shapeId;
  private String routeId = null;
  private String directionId = null;
  private String vehicleId = null;
  private List<AddedStopInfo> stops = new ArrayList<>();

  private BlockDescriptor.ScheduleRelationship scheduleRelationship;

  public BlockDescriptor.ScheduleRelationship getScheduleRelationship() {
    return scheduleRelationship;
  }

  public void setScheduleRelationshipValue(String value) {
    this.scheduleRelationship = BlockDescriptor.ScheduleRelationship.valueOf(value);
  }

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  public boolean hasServiceDate() {
    return serviceDate > 0;
  }
  public int getTripStartTime() {
    return tripStartTime;
  }

  public void setTripStartTime(int tripStartTime) {
    this.tripStartTime = tripStartTime;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public void addStopTime(AddedStopInfo stopInfo) {
    stops.add(stopInfo);
  }

  public List<AddedStopInfo> getStops() {
    return stops;
  }

  public void setStops(List<AddedStopInfo> stops) {
    this.stops = stops;
  }

  public void setServiceDateFromStopTime(long time) {
    serviceDate = getStartOfDay(new Date(time)).getTime();
  }
  public static Date getStartOfDay(Date serviceDate) {
    final Calendar cal = Calendar.getInstance();
    cal.setTime(serviceDate);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 000);
    return cal.getTime();
  }
  public long getServiceDate() {
    return serviceDate;
  }
  public void setServiceDate(long time) {
    this.serviceDate = time;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
}
