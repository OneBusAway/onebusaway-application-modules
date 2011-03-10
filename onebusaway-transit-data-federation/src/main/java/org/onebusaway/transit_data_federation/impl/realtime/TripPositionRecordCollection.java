package org.onebusaway.transit_data_federation.impl.realtime;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.utility.EOutOfRangeStrategy;
import org.onebusaway.utility.InterpolationLibrary;

/**
 * A collection of trip position records from the same trip/vehicle over a time
 * range, designed to maintain a cache of recent records and easily interpolate
 * position and schedule deviations from specific timestamps
 * 
 * @author bdferris
 */
public final class TripPositionRecordCollection implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long fromTime;

  private final long toTime;

  /**
   * Trip schedule deviations at particular points in time. Key = unix time ms
   * and Value = seconds.
   */
  private final SortedMap<Long, Integer> scheduleDeviations;

  private AgencyAndId vehicleId;

  public TripPositionRecordCollection(long fromTime, long toTime,
      SortedMap<Long, Integer> scheduleDeviations) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.scheduleDeviations = scheduleDeviations;
  }

  /**
   * Convenience method that creates a link {@link TripPositionRecordCollection}
   * from a list of records
   * 
   * @param records
   * @return a collection instance from the specified records
   */
  public static TripPositionRecordCollection createFromRecords(
      List<TripPositionRecord> records) {

    if (records.isEmpty())
      return null;

    long fromTime = Long.MAX_VALUE;
    long toTime = Long.MIN_VALUE;
    SortedMap<Long, Integer> scheduleDeviations = new TreeMap<Long, Integer>();
    AgencyAndId vehicleId = null;

    for (TripPositionRecord record : records) {
      fromTime = Math.min(fromTime, record.getTime());
      toTime = Math.max(toTime, record.getTime());
      scheduleDeviations.put(record.getTime(), record.getScheduleDeviation());
      if (record.getVehicleId() != null)
        vehicleId = record.getVehicleId();
    }

    TripPositionRecordCollection collection = new TripPositionRecordCollection(
        fromTime, toTime, scheduleDeviations);
    collection.vehicleId = vehicleId;
    return collection;
  }

  public long getFromTime() {
    return fromTime;
  }

  public long getToTime() {
    return toTime;
  }

  public boolean isEmpty() {
    return scheduleDeviations.isEmpty();
  }

  public int getScheduleDeviationForTargetTime(long targetTime) {

    if (scheduleDeviations.isEmpty())
      return 0;

    return (int) Math.round(InterpolationLibrary.interpolate(
        scheduleDeviations, targetTime, EOutOfRangeStrategy.LAST_VALUE));
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public TripPositionRecordCollection addRecord(TripPositionRecord record,
      long windowSize) {

    long time = record.getTime();
    int scheduleDeviation = record.getScheduleDeviation();

    SortedMap<Long, Integer> updatedScheduleDeviations = new TreeMap<Long, Integer>(
        this.scheduleDeviations);
    updatedScheduleDeviations.put(time, scheduleDeviation);

    long updatedFromTime = Math.min(fromTime, time);
    long updatedToTime = Math.max(toTime, time);
    long updatedWindowSize = updatedToTime - updatedFromTime;
    if (updatedWindowSize > windowSize) {
      double ratio = ((double) windowSize) / updatedWindowSize;
      updatedFromTime = (long) (time - (time - updatedFromTime) * ratio);
      updatedToTime = (long) (time + (updatedToTime - time) * ratio);
      // The +1 makes sure that we included the updatedToTime in the submap
      updatedScheduleDeviations = updatedScheduleDeviations.subMap(
          updatedFromTime, updatedToTime + 1);
      updatedScheduleDeviations = new TreeMap<Long, Integer>(
          updatedScheduleDeviations);
    }

    TripPositionRecordCollection collection = new TripPositionRecordCollection(
        updatedFromTime, updatedToTime, updatedScheduleDeviations);

    if (record.getVehicleId() != null)
      collection.vehicleId = record.getVehicleId();
    else
      collection.vehicleId = this.vehicleId;

    return collection;
  }

}
