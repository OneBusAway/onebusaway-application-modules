package org.onebusaway.transit_data.model.realtime;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class VehicleLocationRecordBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private String blockId;

  private String tripId;

  private String vehicleId;

  private long timeOfRecord;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
  private double scheduleDeviation = Double.NaN;

  private double distanceAlongBlock = Double.NaN;

  private CoordinatePoint currentLocation;

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  private double currentOrientation = Double.NaN;

  private String phase;

  private String status;

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
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

  public String getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }

  public long getTimeOfRecord() {
    return timeOfRecord;
  }

  public void setTimeOfRecord(long timeOfRecord) {
    this.timeOfRecord = timeOfRecord;
  }

  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean isDistanceAlongBlockSet() {
    return !Double.isNaN(distanceAlongBlock);
  }

  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public CoordinatePoint getCurrentLocation() {
    return currentLocation;
  }

  public void setCurrentLocation(CoordinatePoint currentLocation) {
    this.currentLocation = currentLocation;
  }

  public boolean isCurrentOrientationSet() {
    return !Double.isNaN(currentOrientation);
  }

  public double getCurrentOrientation() {
    return currentOrientation;
  }

  public void setCurrentOrientation(double currentOrientation) {
    this.currentOrientation = currentOrientation;
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
}
