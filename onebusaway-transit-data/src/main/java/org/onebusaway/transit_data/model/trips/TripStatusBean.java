package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;

public final class TripStatusBean implements Serializable {

  private static final long serialVersionUID = 1L;

  /****
   * These are fields that we can supply from schedule data
   ****/

  private TripBean activeTrip;

  private long serviceDate;

  private double scheduledDistanceAlongTrip = Double.NaN;

  private double totalDistanceAlongTrip = Double.NaN;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private String phase;

  private String status;

  private CoordinatePoint location;

  private double orientation = Double.NaN;

  private StopBean closestStop;

  private int closestStopTimeOffset;

  private StopBean nextStop;

  private int nextStopTimeOffset;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private boolean predicted = false;

  private long lastUpdateTime;

  private CoordinatePoint lastKnownLocation;

  private double lastKnownOrientation = Double.NaN;

  private double scheduleDeviation;

  private double distanceAlongTrip = Double.NaN;

  private String vehicleId;

  private List<SituationBean> situations;

  private double nextStopDistanceAlongTrip;

  public TripBean getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(TripBean activeTrip) {
    this.activeTrip = activeTrip;
  }

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

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
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

  public boolean isOrientationSet() {
    return !Double.isNaN(orientation);
  }

  public double getOrientation() {
    return orientation;
  }

  public void setOrientation(double orientation) {
    this.orientation = orientation;
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

  public StopBean getNextStop() {
    return nextStop;
  }

  public void setNextStop(StopBean nextStop) {
    this.nextStop = nextStop;
  }

  /**
   * The time offset, in seconds, from the next stop to the current position of
   * the transit vehicle according to the schedule.
   * 
   * @return time, in seconds
   */
  public int getNextStopTimeOffset() {
    return nextStopTimeOffset;
  }

  public void setNextStopTimeOffset(int nextStopTimeOffset) {
    this.nextStopTimeOffset = nextStopTimeOffset;
  }

  /**
   * @return true if there is an real-time data for this trip, whether
   *         prediction or location
   */
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

  public CoordinatePoint getLastKnownLocation() {
    return lastKnownLocation;
  }

  public void setLastKnownLocation(CoordinatePoint lastKnownLocation) {
    this.lastKnownLocation = lastKnownLocation;
  }

  public boolean isLastKnownOrientationSet() {
    return !Double.isNaN(lastKnownOrientation);
  }

  public double getLastKnownOrientation() {
    return lastKnownOrientation;
  }

  public void setLastKnownOrientation(double lastKnownOrientation) {
    this.lastKnownOrientation = lastKnownOrientation;
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

  public List<SituationBean> getSituations() {
    return situations;
  }

  public void setSituations(List<SituationBean> situations) {
    this.situations = situations;
  }

  public double getNextStopDistanceAlongTrip() {
    return nextStopDistanceAlongTrip;
  }

  public void setNextStopDistanceAlongTrip(double d) {
    this.nextStopDistanceAlongTrip = d;
  }
}
