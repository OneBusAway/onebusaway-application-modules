package org.onebusaway.transit_data_federation.impl.predictions;

import org.onebusaway.utility.InterpolationLibrary;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public final class TripTimePredictionsEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long fromTime;

  private final long toTime;

  private final SortedMap<Long, Integer> scheduleDeviations;

  public TripTimePredictionsEntry(long fromTime, long toTime,
      SortedMap<Long, Integer> scheduleDeviations) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.scheduleDeviations = scheduleDeviations;
  }
  
  public long getFromTime() {
    return fromTime;
  }
  
  public long getToTime() {
    return toTime;
  }

  public int getScheduleDeviationForTargetTime(long targetTime) {
    
    if( ! (fromTime <= targetTime && targetTime <= toTime))
      return 0;
    
    if( scheduleDeviations.isEmpty() )
      return 0;
    
    long firstKey = scheduleDeviations.firstKey();
    long lastKey = scheduleDeviations.lastKey();
    
    if( targetTime < firstKey )
      return scheduleDeviations.get(firstKey);
    
    if( targetTime > lastKey)
      return scheduleDeviations.get(lastKey);

    return (int) Math.round(InterpolationLibrary.interpolate(scheduleDeviations,
        targetTime));
  }

  public TripTimePredictionsEntry addPrediction(long time,
      int scheduleDeviation, long windowSize) {
    
    SortedMap<Long, Integer> updatedScheduleDeviations = new TreeMap<Long, Integer>(this.scheduleDeviations);
    updatedScheduleDeviations.put(time, scheduleDeviation);
    
    long updatedFromTime = Math.min(fromTime, time);
    long updatedToTime = Math.max(toTime,time);
    long updatedWindowSize = updatedToTime - updatedFromTime;
    if( updatedWindowSize > windowSize) {
      double ratio = ((double) windowSize) / updatedWindowSize;
      updatedFromTime = (long) (time - (time-updatedFromTime) * ratio);
      updatedToTime = (long) (time + (updatedToTime - time) * ratio);
      // The +1 makes sure that we included the updatedToTime in the submap
      updatedScheduleDeviations = updatedScheduleDeviations.subMap(updatedFromTime, updatedToTime+1);
    }
    
    return new TripTimePredictionsEntry(updatedFromTime,updatedToTime,updatedScheduleDeviations);
  }
}
