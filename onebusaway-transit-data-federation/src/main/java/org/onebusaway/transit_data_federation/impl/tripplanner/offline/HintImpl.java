/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.Min;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexContext;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexResult;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

public class HintImpl {

  private StopTimeOp _op;

  public HintImpl(StopTimeOp op) {
    _op = op;
  }

  public StopTimeIndexResult getNextStopTime(StopTimeIndex stopIndex,
      StopTimeIndexContext context, long targetTime) {

    Map<LocalizedServiceId, List<Date>> _serviceDates = context.getNextServiceDates(
        stopIndex.getServiceIdIntervals(), targetTime);

    Min<StopTimeInstanceProxy> m = new Min<StopTimeInstanceProxy>();

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : _serviceDates.entrySet()) {

      LocalizedServiceId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimes = stopIndex.getStopTimesForServiceIdSortedByDeparture(serviceId);

      if (stopTimes.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int index = searchNext(stopTimes, 0, stopTimes.size(), serviceDate,
            targetTime);

        if (index < 0 || index >= stopTimes.size())
          continue;

        while (index > 0
            && _op.getValue(stopTimes.get(index)) == _op.getValue(stopTimes.get(index - 1)))
          index--;

        double previousTime = -1;

        while (0 <= index && index < stopTimes.size()) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(
              stopTimes.get(index), serviceDate);
          double stiTime = _op.getValue(sti);
          if (previousTime == -1)
            previousTime = stiTime;
          if (previousTime != stiTime)
            break;
          double delta = stiTime - targetTime;
          m.add(delta, sti);
          index++;
        }
      }
    }

    return new StopTimeIndexResult(m.getMinElements(), null);
  }

  public StopTimeIndexResult getPreviousStopTime(StopTimeIndex stopIndex,
      StopTimeIndexContext context, long targetTime) {

    Map<LocalizedServiceId, List<Date>> serviceDates = context.getPreviousServiceDates(
        stopIndex.getServiceIdIntervals(), targetTime);

    Min<StopTimeInstanceProxy> m = new Min<StopTimeInstanceProxy>();

    for (Map.Entry<LocalizedServiceId, List<Date>> entry : serviceDates.entrySet()) {

      LocalizedServiceId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimes = stopIndex.getStopTimesForServiceIdSortedByArrival(serviceId);

      if (stopTimes.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int index = searchPrevious(stopTimes, 0, stopTimes.size(), serviceDate,
            targetTime);

        if (index == 0 || index > stopTimes.size())
          continue;

        while (index < stopTimes.size()
            && _op.getValue(stopTimes.get(index - 1)) == _op.getValue(stopTimes.get(index)))
          index++;

        double previousTime = -1;

        while (0 < index && index <= stopTimes.size()) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(
              stopTimes.get(index - 1), serviceDate);
          double stiTime = _op.getValue(sti);
          if (previousTime == -1)
            previousTime = stiTime;
          if (stiTime != previousTime)
            break;
          double delta = targetTime - stiTime;
          m.add(delta, sti);
          index--;
        }
      }
    }

    return new StopTimeIndexResult(m.getMinElements(), null);
  }

  private int searchNext(List<StopTimeEntry> stopTimes, int indexFrom,
      int indexTo, Date serviceDate, long targetTime) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo) / 2;

    StopTimeEntry stopTime = stopTimes.get(index);
    long time = (long) (serviceDate.getTime() + _op.getValue(stopTime) * 1000);

    if (time == targetTime)
      return index;

    if (targetTime < time)
      return searchNext(stopTimes, indexFrom, index, serviceDate, targetTime);
    else
      return searchNext(stopTimes, index + 1, indexTo, serviceDate, targetTime);
  }

  private int searchPrevious(List<StopTimeEntry> stopTimes, int indexFrom,
      int indexTo, Date serviceDate, long targetTime) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo + 1) / 2;

    StopTimeEntry stopTime = stopTimes.get(index - 1);
    long time = (long) (serviceDate.getTime() + _op.getValue(stopTime) * 1000);

    if (time == targetTime)
      return index;

    if (targetTime < time)
      return searchPrevious(stopTimes, indexFrom, index - 1, serviceDate,
          targetTime);
    else
      return searchPrevious(stopTimes, index, indexTo, serviceDate, targetTime);
  }
}