package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.StopBean;

public final class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  /****
   * These are fields that we can supply from schedule data
   ****/

  private long serviceDate;

  private double scheduledDistanceAlongTrip = Double.NaN;

  private double totalDistanceAlongTrip = Double.NaN;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private String status;

  private CoordinatePoint location;

  private StopBean closestStop;

  private int closestStopTimeOffset;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private boolean predicted = false;

  private long lastUpdateTime;

  private double scheduleDeviation;

  private double distanceAlongTrip = Double.NaN;

  private String vehicleId;

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  /**
   * The vehicle's scheduled distance along the trip.
   * 
   * @return distance, in meters
   */
  public double getScheduledDistanceAlongTrip() {
    return scheduledDistanceAlongTrip;
  }

  public void setScheduledDistanceAlongTrip(double scheduledDistanceAlongTrip) {
    this.scheduledDistanceAlongTrip = scheduledDistanceAlongTrip;
  }

  public double getTotalDistanceAlongTrip() {
    return totalDistanceAlongTrip;
  }

  public void setTotalDistanceAlongTrip(double totalDistanceAlongTrip) {
    this.totalDistanceAlongTrip = totalDistanceAlongTrip;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public StopBean getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(StopBean closestStop) {
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

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public void setLastUpdateTime(long time) {
    this.lastUpdateTime = time;
  }

  /**
   * @return the time we last heard from the bus (Unix-time)
   */
  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  /**
   * @return true if {@link #getDistanceAlongTrip()} has been set
   */
  public boolean isDistanceAlongTripSet() {
    return !Double.isNaN(distanceAlongTrip);
  }

  /**
   * See description in {@link #getDistanceAlongTrip()}.
   * 
   * @param distanceAlongTrip
   */
  public void setDistanceAlongTrip(double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  /**
   * The vehicle's distance along the trip.
   * 
   * @return distance, in meters
   */
  public double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
}
