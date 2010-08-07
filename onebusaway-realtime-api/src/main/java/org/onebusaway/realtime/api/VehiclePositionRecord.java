package org.onebusaway.realtime.api;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Vehicle position records are the key data structure for passing real-time
 * position data about a transit vehicle from an external data source into
 * OneBusAway. It tries to capture a variety of fields that might be present in
 * an AVL stream:
 * 
 * <ul>
 * <li>block id</li>
 * <li>trip id</li>
 * <li>vehicle id</li>
 * <li>position</li>
 * <li>schedule adherence</li>
 * <li>arrival data relative to a timepoint</li>
 * </ul>
 * 
 * Not all of these fields will necessarily be set by the AVL source, so we may
 * have to be flexible in how we process the data.
 * 
 * @author bdferris
 * @see VehiclePositionListener
 */
public class VehiclePositionRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private AgencyAndId blockId;

  private AgencyAndId tripId;

  private AgencyAndId vehicleId;

  private double currentLocationLat = Double.NaN;

  private double currentLocationLon = Double.NaN;

  private long currentTime;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
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

  public VehiclePositionRecord() {

  }

  public VehiclePositionRecord(VehiclePositionRecord r) {
    this.blockId = r.blockId;
    this.currentLocationLat = r.currentLocationLat;
    this.currentLocationLon = r.currentLocationLon;
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

  public double getCurrentLocationLat() {
    return currentLocationLat;
  }

  public void setCurrentLocationLat(double currentLocationLat) {
    this.currentLocationLat = currentLocationLat;
  }

  public double getCurrentLocationLon() {
    return currentLocationLon;
  }

  public void setCurrentLocationLon(double currentLocationLon) {
    this.currentLocationLon = currentLocationLon;
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
