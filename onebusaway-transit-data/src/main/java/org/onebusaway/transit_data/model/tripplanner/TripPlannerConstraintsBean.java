package org.onebusaway.transit_data.model.tripplanner;

import java.io.Serializable;

public class TripPlannerConstraintsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long minDepartureTime = -1;

  private long maxDepartureTime = -1;

  private int maxTripDuration = -1;

  private int maxTransfers = -1;

  private double maxWalkingDistance = -1;

  private boolean walkingOnly = false;

  public TripPlannerConstraintsBean() {

  }

  public TripPlannerConstraintsBean(TripPlannerConstraintsBean constraints) {
    minDepartureTime = constraints.minDepartureTime;
    maxDepartureTime = constraints.maxDepartureTime;
    maxTripDuration = constraints.maxTripDuration;
    maxTransfers = constraints.maxTransfers;
    maxWalkingDistance = constraints.maxWalkingDistance;
    walkingOnly = constraints.walkingOnly;
  }

  public boolean hasMinDepartureTime() {
    return minDepartureTime != -1;
  }

  public long getMinDepartureTime() {
    return minDepartureTime;
  }

  public void setMinDepartureTime(long minStartTime) {
    this.minDepartureTime = minStartTime;
  }

  public boolean hasMaxDepartureTime() {
    return maxDepartureTime != -1;
  }

  public long getMaxDepartureTime() {
    return maxDepartureTime;
  }

  public void setMaxDepartureTime(long maxDepartureTime) {
    this.maxDepartureTime = maxDepartureTime;
  }

  public boolean hasMaxTripDuration() {
    return maxTripDuration != -1;
  }

  /**
   * 
   * @return duration, in minutes, of the max trip length
   */
  public int getMaxTripDuration() {
    return maxTripDuration;
  }

  /**
   * 
   * @param maxTripLength duration, in minutes, of the max trip length
   */
  public void setMaxTripDuration(int maxTripLength) {
    this.maxTripDuration = maxTripLength;
  }

  public boolean hasMaxTransfers() {
    return maxTransfers != -1;
  }

  public int getMaxTransfers() {
    return maxTransfers;
  }

  public void setMaxTransfers(int maxTransfers) {
    this.maxTransfers = maxTransfers;
  }

  public boolean hasMaxWalkingDistance() {
    return maxWalkingDistance != -1;
  }

  public double getMaxWalkingDistance() {
    return maxWalkingDistance;
  }

  public void setMaxWalkingDistance(double maxWalkingDistance) {
    this.maxWalkingDistance = maxWalkingDistance;
  }

  public boolean isWalkingOnly() {
    return walkingOnly;
  }

  public void setWalkingOnly(boolean walkingOnly) {
    this.walkingOnly = walkingOnly;
  }
}
