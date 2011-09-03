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
package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ConstraintsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean departNow = true;

  private boolean arriveBy = false;

  private int resultCount = 3;

  private boolean useRealTime = false;

  private Set<String> modes = null;

  private int maxTripDuration = -1;

  private String optimizeFor;

  private double walkSpeed = -1;

  private double walkReluctance = -1;

  private double maxWalkingDistance = -1;

  private double initialWaitReluctance = -1;

  private double waitReluctance = -1;

  private int minTransferTime = -1;

  private int transferCost = -1;

  private int maxTransfers = -1;

  private long maxComputationTime = -1;

  /**
   * Why do we include an alternate definition of the current time? It's useful
   * when attempting to run simulated queries in the past
   */
  private long currentTime = -1;

  /**
   * When you've specified a departure time, the {@link #lookaheadTime}
   * parameter specifies a time value, in seconds, that will shift the departure
   * time forward to include trips that start after the lookahead-adjusted start
   * time. Why not just modify the departure time yourself? Trips that start
   * between the lookahead departure time and the original departure time are
   * not counted against {@link #resultCount}. This parameter is designed to
   * allow you to include trips that JUST left in the result set while still
   * including the trips that depart as normal.
   * 
   * For arriveBy trips, the semantics are reversed aka the arrival time is
   * shifted to be later by the {@link #lookaheadTime} parameter.
   */
  private int lookaheadTime = 0;

  private ItineraryBean selectedItinerary = null;

  public ConstraintsBean() {

  }

  public ConstraintsBean(ConstraintsBean c) {
    this.departNow = c.departNow;
    this.arriveBy = c.arriveBy;
    this.initialWaitReluctance = c.initialWaitReluctance;
    this.maxTransfers = c.maxTransfers;
    this.maxTripDuration = c.maxTripDuration;
    this.maxWalkingDistance = c.maxWalkingDistance;
    this.minTransferTime = c.minTransferTime;
    if (c.modes != null)
      this.modes = new HashSet<String>(c.modes);
    this.optimizeFor = c.optimizeFor;
    this.resultCount = c.resultCount;
    this.transferCost = c.transferCost;
    this.useRealTime = c.useRealTime;
    this.waitReluctance = c.waitReluctance;
    this.walkReluctance = c.walkReluctance;
    this.walkSpeed = c.walkSpeed;
  }

  public boolean isDepartNow() {
    return departNow;
  }

  public void setDepartNow(boolean departNow) {
    this.departNow = departNow;
  }

  public boolean isArriveBy() {
    return arriveBy;
  }

  public void setArriveBy(boolean arriveBy) {
    this.arriveBy = arriveBy;
  }

  public int getResultCount() {
    return resultCount;
  }

  public void setResultCount(int resultCount) {
    this.resultCount = resultCount;
  }

  public boolean isUseRealTime() {
    return useRealTime;
  }

  public void setUseRealTime(boolean useRealTime) {
    this.useRealTime = useRealTime;
  }

  public Set<String> getModes() {
    return modes;
  }

  public void setModes(Set<String> modes) {
    this.modes = modes;
  }

  /**
   * @return maximum trip duration, in seconds
   */
  public int getMaxTripDuration() {
    return maxTripDuration;
  }

  /**
   * @param maxTripDuration time, in seconds
   */
  public void setMaxTripDuration(int maxTripDuration) {
    this.maxTripDuration = maxTripDuration;
  }

  public String getOptimizeFor() {
    return optimizeFor;
  }

  public void setOptimizeFor(String optimizeFor) {
    this.optimizeFor = optimizeFor;
  }

  public double getWalkSpeed() {
    return walkSpeed;
  }

  public void setWalkSpeed(double walkSpeed) {
    this.walkSpeed = walkSpeed;
  }

  public double getWalkReluctance() {
    return walkReluctance;
  }

  public void setWalkReluctance(double walkReluctance) {
    this.walkReluctance = walkReluctance;
  }

  public double getMaxWalkingDistance() {
    return maxWalkingDistance;
  }

  public void setMaxWalkingDistance(double maxWalkingDistance) {
    this.maxWalkingDistance = maxWalkingDistance;
  }

  public double getInitialWaitReluctance() {
    return initialWaitReluctance;
  }

  public void setInitialWaitReluctance(double initialWaitReluctance) {
    this.initialWaitReluctance = initialWaitReluctance;
  }

  public double getWaitReluctance() {
    return waitReluctance;
  }

  public void setWaitReluctance(double waitReluctance) {
    this.waitReluctance = waitReluctance;
  }

  public int getMinTransferTime() {
    return minTransferTime;
  }

  public void setMinTransferTime(int minTransferTime) {
    this.minTransferTime = minTransferTime;
  }

  public int getTransferCost() {
    return transferCost;
  }

  public void setTransferCost(int transferCost) {
    this.transferCost = transferCost;
  }

  public int getMaxTransfers() {
    return maxTransfers;
  }

  public void setMaxTransfers(int maxTransfers) {
    this.maxTransfers = maxTransfers;
  }

  public long getMaxComputationTime() {
    return maxComputationTime;
  }

  public void setMaxComputationTime(long maxComputationTime) {
    this.maxComputationTime = maxComputationTime;
  }

  public long getCurrentTime() {
    return currentTime;
  }

  public void setCurrentTime(long currentTime) {
    this.currentTime = currentTime;
  }

  /**
   * When you've specified a departure time, the {@link #lookaheadTime}
   * parameter specifies a time value, in seconds, that will shift the departure
   * time forward to include trips that start after the lookahead-adjusted start
   * time. Why not just modify the departure time yourself? Trips that start
   * between the lookahead departure time and the original departure time are
   * not counted against {@link #resultCount}. This parameter is designed to
   * allow you to include trips that JUST left in the result set while still
   * including the trips that depart as normal.
   * 
   * For arriveBy trips, the semantics are reversed aka the arrival time is
   * shifted to be later by the {@link #lookaheadTime} parameter.
   * 
   * @return time, in seconds
   */
  public int getLookaheadTime() {
    return lookaheadTime;
  }

  public void setLookaheadTime(int lookaheadTime) {
    this.lookaheadTime = lookaheadTime;
  }

  public ItineraryBean getSelectedItinerary() {
    return selectedItinerary;
  }

  public void setSelectedItinerary(ItineraryBean selectedItinerary) {
    this.selectedItinerary = selectedItinerary;
  }
}
