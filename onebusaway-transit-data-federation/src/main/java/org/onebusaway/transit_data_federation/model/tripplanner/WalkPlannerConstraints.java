package org.onebusaway.transit_data_federation.model.tripplanner;

public class WalkPlannerConstraints {

  private double maxTripLength = -1;

  private long maxComputationTime = -1;

  public boolean hasMaxTripLength() {
    return maxTripLength >= 0;
  }

  /**
   * @return distance
   */
  public double getMaxTripLength() {
    return maxTripLength;
  }

  /**
   * 
   * @param maxTripLength distance
   */
  public void setMaxTripLength(double maxTripLength) {
    this.maxTripLength = maxTripLength;
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
