package org.onebusaway.transit_data_federation.services.realtime;

import java.io.Serializable;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;

public class ScheduleAdherenceRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private AgencyAndId blockId;

  private AgencyAndId tripId;

  private AgencyAndId vehicleId;

  private CoordinatePoint currentLocation;

  private long currentTime;

  private int scheduleDeviation;

  private AgencyAndId timepointId;

  /**
   * In seconds since the start of the service date
   */
  private int timepointScheduledTime;

  /**
   * In seconds since the start of the service date
   */
  private int timepointPredictedTime;

  public ScheduleAdherenceRecord() {

  }

  public ScheduleAdherenceRecord(ScheduleAdherenceRecord r) {
    this.blockId = r.blockId;
    this.currentLocation = r.currentLocation;
    this.currentTime = r.currentTime;
    this.scheduleDeviation = r.scheduleDeviation;
    this.serviceDate = r.serviceDate;
    this.timepointId = r.timepointId;
    this.timepointPredictedTime = r.timepointPredictedTime;
    this.timepointScheduledTime = r.timepointScheduledTime;
    this.tripId = r.tripId;
    this.vehicleId = r.vehicleId;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

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

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  public CoordinatePoint getCurrentLocation() {
    return currentLocation;
  }

  public void setCurrentLocation(CoordinatePoint currentLocation) {
    this.currentLocation = currentLocation;
  }

  public long getCurrentTime() {
    return currentTime;
  }

  public void setCurrentTime(long currentTime) {
    this.currentTime = currentTime;
  }

  /**
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation - in seconds (+deviation is late, -deviation is
   *          early)
   */
  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public AgencyAndId getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(AgencyAndId timepointId) {
    this.timepointId = timepointId;
  }

  /**
   * 
   * @return seconds since the start of the service date
   */
  public int getTimepointScheduledTime() {
    return timepointScheduledTime;
  }

  /**
   * 
   * @param timepointScheduledTime - seconds since the start of the service date
   */
  public void setTimepointScheduledTime(int timepointScheduledTime) {
    this.timepointScheduledTime = timepointScheduledTime;
  }

  /**
   * 
   * @return seconds since the start of the service date
   */
  public int getTimepointPredictedTime() {
    return timepointPredictedTime;
  }

  /**
   * 
   * @param timepointPredictedTime - seconds since the start of the service date
   */
  public void setTimepointPredictedTime(int timepointPredictedTime) {
    this.timepointPredictedTime = timepointPredictedTime;
  }
}
