package org.onebusaway.kcmetro_tcip.model;

import java.io.Serializable;

public class TimepointPrediction implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String blockId;

  private String trackerTripId;

  private String timepointId;

  private int scheduledTime;

  private int vehicleId;

  private String predictorType;

  private int goalTime;

  private int goalDeviation;

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public String getTrackerTripId() {
    return trackerTripId;
  }

  public void setTrackerTripId(String tripId) {
    this.trackerTripId = tripId;
  }

  public String getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(String timepointId) {
    this.timepointId = timepointId;
  }

  public int getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(int scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public int getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(int vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getPredictorType() {
    return predictorType;
  }

  public void setPredictorType(String predictorType) {
    this.predictorType = predictorType;
  }

  public int getGoalTime() {
    return goalTime;
  }

  public void setGoalTime(int goalTime) {
    this.goalTime = goalTime;
  }

  public int getGoalDeviation() {
    return goalDeviation;
  }

  public void setGoalDeviation(int goalDeviation) {
    this.goalDeviation = goalDeviation;
  }

  @Override
  public String toString() {
    return "TimepointPrediction(tripId=" + trackerTripId + " timepointId="
        + timepointId + " scheduledTime=" + scheduledTime + " goalTime="
        + goalTime + ")";
  }

}
