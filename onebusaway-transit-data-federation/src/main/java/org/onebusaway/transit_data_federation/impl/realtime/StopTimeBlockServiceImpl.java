package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.stereotype.Component;

@Component
public class StopTimeBlockServiceImpl implements StopTimeBlockService {

  private static final StopTimeOp TIME_OP = new TimeOp();

  private static final StopTimeOp DISTANCE_OP = new DistanceOp();

  @Override
  public List<StopTimeEntry> getSurroundingStopTimes(TripEntry trip,
      int scheduleTime) {

    return getSurroundingStopTimes(TIME_OP, trip, scheduleTime);
  }

  @Override
  public List<StopTimeEntry> getSurroundingStopTimesFromDistanceAlongBlock(
      TripEntry trip, double distanceAlongBlock) {

    return getSurroundingStopTimes(DISTANCE_OP, trip, distanceAlongBlock);
  }

  /****
   * Private Methods
   ****/

  private List<StopTimeEntry> getSurroundingStopTimes(StopTimeOp op,
      TripEntry trip, double value) {

    List<StopTimeEntry> stopTimes = trip.getStopTimes();

    if (stopTimes.isEmpty()) {

    }

    StopTimeEntry first = stopTimes.get(0);
    StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);

    if (op.isStopTimeLessThanOrEqualtToValue(first, value)
        && op.isValueLessThanOrEqualToStopTime(value, last))
      return stopTimes;
    else if (op.isValueLessThanOrEqualToStopTime(value, first))
      return getSurroundingStopTimesLessThan(op, trip, value);
    else
      return getSurroundingStopTimesGreaterThan(op, trip, value);
  }

  private List<StopTimeEntry> getSurroundingStopTimesLessThan(StopTimeOp op,
      TripEntry trip, double value) {
    TripEntry previous = trip.getPrevTrip();
    if (previous == null)
      return Collections.emptyList();
    return getSurroundingStopTimesLessThan(op, previous, trip, value);
  }

  private List<StopTimeEntry> getSurroundingStopTimesLessThan(StopTimeOp op,
      TripEntry trip, TripEntry next, double value) {

    List<StopTimeEntry> stopTimes = trip.getStopTimes();

    if (stopTimes.isEmpty()) {
      TripEntry previous = trip.getPrevTrip();
      if (previous != null)
        return getSurroundingStopTimesLessThan(op, previous, next, value);
      else
        return Collections.emptyList();
    }

    StopTimeEntry first = stopTimes.get(0);
    StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);

    if (op.isStopTimeLessThanOrEqualtToValue(first, value)
        && op.isValueLessThanOrEqualToStopTime(value, last)) {
      return stopTimes;
    } else if (op.isValueLessThanOrEqualToStopTime(value, first)) {
      return getSurroundingStopTimesLessThan(op, trip, value);
    } else {
      List<StopTimeEntry> nextStopTimes = next.getStopTimes();
      return Arrays.asList(last, nextStopTimes.get(0));
    }
  }

  private List<StopTimeEntry> getSurroundingStopTimesGreaterThan(StopTimeOp op,
      TripEntry trip, double value) {
    TripEntry next = trip.getNextTrip();
    if (next == null)
      return Collections.emptyList();
    return getSurroundingStopTimesGreaterThan(op, next, trip, value);
  }

  private List<StopTimeEntry> getSurroundingStopTimesGreaterThan(StopTimeOp op,
      TripEntry trip, TripEntry previous, double value) {

    List<StopTimeEntry> stopTimes = trip.getStopTimes();

    if (stopTimes.isEmpty()) {
      TripEntry next = trip.getNextTrip();
      if (next != null)
        return getSurroundingStopTimesGreaterThan(op, next, previous, value);
      else
        return Collections.emptyList();
    }

    StopTimeEntry first = stopTimes.get(0);
    StopTimeEntry last = stopTimes.get(stopTimes.size() - 1);

    if (op.isStopTimeLessThanOrEqualtToValue(first, value)
        && op.isValueLessThanOrEqualToStopTime(value, last)) {
      return stopTimes;
    } else if (op.isStopTimeLessThanOrEqualtToValue(last, value)) {
      return getSurroundingStopTimesGreaterThan(op, trip, value);
    } else {
      List<StopTimeEntry> previousStopTimes = previous.getStopTimes();
      return Arrays.asList(previousStopTimes.get(previousStopTimes.size() - 1),
          first);
    }
  }

  private interface StopTimeOp {
    public boolean isStopTimeLessThanOrEqualtToValue(StopTimeEntry stopTime,
        double value);

    public boolean isValueLessThanOrEqualToStopTime(double value,
        StopTimeEntry stopTime);
  }

  private static class TimeOp implements StopTimeOp {

    @Override
    public boolean isStopTimeLessThanOrEqualtToValue(StopTimeEntry stopTime,
        double value) {
      return stopTime.getArrivalTime() <= value;
    }

    @Override
    public boolean isValueLessThanOrEqualToStopTime(double value,
        StopTimeEntry stopTime) {
      return value <= stopTime.getDepartureTime();
    }
  }

  private static class DistanceOp implements StopTimeOp {

    @Override
    public boolean isStopTimeLessThanOrEqualtToValue(StopTimeEntry stopTime,
        double value) {
      return stopTime.getDistaceAlongBlock() <= value;
    }

    @Override
    public boolean isValueLessThanOrEqualToStopTime(double value,
        StopTimeEntry stopTime) {
      return value <= stopTime.getDistaceAlongBlock();
    }
  }
}
