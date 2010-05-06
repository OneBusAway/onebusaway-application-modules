package org.onebusaway.transit_data.model.oba;

import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;

import java.io.Serializable;

public class OneBusAwayConstraintsBean extends TripPlannerConstraintsBean
    implements Serializable {

  private static final long serialVersionUID = 1L;

  private int minDepartureTimeOfDay = -1;

  private int maxDepartureTimeOfDay = -1;

  public OneBusAwayConstraintsBean() {

  }

  public OneBusAwayConstraintsBean(OneBusAwayConstraintsBean constraints) {
    super(constraints);
    minDepartureTimeOfDay = constraints.minDepartureTimeOfDay;
    maxDepartureTimeOfDay = constraints.maxDepartureTimeOfDay;
  }

  public boolean hasMinDepartureTimeOfDay() {
    return minDepartureTimeOfDay != -1;
  }

  /**
   * 
   * @return time of day int seconds
   */
  public int getMinDepartureTimeOfDay() {
    return minDepartureTimeOfDay;
  }

  /**
   * 
   * @param minDepartureTimeOfDay time of day in seconds
   */
  public void setMinDepartureTimeOfDay(int minDepartureTimeOfDay) {
    this.minDepartureTimeOfDay = minDepartureTimeOfDay;
  }

  public boolean hasMaxDepartureTimeOfDay() {
    return maxDepartureTimeOfDay != -1;
  }

  /**
   * 
   * @return time of day in seconds
   */
  public int getMaxDepartureTimeOfDay() {
    return maxDepartureTimeOfDay;
  }

  /**
   * 
   * @param maxDepartureTimeOfDay time of day in seconds
   */
  public void setMaxDepartureTimeOfDay(int maxDepartureTimeOfDay) {
    this.maxDepartureTimeOfDay = maxDepartureTimeOfDay;
  }
}