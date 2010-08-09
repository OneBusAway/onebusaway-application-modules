package org.onebusaway.realtime.api;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Vehicle location records are the key data structure for passing real-time
 * location data about a transit vehicle from an external data source into
 * OneBusAway. It tries to capture a variety of fields that might be present in
 * an AVL stream:
 * 
 * <ul>
 * <li>block id</li>
 * <li>trip id</li>
 * <li>vehicle id</li>
 * <li>location</li>
 * <li>schedule adherence</li>
 * <li>arrival data relative to a timepoint</li>
 * </ul>
 * 
 * Not all of these fields will necessarily be set by the AVL source, so we may
 * have to be flexible in how we process the data.
 * 
 * @author bdferris
 * @see VehicleLocationListener
 */
public class VehicleLocationRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  private long serviceDate;

  private AgencyAndId blockId;

  private AgencyAndId tripId;

  private AgencyAndId vehicleId;

  private double currentLocationLat = Double.NaN;

  private double currentLocationLon = Double.NaN;

  private long currentTime;

  /**
   * 
   */
  private boolean scheduleDeviationSet = false;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
  private int scheduleDeviation;

  /**
   * 
   */
  private boolean positionDeviationSet = false;

  /**
   * position deviation, in meters, (+deviation is late, -deviation is early)
   */
  private double positionDeviation;

  /**
   * 
   */

  private AgencyAndId timepointId;

  /**
   * In seconds since the start of the service date
   */
  private int timepointScheduledTime;

  /**
   * In seconds since the start of the service date
   */
  private int timepointPredictedTime;

  public VehicleLocationRecord() {

  }

  public VehicleLocationRecord(VehicleLocationRecord r) {
    this.blockId = r.blockId;
    this.currentLocationLat = r.currentLocationLat;
    this.currentLocationLon = r.currentLocationLon;
    this.currentTime = r.currentTime;
    this.positionDeviationSet = r.positionDeviationSet;
    this.positionDeviation = r.positionDeviation;
    this.scheduleDeviationSet = r.scheduleDeviationSet;
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
   * @return true if schedule deviation information has been provided
   */
  public boolean isScheduleDeviationSet() {
    return scheduleDeviationSet;
  }

  public void setScheduleDeviationSet(boolean scheduleDeviationSet) {
    this.scheduleDeviationSet = scheduleDeviationSet;
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
    this.scheduleDeviationSet = true;
    this.scheduleDeviation = scheduleDeviation;
  }

  /**
   * @return true if position deviation information has been provided
   */
  public boolean isPositionDeviationSet() {
    return positionDeviationSet;
  }

  public void setPositionDeviationSet(boolean positionDeviationSet) {
    this.positionDeviationSet = positionDeviationSet;
  }

  /**
   * @return position deviation, in meters, (+deviation is late, -deviation is
   *         early)
   */

  public double getPositionDeviation() {
    return positionDeviation;
  }

  /**
   * 
   * @param positionDeviation - in meters (+deviation is late, -deviation is
   *          early)
   */
  public void setPositionDeviation(double positionDeviation) {
    this.positionDeviationSet = true;
    this.positionDeviation = positionDeviation;
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
