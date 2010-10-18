package org.onebusaway.api.model.transit;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public final class TripStatusV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  /****
   * These are fields that we can supply from schedule data
   ****/

  private long serviceDate;

  private Double scheduledDistanceAlongTrip;

  private Double totalDistanceAlongTrip;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private CoordinatePoint position;

  private String closestStop;

  private int closestStopTimeOffset;

  private String nextStop;

  private int nextStopTimeOffset;

  private String phase;

  private String status;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private boolean predicted = false;

  private Long lastUpdateTime;

  private Integer scheduleDeviation;

  private Double distanceAlongTrip;

  private String vehicleId;

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public Double getScheduledDistanceAlongTrip() {
    return scheduledDistanceAlongTrip;
  }

  public void setScheduledDistanceAlongTrip(Double scheduledDistanceAlongTrip) {
    this.scheduledDistanceAlongTrip = scheduledDistanceAlongTrip;
  }

  public Double getTotalDistanceAlongTrip() {
    return totalDistanceAlongTrip;
  }

  public void setTotalDistanceAlongTrip(Double totalDistanceAlongTrip) {
    this.totalDistanceAlongTrip = totalDistanceAlongTrip;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }

  public String getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(String closestStop) {
    this.closestStop = closestStop;
  }

  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }

  public String getNextStop() {
    return nextStop;
  }

  public void setNextStop(String nextStop) {
    this.nextStop = nextStop;
  }

  public int getNextStopTimeOffset() {
    return nextStopTimeOffset;
  }

  public void setNextStopTimeOffset(int nextStopTimeOffset) {
    this.nextStopTimeOffset = nextStopTimeOffset;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public Long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public Integer getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(Integer scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public Double getDistanceAlongTrip() {
    return distanceAlongTrip;
  }

  public void setDistanceAlongTrip(Double distanceAlongTrip) {
    this.distanceAlongTrip = distanceAlongTrip;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
}
