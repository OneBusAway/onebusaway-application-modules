package org.onebusaway.where.model;

import java.io.Serializable;

public class TimepointPrediction implements Serializable {

  private static final long serialVersionUID = 1L;

  private String agencyId;

  private String blockId;

  private String tripId;

  private String timepointId;

  private int scheduledTime;

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

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
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
    return "tripId=" + tripId + " timepointId=" + timepointId;
  }
}
