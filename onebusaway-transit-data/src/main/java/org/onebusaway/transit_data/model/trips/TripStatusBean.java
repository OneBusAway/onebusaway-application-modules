package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.StopBean;

public final class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String status;

  private long serviceDate;

  private CoordinatePoint position;

  private double distanceAlongTrip;

  private double scheduleDeviation;

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

  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(double scheduleDeviation) {
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
