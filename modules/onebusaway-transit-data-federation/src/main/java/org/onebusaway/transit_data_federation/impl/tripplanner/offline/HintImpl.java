/**
 * 
 */
package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexContext;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexResult;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import edu.washington.cs.rse.collections.stats.Min;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class HintImpl {

  private StopTimeOp _op;

  public HintImpl(StopTimeOp op) {
    _op = op;
  }

  public StopTimeIndexResult getNextStopTime(Map<AgencyAndId, List<StopTimeEntry>> timesByServiceId,
      StopTimeIndexContext context, long targetTime) {

    Map<AgencyAndId, List<Date>> _serviceDates = context.getNextServiceDates(timesByServiceId.keySet(), targetTime);

    Min<StopTimeInstanceProxy> m = new Min<StopTimeInstanceProxy>();

    for (Map.Entry<AgencyAndId, List<Date>> entry : _serviceDates.entrySet()) {

      AgencyAndId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimes = timesByServiceId.get(serviceId);

      if (stopTimes.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int index = searchNext(stopTimes, 0, stopTimes.size(), serviceDate, targetTime);

        if (index < 0 || index >= stopTimes.size())
          continue;

        while (index > 0 && _op.getTime(stopTimes.get(index)) == _op.getTime(stopTimes.get(index - 1)))
          index--;

        long previousTime = -1;

        while (0 <= index && index < stopTimes.size()) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(stopTimes.get(index), serviceDate);
          long stiTime = _op.getTime(sti);
          if (previousTime == -1)
            previousTime = stiTime;
          if (previousTime != stiTime)
            break;
          long delta = stiTime - targetTime;
          m.add(delta, sti);
          index++;
        }
      }
    }

    return new StopTimeIndexResult(m.getMinElements(), null);
  }

  public StopTimeIndexResult getPreviousStopTime(Map<AgencyAndId, List<StopTimeEntry>> timesByServiceId,
      StopTimeIndexContext context, long targetTime) {

    Map<AgencyAndId, List<Date>> serviceDates = context.getPreviousServiceDates(timesByServiceId.keySet(), targetTime);

    Min<StopTimeInstanceProxy> m = new Min<StopTimeInstanceProxy>();

    for (Map.Entry<AgencyAndId, List<Date>> entry : serviceDates.entrySet()) {

      AgencyAndId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimes = timesByServiceId.get(serviceId);

      if (stopTimes.isEmpty())
        continue;

      for (Date serviceDate : entry.getValue()) {

        int index = searchPrevious(stopTimes, 0, stopTimes.size(), serviceDate, targetTime);

        if (index == 0 || index > stopTimes.size())
          continue;

        while (index < stopTimes.size() && _op.getTime(stopTimes.get(index - 1)) == _op.getTime(stopTimes.get(index)))
          index++;

        long previousTime = -1;

        while (0 < index && index <= stopTimes.size()) {
          StopTimeInstanceProxy sti = new StopTimeInstanceProxy(stopTimes.get(index - 1), serviceDate);
          long stiTime = _op.getTime(sti);
          if (previousTime == -1)
            previousTime = stiTime;
          if (stiTime != previousTime)
            break;
          long delta = targetTime - stiTime;
          m.add(delta, sti);
          index--;
        }
      }
    }

    return new StopTimeIndexResult(m.getMinElements(), null);
  }

  private int searchNext(List<StopTimeEntry> stopTimes, int indexFrom, int indexTo, Date serviceDate,
      long targetTime) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo) / 2;

    StopTimeEntry stopTime = stopTimes.get(index);
    long time = serviceDate.getTime() + _op.getTime(stopTime) * 1000;

    if (time == targetTime)
      return index;

    if (targetTime < time)
      return searchNext(stopTimes, indexFrom, index, serviceDate, targetTime);
    else
      return searchNext(stopTimes, index + 1, indexTo, serviceDate, targetTime);
  }

  private int searchPrevious(List<StopTimeEntry> stopTimes, int indexFrom, int indexTo, Date serviceDate,
      long targetTime) {

    if (indexTo == indexFrom)
      return indexFrom;

    int index = (indexFrom + indexTo + 1) / 2;

    StopTimeEntry stopTime = stopTimes.get(index - 1);
    long time = serviceDate.getTime() + _op.getTime(stopTime) * 1000;

    if (time == targetTime)
      return index;

    if (targetTime < time)
      return searchPrevious(stopTimes, indexFrom, index - 1, serviceDate, targetTime);
    else
      return searchPrevious(stopTimes, index, indexTo, serviceDate, targetTime);
  }
}