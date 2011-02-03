package org.onebusaway.transit_data.model.tripplanning;

import java.io.Serializable;
import java.util.Set;

public class ConstraintsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long time;

  private boolean arriveBy = false;

  private int resultCount = 3;

  private boolean useRealTime = false;

  private Set<String> modes = null;

  private int maxTripDuration = -1;

  private double walkSpeed = -1;

  private double walkReluctance = -1;

  private double maxWalkingDistance = -1;

  private double initialWaitReluctance = -1;

  private double waitReluctance = -1;

  private int minTransferTime = -1;

  private int transferCost = -1;

  private int maxTransfers = -1;

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
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

  public int getMaxTripDuration() {
    return maxTripDuration;
  }

  public void setMaxTripDuration(int maxTripDuration) {
    this.maxTripDuration = maxTripDuration;
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
}
