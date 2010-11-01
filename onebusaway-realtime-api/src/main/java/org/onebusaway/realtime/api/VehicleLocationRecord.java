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

  private long timeOfRecord;

  /**
   * schedule deviation, in seconds, (+deviation is late, -deviation is early)
   */
  private double scheduleDeviation = Double.NaN;

  private double distanceAlongBlock = Double.NaN;

  private double currentLocationLat = Double.NaN;

  private double currentLocationLon = Double.NaN;

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  private double currentOrientation = Double.NaN;

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

  private EVehiclePhase phase;

  private String status;

  public VehicleLocationRecord() {

  }

  public VehicleLocationRecord(VehicleLocationRecord r) {
    this.blockId = r.blockId;
    this.currentLocationLat = r.currentLocationLat;
    this.currentLocationLon = r.currentLocationLon;
    this.currentOrientation = r.currentOrientation;
    this.timeOfRecord = r.timeOfRecord;
    this.distanceAlongBlock = r.distanceAlongBlock;
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

  /**
   * 
   * @return time when the vehicle location record was made, in unix-time (ms)
   */
  public long getTimeOfRecord() {
    return timeOfRecord;
  }

  /**
   * 
   * @param timeOfRecord time when the vehicle location record was made, in
   *          unix-time (ms)
   */
  public void setTimeOfRecord(long timeOfRecord) {
    this.timeOfRecord = timeOfRecord;
  }

  /**
   * @return true if schedule deviation information has been provided
   */
  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  /**
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation - in seconds (+deviation is late, -deviation is
   *          early)
   */
  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean isDistanceAlongBlockSet() {
    return !Double.isNaN(distanceAlongBlock);
  }

  /**
   * 
   * @return the distance traveled along the block in meters, or NaN if not set
   */
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  /**
   * 
   * @param distanceAlongBlock distance traveled along the block in meters, or
   *          NaN if not set
   */
  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public boolean isCurrentLocationSet() {
    return !(Double.isNaN(currentLocationLat) || Double.isNaN(currentLocationLon));
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

  public boolean isCurrentOrientationSet() {
    return !Double.isNaN(currentOrientation);
  }

  public double getCurrentOrientation() {
    return currentOrientation;
  }

  public void setCurrentOrientation(double currentOrientation) {
    this.currentOrientation = currentOrientation;
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

  public EVehiclePhase getPhase() {
    return phase;
  }

  public void setPhase(EVehiclePhase phase) {
    this.phase = phase;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
