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
package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

/**
 * Vehicle location information for a particular trip.
 * 
 * @author bdferris
 */
public class TripLocation {

  /****
   * These are fields that we can supply from schedule data
   ****/

  private TripEntry trip;

  private long serviceDate;

  private boolean inService;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private CoordinatePoint location;

  private double distanceAlongRoute = Double.NaN;

  private StopTimeEntry closestStop;

  private int closestStopTimeOffset;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private boolean predicted;

  private long lastUpdateTime;

  private CoordinatePoint lastKnownLocation;

  private double scheduleDeviation = Double.NaN;

  private AgencyAndId vehicleId;

  public TripLocation() {

  }

  public TripEntry getTrip() {
    return trip;
  }

  public void setTrip(TripEntry trip) {
    this.trip = trip;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  /**
   * Note that a trip may be considered in service even if the vehicle isn't
   * actively serving the trip segment if the parent block is in service.
   * 
   * @return true if the trip is actively in service
   */
  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }

  /**
   * @return the trip location
   */
  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  /**
   * @return true if {@link #getDistanceAlongTrip()} is set
   */
  public boolean hasDistanceAlongTrip() {
    return !Double.isNaN(distanceAlongRoute);
  }

  /**
   * Note that this value could potentially be negative or larger than the total
   * length of the trip's shape if we are tracking its location along a block
   * before or after the trip. If this value is not set, the value will be
   * {@link Double#NaN}.
   * 
   * @return the distance traveled along the shape of the route, in meters
   */
  public double getDistanceAlongTrip() {
    return distanceAlongRoute;
  }

  public void setDistanceAlongRoute(double distanceAlongRoute) {
    this.distanceAlongRoute = distanceAlongRoute;
  }

  /**
   * The closest stop to the current position of the transit vehicle among the
   * stop times of the current trip.
   * 
   * @return the closest stop time entry
   */
  public StopTimeEntry getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(StopTimeEntry closestStop) {
    this.closestStop = closestStop;
  }

  /**
   * The time offset, in seconds, from the closest stop to the current position
   * of the transit vehicle among the stop times of the current trip. If the
   * number is positive, the stop is coming up. If negative, the stop has
   * already been passed.
   * 
   * @return time, in seconds
   */
  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  /**
   * See description in {@link #getClosestStopTimeOffset()}.
   * 
   * @param closestStopTimeOffset the time offset from the closest stop, in
   *          seconds
   */
  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }

  /**
   * If real-time data is available in any form for this vehicle
   * 
   * @return true if real-time is available
   */
  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  /**
   * @return the time we last heard from the bus (Unix-time)
   */
  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long time) {
    this.lastUpdateTime = time;
  }

  public CoordinatePoint getLastKnownLocation() {
    return lastKnownLocation;
  }

  public void setLastKnownLocation(CoordinatePoint lastKnownLocation) {
    this.lastKnownLocation = lastKnownLocation;
  }

  /**
   * @return true if {@link #getScheduleDeviation()} is set
   */
  public boolean hasScheduleDeviation() {
    return !Double.isNaN(scheduleDeviation);
  }

  /**
   * If schedule deviation information is not available, this value will be
   * {@link Double#NaN}.
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * @param scheduleDeviation schedule deviation, in seconds, (+deviation is
   *          late, -deviation is early)
   */
  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

}
