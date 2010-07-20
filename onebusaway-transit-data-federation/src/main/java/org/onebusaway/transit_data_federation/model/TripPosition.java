package org.onebusaway.transit_data_federation.model;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

/**
 * Vehicle position information for a particular trip. The
 * {@link #getScheduleDeviation()} schedule deviation information provides
 * information about whether the position information is from real-time data or
 * from schedule data.
 * 
 * @author bdferris
 * 
 */
public class TripPosition {

  private ScheduleDeviation scheduleDeviation;

  private CoordinatePoint position;

  private StopTimeEntry closestStop;

  private int closestStopTimeOffset;

  /**
   * @return schedule deviation for the current trip at this position
   */
  public ScheduleDeviation getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(ScheduleDeviation scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
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
