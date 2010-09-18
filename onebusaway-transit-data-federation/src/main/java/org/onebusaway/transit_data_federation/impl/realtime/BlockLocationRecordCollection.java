package org.onebusaway.transit_data_federation.impl.realtime;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.utility.EOutOfRangeStrategy;
import org.onebusaway.utility.InterpolationLibrary;

/**
 * A collection of block location records from the same block/trip/vehicle over
 * a time range, designed to maintain a cache of recent records and easily
 * interpolate location and schedule deviations from specific timestamps
 * 
 * @author bdferris
 */
public final class BlockLocationRecordCollection implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long fromTime;

  private final long toTime;

  /**
   * Schedule deviations at particular points in time. Key = unix time ms and
   * Value = seconds.
   */
  private final SortedMap<Long, Double> scheduleDeviationsByTime;

  /**
   * Schedule deviations at particular points in the schedule. Key = schedule
   * time (secs) and Value = seconds.
   */
  private final SortedMap<Integer, Double> scheduleDeviationsByScheduleTime;

  /**
   * Distance along block at particular points in time. Key = unix time ms and
   * Value = meters.
   */
  private final SortedMap<Long, Double> distancesAlongBlock;

  /**
   * Locations at particular points in time. Key = unix time ms and Value =
   * location.
   */
  private final SortedMap<Long, CoordinatePoint> locations;

  private AgencyAndId vehicleId;

  public BlockLocationRecordCollection(long fromTime, long toTime,
      SortedMap<Long, Double> scheduleDeviationsByTime,
      SortedMap<Integer, Double> scheduleDeviationsByScheduleTime,
      SortedMap<Long, Double> distancesAlongBlock,
      SortedMap<Long, CoordinatePoint> locations) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.scheduleDeviationsByTime = scheduleDeviationsByTime;
    this.scheduleDeviationsByScheduleTime = scheduleDeviationsByScheduleTime;
    this.distancesAlongBlock = distancesAlongBlock;
    this.locations = locations;
  }

  public BlockLocationRecordCollection(long fromTime, long toTime) {
    this(fromTime, toTime, new TreeMap<Long, Double>(),
        new TreeMap<Integer, Double>(), new TreeMap<Long, Double>(),
        new TreeMap<Long, CoordinatePoint>());
  }

  /**
   * Convenience method that creates a link
   * {@link BlockLocationRecordCollection} from a list of records
   * 
   * @param records
   * @return a collection instance from the specified records
   */
  public static BlockLocationRecordCollection createFromRecords(
      List<BlockLocationRecord> records) {

    if (records.isEmpty())
      return null;

    long fromTime = Long.MAX_VALUE;
    long toTime = Long.MIN_VALUE;
    SortedMap<Long, Double> scheduleDeviationsByTime = new TreeMap<Long, Double>();
    SortedMap<Integer, Double> scheduleDeviationsByScheduleTime = new TreeMap<Integer, Double>();
    SortedMap<Long, Double> distancesAlongBlock = new TreeMap<Long, Double>();
    SortedMap<Long, CoordinatePoint> locations = new TreeMap<Long, CoordinatePoint>();
    AgencyAndId vehicleId = null;

    for (BlockLocationRecord record : records) {
      fromTime = Math.min(fromTime, record.getTime());
      toTime = Math.max(toTime, record.getTime());

      if (record.hasScheduleDeviation())
        scheduleDeviationsByTime.put(record.getTime(),
            record.getScheduleDeviation());

      if (record.hasDistanceAlongBlock())
        distancesAlongBlock.put(record.getTime(),
            record.getDistanceAlongBlock());

      if (record.hasLocation())
        locations.put(record.getTime(), record.getLocation());

      if (record.getVehicleId() != null)
        vehicleId = record.getVehicleId();
    }

    BlockLocationRecordCollection collection = new BlockLocationRecordCollection(
        fromTime, toTime, scheduleDeviationsByTime,
        scheduleDeviationsByScheduleTime, distancesAlongBlock, locations);
    collection.vehicleId = vehicleId;
    return collection;
  }

  public long getFromTime() {
    return fromTime;
  }

  public long getToTime() {
    return toTime;
  }

  public boolean hasScheduleDeviations() {
    return !scheduleDeviationsByTime.isEmpty();
  }

  public boolean hasDistancesAlongBlock() {
    return !distancesAlongBlock.isEmpty();
  }

  public boolean hasLocations() {
    return !locations.isEmpty();
  }

  public boolean isEmpty() {
    return scheduleDeviationsByTime.isEmpty() && distancesAlongBlock.isEmpty()
        && locations.isEmpty();
  }

  public int getScheduleDeviationForTargetTime(long targetTime) {

    if (scheduleDeviationsByTime.isEmpty())
      return 0;

    return (int) Math.round(InterpolationLibrary.interpolate(
        scheduleDeviationsByTime, targetTime, EOutOfRangeStrategy.LAST_VALUE));
  }

  public int getScheduleDeviationForScheduleTime(int scheduleTime) {
    if (scheduleDeviationsByScheduleTime.isEmpty())
      return 0;

    return (int) Math.round(InterpolationLibrary.interpolate(
        scheduleDeviationsByScheduleTime, scheduleTime,
        EOutOfRangeStrategy.LAST_VALUE));
  }

  public double getDistanceAlongBlockForTargetTime(long targetTime) {

    if (distancesAlongBlock.isEmpty())
      return Double.NaN;

    return InterpolationLibrary.interpolate(distancesAlongBlock, targetTime,
        EOutOfRangeStrategy.INTERPOLATE);
  }

  public CoordinatePoint getLastLocationForTargetTime(long targetTime) {

    if (locations.isEmpty())
      return null;

    SortedMap<Long, CoordinatePoint> headMap = locations.headMap(targetTime + 1);
    if (headMap.isEmpty())
      return null;

    return headMap.get(headMap.lastKey());
  }
  
  public long getLastUpdateTime(long targetTime) {
    if( ! scheduleDeviationsByTime.isEmpty() )
      return getLastUpdateTime(targetTime, scheduleDeviationsByTime);
    if( ! distancesAlongBlock.isEmpty() )
      return getLastUpdateTime(targetTime, distancesAlongBlock);
    if( ! locations.isEmpty() )
      return getLastUpdateTime(targetTime, locations);
    return 0;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public BlockLocationRecordCollection addRecord(BlockLocationRecord record,
      long windowSize) {

    long time = record.getTime();

    long updatedFromTime = Math.min(fromTime, time);
    long updatedToTime = Math.max(toTime, time);
    long updatedWindowSize = updatedToTime - updatedFromTime;

    SortedMap<Long, Double> updatedScheduleDeviationsByTime = getUpdatedScheduleDeviationsByTime(record);
    SortedMap<Integer, Double> updatedScheduleDeviationsByScheduleTime = getUpdatedScheduleDeviationsByScheduleTime(record);
    SortedMap<Long, Double> updatedDistancesAlongBlock = getUpdatedDistancesAlongBlock(record);
    SortedMap<Long, CoordinatePoint> updatedLocations = getUpdatedLocations(record);

    if (updatedWindowSize > windowSize) {

      double ratio = ((double) windowSize) / updatedWindowSize;
      updatedFromTime = (long) (time - (time - updatedFromTime) * ratio);
      updatedToTime = (long) (time + (updatedToTime - time) * ratio);

      updatedScheduleDeviationsByTime = submap(updatedFromTime, updatedToTime,
          updatedScheduleDeviationsByTime);
      updatedDistancesAlongBlock = submap(updatedFromTime, updatedToTime,
          updatedDistancesAlongBlock);
      updatedLocations = submap(updatedFromTime, updatedToTime,
          updatedLocations);

      updatedScheduleDeviationsByScheduleTime = submap(
          updatedScheduleDeviationsByScheduleTime, updatedFromTime,
          updatedToTime, record.getServiceDate());
    }

    BlockLocationRecordCollection collection = new BlockLocationRecordCollection(
        updatedFromTime, updatedToTime, updatedScheduleDeviationsByTime,
        updatedScheduleDeviationsByScheduleTime, updatedDistancesAlongBlock,
        updatedLocations);

    if (record.getVehicleId() != null)
      collection.vehicleId = record.getVehicleId();
    else
      collection.vehicleId = this.vehicleId;

    return collection;
  }

  private <T> SortedMap<Long, T> submap(long updatedFromTime,
      long updatedToTime, SortedMap<Long, T> map) {
    // The +1 makes sure that we included the updatedToTime in the submap
    map = map.subMap(updatedFromTime, updatedToTime + 1);
    return new TreeMap<Long, T>(map);
  }

  private SortedMap<Integer, Double> submap(
      SortedMap<Integer, Double> scheduleDeviationsByScheduleTime,
      long updatedFromTime, long updatedToTime, long serviceDate) {

    scheduleDeviationsByScheduleTime = new TreeMap<Integer, Double>(
        scheduleDeviationsByScheduleTime);

    while (!scheduleDeviationsByScheduleTime.isEmpty()) {
      Integer key = scheduleDeviationsByScheduleTime.firstKey();
      Double deviation = scheduleDeviationsByScheduleTime.get(key);
      long time = (long) (serviceDate + (key + deviation) * 1000);
      if (time < updatedFromTime)
        scheduleDeviationsByScheduleTime.remove(key);
      else
        break;
    }

    while (!scheduleDeviationsByScheduleTime.isEmpty()) {
      Integer key = scheduleDeviationsByScheduleTime.lastKey();
      Double deviation = scheduleDeviationsByScheduleTime.get(key);
      long time = (long) (serviceDate + (key + deviation) * 1000);
      if (updatedToTime < time)
        scheduleDeviationsByScheduleTime.remove(key);
      else
        break;
    }

    return scheduleDeviationsByScheduleTime;
  }
  
  private <V> long getLastUpdateTime(long targetTime, SortedMap<Long,V> map) {
    if( map.isEmpty() )
      return 0;
    SortedMap<Long, V> headMap = map.headMap(targetTime+1);
    if( headMap.isEmpty() )
      return map.firstKey();
    return headMap.lastKey();    
  }

  /****
   * Private Methods
   ****/

  private SortedMap<Long, Double> getUpdatedScheduleDeviationsByTime(
      BlockLocationRecord record) {

    SortedMap<Long, Double> updatedScheduleDeviations = this.scheduleDeviationsByTime;

    if (record.hasScheduleDeviation()) {
      updatedScheduleDeviations = new TreeMap<Long, Double>(
          updatedScheduleDeviations);
      updatedScheduleDeviations.put(record.getTime(),
          record.getScheduleDeviation());
    }

    return updatedScheduleDeviations;
  }

  private SortedMap<Integer, Double> getUpdatedScheduleDeviationsByScheduleTime(
      BlockLocationRecord record) {

    SortedMap<Integer, Double> updatedScheduleDeviations = this.scheduleDeviationsByScheduleTime;

    if (record.hasScheduleDeviation()) {
      double scheduleDeviation = record.getScheduleDeviation();
      int effectiveScheduleTime = (int) ((record.getTime() - record.getServiceDate()) / 1000 - scheduleDeviation);
      updatedScheduleDeviations = new TreeMap<Integer, Double>(
          updatedScheduleDeviations);
      // We clear the tail map so that if a new record comes in that increases
      // the schedule deviation to effective move us backwards, we clear out any
      // previous records in that interval
      updatedScheduleDeviations.tailMap(effectiveScheduleTime).clear();
      updatedScheduleDeviations.put(effectiveScheduleTime, scheduleDeviation);
    }

    return updatedScheduleDeviations;
  }

  private SortedMap<Long, Double> getUpdatedDistancesAlongBlock(
      BlockLocationRecord record) {

    SortedMap<Long, Double> updatedDistancesAlongBlock = this.distancesAlongBlock;

    if (record.hasDistanceAlongBlock()) {
      updatedDistancesAlongBlock = new TreeMap<Long, Double>(
          updatedDistancesAlongBlock);
      updatedDistancesAlongBlock.put(record.getTime(),
          record.getDistanceAlongBlock());
    }

    return updatedDistancesAlongBlock;
  }

  private SortedMap<Long, CoordinatePoint> getUpdatedLocations(
      BlockLocationRecord record) {

    SortedMap<Long, CoordinatePoint> updatedLocations = this.locations;

    if (record.hasLocation()) {
      updatedLocations = new TreeMap<Long, CoordinatePoint>(updatedLocations);
      updatedLocations.put(record.getTime(), record.getLocation());
    }

    return updatedLocations;
  }

}
