package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.StopBean;

public final class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private String status;

  private long serviceDate;

  private CoordinatePoint position;
  
  private double distanceAlongRoute;

  private int scheduleDeviation;

  private boolean predicted = false;
  
  private String vehicleId;
  
  private StopBean closestStop;
  
  private int closestStopTimeOffset;

  private long time;
  
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
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

  /**
   * See description in {@link #getDistanceAlongRoute()}.
   * @param distanceAlongRoute
   */
  public void setDistanceAlongRoute(double distanceAlongRoute) {
    this.distanceAlongRoute = distanceAlongRoute;
  }

  /**
   * The vehicle's distance along the route.
   * @return distance, in meters
   */
  public double getDistanceAlongRoute() {
    return distanceAlongRoute;
  }

  public void setTime(long time) {
    this.time = time;
  }

  /**
   * @return the time we last heard from the bus (Unix-time)
   */
  public long getTime() {
    return time;
  }
}
