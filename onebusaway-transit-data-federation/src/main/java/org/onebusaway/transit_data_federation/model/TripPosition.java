package org.onebusaway.transit_data_federation.model;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

/**
 * Vehicle position information for a particular trip.
 * 
 * @author bdferris
 */
public class TripPosition {

  private AgencyAndId tripId;

  private long serviceDate;

  private boolean predicted;

  private CoordinatePoint position;

  private int scheduleDeviation;

  private AgencyAndId vehicleId;

  private StopTimeEntry closestStop;

  private int closestStopTimeOffset;

  public TripPosition() {

  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  /**
   * If real-time data is not avaialble, schedule deviation will be zero
   * 
   * @return true if the schedule deviation data is from real-time data
   */
  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  /**
   * @return the trip position
   */
  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }

  /**
   * If {@link #isPredicted()} is false, indicating no real-time data is
   * available, schedule deviation will be zero.
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation schedule deviation, in seconds, (+deviation is
   *          late, -deviation is early)
   */
  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
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
}
