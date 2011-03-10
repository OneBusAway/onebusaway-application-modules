package org.onebusaway.transit_data_federation.impl.realtime.mybus;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class TimepointPrediction implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyAndId blockId;

  private AgencyAndId tripId;

  private AgencyAndId timepointId;

  private int timepointScheduledTime;

  private int timepointPredictedTime;

  private int scheduleDeviation;

  private AgencyAndId vehicleId;

  private String predictorType;

  private int timeOfPrediction;

  public AgencyAndId getBlockId() {
    return blockId;
  }

  public void setBlockId(AgencyAndId blockId) {
    this.blockId = blockId;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public AgencyAndId getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(AgencyAndId timepointId) {
    this.timepointId = timepointId;
  }

  public int getTimepointScheduledTime() {
    return timepointScheduledTime;
  }

  public void setTimepointScheduledTime(int timepointScheduledTime) {
    this.timepointScheduledTime = timepointScheduledTime;
  }

  public int getTimepointPredictedTime() {
    return timepointPredictedTime;
  }

  public void setTimepointPredictedTime(int timepointPredictedTime) {
    this.timepointPredictedTime = timepointPredictedTime;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  public String getPredictorType() {
    return predictorType;
  }

  public void setPredictorType(String predictorType) {
    this.predictorType = predictorType;
  }
  
  public int getTimeOfPrediction() {
    return timeOfPrediction;
  }

  public void setTimeOfPrediction(int timeOfPrediction) {
    this.timeOfPrediction = timeOfPrediction;
  }
}
