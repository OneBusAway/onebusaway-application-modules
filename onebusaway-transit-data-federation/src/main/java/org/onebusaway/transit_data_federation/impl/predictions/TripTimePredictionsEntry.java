package org.onebusaway.transit_data_federation.impl.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.utility.EOutOfRangeStrategy;
import org.onebusaway.utility.InterpolationLibrary;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public final class TripTimePredictionsEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long fromTime;

  private final long toTime;

  /**
   * Trip schedule deviations at particular points in time. Key = unix time ms
   * and Value = seconds.
   */
  private final SortedMap<Long, Integer> scheduleDeviations;

  private AgencyAndId vehicleId;

  public TripTimePredictionsEntry(long fromTime, long toTime,
      SortedMap<Long, Integer> scheduleDeviations, AgencyAndId vehicleId) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.scheduleDeviations = scheduleDeviations;
    this.vehicleId = vehicleId;
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

  public TripTimePredictionsEntry addPrediction(long time,
      int scheduleDeviation, long windowSize) {

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

    return new TripTimePredictionsEntry(updatedFromTime, updatedToTime,
        updatedScheduleDeviations,this.vehicleId);
  }
}
