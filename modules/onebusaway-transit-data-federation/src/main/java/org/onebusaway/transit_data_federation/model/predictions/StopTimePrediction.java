package org.onebusaway.transit_data_federation.model.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

public final class StopTimePrediction implements Serializable {

  private static final long serialVersionUID = 1L;

  private AgencyAndId tripId;

  private AgencyAndId stopId;

  private long serviceDate;

  private int scheduledArrivalTime;

  private int predictedArrivalOffset;

  private int scheduledDepartureTime;

  private int predictedDepartureOffset;

  private long predictionTime;

  public AgencyAndId getTripId() {
    return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
    this.tripId = tripId;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  public void setStopId(AgencyAndId stopId) {
    this.stopId = stopId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  /**
   * @return scheduled arrival time in seconds from midnight
   */
  public int getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(int scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  /**
   * @return the predicted arrival offset, in seconds
   */
  public int getPredictedArrivalOffset() {
    return predictedArrivalOffset;
  }

  /**
   * Set the predicted arrival offset, in seconds
   * 
   * @param predictedArrivalOffset in seconds
   */
  public void setPredictedArrivalOffset(int predictedArrivalOffset) {
    this.predictedArrivalOffset = predictedArrivalOffset;
  }

  /**
   * @return scheduled departure time in seconds from midnight
   */
  public int getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(int scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  /**
   * @return predicted departure offset, in seconds
   */
  public int getPredictedDepartureOffset() {
    return predictedDepartureOffset;
  }

  /**
   * Set the predicted departure offset, in seconds
   * 
   * @param predictedDepartureOffset in seconds
   */
  public void setPredictedDepartureOffset(int predictedDepartureOffset) {
    this.predictedDepartureOffset = predictedDepartureOffset;
  }

  public long getPredictionTime() {
    return predictionTime;
  }

  public void setPredictionTime(long predictionTime) {
    this.predictionTime = predictionTime;
  }
}
