package org.onebusaway.transit_data_federation.services.realtime;

import java.util.SortedMap;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

/**
 * Vehicle position information for a particular block.
 * 
 * @author bdferris
 */
public class BlockLocation {

  /****
   * These are fields that we can supply from schedule data
   ****/

  private BlockInstance blockInstance;

  private BlockTripEntry activeTrip;

  private boolean inService;

  private double scheduledDistanceAlongBlock = Double.NaN;

  /****
   * These are fields that we can supply from schedule data, but also update
   * from real-time data when available
   ****/

  private CoordinatePoint location;

  private double orientation = Double.NaN;

  private BlockStopTimeEntry closestStop;

  private int closestStopTimeOffset;

  private BlockStopTimeEntry nextStop;

  private int nextStopTimeOffset;

  private EVehiclePhase phase;

  private String status;

  /****
   * These are fields that we can supply only from real-time data
   ****/

  private boolean predicted;

  private long lastUpdateTime;

  private CoordinatePoint lastKnownLocation;

  private double lastKnownOrientation = Double.NaN;

  private double scheduleDeviation = Double.NaN;

  private SortedMap<Integer, Double> scheduleDeviations = null;

  private double distanceAlongBlock = Double.NaN;

  private AgencyAndId vehicleId;

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public void setBlockInstance(BlockInstance instance) {
    this.blockInstance = instance;
  }

  /**
   * @return the active trip for the block
   */
  public BlockTripEntry getActiveTrip() {
    return activeTrip;
  }

  public void setActiveTrip(BlockTripEntry activeTrip) {
    this.activeTrip = activeTrip;
  }

  /**
   * @return true if the block trip is actively in service
   */
  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }

  public boolean isScheduledDistanceAlongBlockSet() {
    return !Double.isNaN(scheduledDistanceAlongBlock);
  }

  /**
   * If the trip is not in service (see {@link #isInService()}), this value will
   * be {@link Double#NaN}.
   * 
   * @return the scheduled distance traveled along the shape of the block, in
   *         meters
   */
  public double getScheduledDistanceAlongBlock() {
    return scheduledDistanceAlongBlock;
  }

  public void setScheduledDistanceAlongBlock(double scheduledDistanceAlongBlock) {
    this.scheduledDistanceAlongBlock = scheduledDistanceAlongBlock;
  }

  /**
   * @return the block position
   */
  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public boolean isOrientationSet() {
    return !Double.isNaN(orientation);
  }

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  public double getOrientation() {
    return orientation;
  }

  public void setOrientation(double orientation) {
    this.orientation = orientation;
  }

  /**
   * The closest stop to the current position of the transit vehicle among the
   * stop times of the current trip.
   * 
   * @return the closest stop time entry
   */
  public BlockStopTimeEntry getClosestStop() {
    return closestStop;
  }

  public void setClosestStop(BlockStopTimeEntry closestStop) {
    this.closestStop = closestStop;
  }

  /**
   * The time offset, in seconds, from the closest stop to the current position
   * of the transit vehicle among the stop times of the current trip. If the
   * number is positive, the stop is coming up. If negative, the stop has
   * already been passed.
   * 
   * @return time, in seconds
   */
  public int getClosestStopTimeOffset() {
    return closestStopTimeOffset;
  }

  /**
   * See description in {@link #getClosestStopTimeOffset()}.
   * 
   * @param closestStopTimeOffset the time offset from the closest stop, in
   *          seconds
   */
  public void setClosestStopTimeOffset(int closestStopTimeOffset) {
    this.closestStopTimeOffset = closestStopTimeOffset;
  }

  /**
   * The next upcoming stop to the current position of the transit vehicle among
   * the stop times of the current trip.
   * 
   * @return the next stop time entry
   */
  public BlockStopTimeEntry getNextStop() {
    return nextStop;
  }

  public void setNextStop(BlockStopTimeEntry nextStop) {
    this.nextStop = nextStop;
  }

  /**
   * The time offset, in seconds, from the next stop to the current position of
   * the transit vehicle.
   * 
   * @return time, in seconds
   */
  public int getNextStopTimeOffset() {
    return nextStopTimeOffset;
  }

  /**
   * See {@link #getNextStopTimeOffset()}
   * 
   * @param nextStopTimeOffset
   */
  public void setNextStopTimeOffset(int nextStopTimeOffset) {
    this.nextStopTimeOffset = nextStopTimeOffset;
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

  /**
   * If real-time data is available in any form (schedule deviation,
   * distanceAlongBlock, last known location) for this vehicle
   * 
   * @return true if real-time is available
   */
  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  /**
   * @return the time we last heard from the bus (Unix-time)
   */
  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public CoordinatePoint getLastKnownLocation() {
    return lastKnownLocation;
  }

  public void setLastKnownLocation(CoordinatePoint lastKnownLocation) {
    this.lastKnownLocation = lastKnownLocation;
  }

  public boolean isLastKnownOrientationSet() {
    return !Double.isNaN(lastKnownOrientation);
  }

  /**
   * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
   */
  public double getLastKnownOrientation() {
    return lastKnownOrientation;
  }

  public void setLastKnownOrientation(double lastKnownOrientation) {
    this.lastKnownOrientation = lastKnownOrientation;
  }

  /**
   * @return true if we have schedule deviation data
   */
  public boolean isScheduleDeviationSet() {
    return !Double.isNaN(scheduleDeviation);
  }

  /**
   * If no schedule deviation data is available, this value with be
   * {@link Double#NaN}.
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public double getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation schedule deviation, in seconds, (+deviation is
   *          late, -deviation is early)
   */
  public void setScheduleDeviation(double scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public boolean areScheduleDeviationsSet() {
    return scheduleDeviations != null && !scheduleDeviations.isEmpty();
  }

  public SortedMap<Integer, Double> getScheduleDeviations() {
    return scheduleDeviations;
  }

  public void setScheduleDeviations(
      SortedMap<Integer, Double> scheduleDeviations) {
    this.scheduleDeviations = scheduleDeviations;
  }

  public boolean isDistanceAlongBlockSet() {
    return !Double.isNaN(distanceAlongBlock);
  }

  /**
   * If the trip is not in service (see {@link #isInService()}), this value will
   * be {@link Double#NaN}.
   * 
   * @return the distance traveled along the shape of the block, in meters
   */
  public double getDistanceAlongBlock() {
    return distanceAlongBlock;
  }

  public void setDistanceAlongBlock(double distanceAlongBlock) {
    this.distanceAlongBlock = distanceAlongBlock;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("BlockLocation(");
    if (phase != null)
      b.append("phase=").append(phase).append(",");
    if (status != null)
      b.append("status=").append(status).append(",");
    if (isScheduleDeviationSet())
      b.append("scheduleDeviation=").append(scheduleDeviation).append(",");
    if (isDistanceAlongBlockSet())
      b.append("distanceAlongBlock=").append(distanceAlongBlock).append(",");
    if (vehicleId != null)
      b.append("vehicleId=").append(vehicleId).append(",");
    b.append(")");
    return b.toString();
  }
}
