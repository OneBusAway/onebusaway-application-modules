/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.model.tripplanner;

public class TripStats {

  private double walkingVelocity;

  private double totalWalkingDistance = 0;

  private double maxSingleWalkDistance = 0;

  private long firstWalkDuration = 0;

  private long initialSlackTime = 0;

  private long initialWaitingTime = 0;

  private long transferWaitingTime = 0;

  private long vehicleTime = 0;

  private int vehicleCount = 0;
  
  private double score;

  public TripStats(double walkingVelocity) {
    this.walkingVelocity = walkingVelocity;
  }

  public TripStats(TripStats stats) {
    this.walkingVelocity = stats.walkingVelocity;
    this.totalWalkingDistance = stats.totalWalkingDistance;
    this.maxSingleWalkDistance = stats.maxSingleWalkDistance;
    this.firstWalkDuration = stats.firstWalkDuration;
    this.initialSlackTime = stats.initialSlackTime;
    this.initialWaitingTime = stats.initialWaitingTime;
    this.transferWaitingTime = stats.transferWaitingTime;
    this.vehicleTime = stats.vehicleTime;
    this.vehicleCount = stats.vehicleCount;
  }

  public double getWalkingVelocity() {
    return walkingVelocity;
  }

  public int getVehicleCount() {
    return vehicleCount;
  }

  public void setVehicleCount(int vehicleCount) {
    this.vehicleCount = vehicleCount;
  }

  public void incrementVehicleCount() {
    this.vehicleCount++;
  }

  public double getTotalWalkingDistance() {
    return totalWalkingDistance;
  }

  public void setTotalWalkingDistance(double totalWalkingDistance) {
    this.totalWalkingDistance = totalWalkingDistance;
  }

  /**
   * This method also updates {@link #getMaxSingleWalkDistance()}
   * 
   * @param distance
   */
  public void incrementTotalWalkingDistance(double distance) {
    this.totalWalkingDistance += distance;
    this.maxSingleWalkDistance = Math.max(this.maxSingleWalkDistance, distance);
  }

  public double getMaxSingleWalkDistance() {
    return maxSingleWalkDistance;
  }

  public void setMaxSingleWalkDistance(double maxSingleWalkDistance) {
    this.maxSingleWalkDistance = maxSingleWalkDistance;
  }

  public long getFirstWalkDuration() {
    return firstWalkDuration;
  }

  public void setFirstWalkDuration(long firstWalkDuration) {
    this.firstWalkDuration = firstWalkDuration;
  }

  public long getInitialSlackTime() {
    return initialSlackTime;
  }

  public void setInitialSlackTime(long initialSlackTime) {
    this.initialSlackTime = initialSlackTime;
  }

  public void incrementInitialSlackTime(long time) {
    this.initialSlackTime += time;
  }

  public long getInitialWaitingTime() {
    return initialWaitingTime;
  }

  public void setInitialWaitingTime(long initialWaitingTime) {
    this.initialWaitingTime = initialWaitingTime;
  }

  public void incrementInitialWaitingTime(long time) {
    this.initialWaitingTime += time;
  }

  public long getTransferWaitingTime() {
    return transferWaitingTime;
  }

  public void setTransferWaitingTime(long transferWaitingTime) {
    this.transferWaitingTime = transferWaitingTime;
  }

  public void incrementTransferWaitingTime(long time) {
    this.transferWaitingTime += time;
  }

  public long getVehicleTime() {
    return vehicleTime;
  }

  public void setVehicleTime(long vehicleTime) {
    this.vehicleTime = vehicleTime;
  }

  public void incrementVehicleTime(long time) {
    this.vehicleTime += time;
  }

  public long getTripDuration() {
    return initialWaitingTime + transferWaitingTime + vehicleTime + (long) (totalWalkingDistance / walkingVelocity);
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }
}
