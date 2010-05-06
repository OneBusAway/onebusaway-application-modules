package org.onebusaway.tripplanner.model;

public class TripPlannerConstraints {

  private long minDepartureTime = -1;

  private long maxDepartureTime = -1;

  private long maxTripDuration = -1;

  private int maxTransferCount = -1;

  private double maxSingleWalkDistance = -1;

  private int maxTripsPerHour = -1;

  private int maxTrips = -1;

  private double maxTripDurationRatio = -1;

  private long maxComputationTime = -1;

  public boolean hasMaxTripDuration() {
    return maxTripDuration != -1;
  }

  /**
   * @return time, in milliseconds
   */
  public long getMaxTripDuration() {
    return maxTripDuration;
  }

  public void setMaxTripDuration(long maxTripDuration) {
    this.maxTripDuration = maxTripDuration;
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

  public void setMaxDepartureTime(long maxStartTime) {
    this.maxDepartureTime = maxStartTime;
  }

  public boolean hasMaxTransferCount() {
    return maxTransferCount != -1;
  }

  public int getMaxTransferCount() {
    return maxTransferCount;
  }

  public void setMaxTransferCount(int maxTransferCount) {
    this.maxTransferCount = maxTransferCount;
  }

  public boolean hasMaxSingleWalkDistance() {
    return maxSingleWalkDistance != -1;
  }

  public double getMaxSingleWalkDistance() {
    return maxSingleWalkDistance;
  }

  public void setMaxSingleWalkDistance(double maxSingleWalkDistance) {
    this.maxSingleWalkDistance = maxSingleWalkDistance;
  }

  public boolean hasMaxTripsPerHour() {
    return maxTripsPerHour != -1;
  }

  public int getMaxTripsPerHour() {
    return maxTripsPerHour;
  }

  public void setMaxTripsPerHour(int maxTripsPerHour) {
    this.maxTripsPerHour = maxTripsPerHour;
  }

  public boolean hasMaxTrips() {
    return maxTrips != -1;
  }

  public int getMaxTrips() {
    return maxTrips;
  }

  public void setMaxTrips(int maxTrips) {
    this.maxTrips = maxTrips;
  }

  public boolean hasMaxTripDurationRatio() {
    return maxTripDurationRatio != -1;
  }

  public double getMaxTripDurationRatio() {
    return maxTripDurationRatio;
  }

  public void setMaxTripDurationRatio(double maxTripDurationRatio) {
    this.maxTripDurationRatio = maxTripDurationRatio;
  }

  public boolean hasMaxComputationTime() {
    return maxComputationTime != -1;
  }

  public long getMaxComputationTime() {
    return maxComputationTime;
  }

  public void setMaxComputationTime(long maxComputationTime) {
    this.maxComputationTime = maxComputationTime;
  }
}
