package org.onebusaway.transit_data_federation.impl.time;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.HintImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexContext;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndexResult;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopTimeSearchOperations {

  public static List<StopTimeInstanceProxy> getStopTimeInstancesInRange(
      StopTimeIndex index, long timeFrom, long timeTo, StopTimeOp stopTimeOp,
      Map<AgencyAndId, List<Date>> serviceIdsAndDates) {

    List<StopTimeInstanceProxy> stopTimeInstancesInRange = new ArrayList<StopTimeInstanceProxy>();
    Set<StopTimeEntry> inRange = new HashSet<StopTimeEntry>();

    Map<AgencyAndId, List<StopTimeEntry>> serviceIdAndStopTimesSortedByStopTimeOp = index.getServiceIdAndStopTimesSortedByStopTimeOp(stopTimeOp);

    for (Map.Entry<AgencyAndId, List<Date>> entry : serviceIdsAndDates.entrySet()) {

      AgencyAndId serviceId = entry.getKey();
      List<StopTimeEntry> stopTimeEntries = serviceIdAndStopTimesSortedByStopTimeOp.get(serviceId);
      if (stopTimeEntries == null)
        continue;

      for (Date serviceDate : entry.getValue()) {
        int timeFromOffset = (int) ((timeFrom - serviceDate.getTime()) / 1000);
        int timeToOffset = (int) ((timeTo - serviceDate.getTime()) / 1000);
        inRange.clear();
        getStopTimeEntriesInRange(stopTimeEntries, stopTimeOp, timeFromOffset,
            timeToOffset, inRange);
        for (StopTimeEntry stopTime : inRange)
          stopTimeInstancesInRange.add(new StopTimeInstanceProxy(stopTime,
              serviceDate));
      }
    }

    return stopTimeInstancesInRange;
  }

  public static List<StopTimeEntry> getStopTimeEntriesInRange(
      List<StopTimeEntry> stopTimeEntries, StopTimeOp stopTimeOp, int timeFrom,
      int timeTo) {
    return getStopTimeEntriesInRange(stopTimeEntries, stopTimeOp, timeFrom,
        timeTo, (List<StopTimeEntry>) new ArrayList<StopTimeEntry>());
  }

  public static <T extends Collection<StopTimeEntry>> T getStopTimeEntriesInRange(
      List<StopTimeEntry> stopTimeEntries, StopTimeOp stopTimeOp, int timeFrom,
      int timeTo, T result) {
    int fromIndex = searchForStopTime(stopTimeEntries, timeFrom, stopTimeOp);
    int toIndex = searchForStopTime(stopTimeEntries, timeTo, stopTimeOp);
    for (int index = fromIndex; index < toIndex; index++)
      result.add(stopTimeEntries.get(index));
    return result;
  }

  /**
   * Find the closest previous {@link StopTimeInstanceProxy} (possibly more than
   * one) whose arrival time is less than or equal to the specified target time.
   * 
   * @param context
   * @param targetTime
   * @param hint
   * @return
   */
  public static StopTimeIndexResult getPreviousStopTimeArrival(
      StopTimeIndex index, StopTimeIndexContext context, long currentTime,
      Object hint) {
    HintImpl h = (HintImpl) hint;
    if (h == null)
      h = new HintImpl(StopTimeOp.ARRIVAL);
    Map<AgencyAndId, List<StopTimeEntry>> stopTimesSortedByStopTimeOp = index.getServiceIdAndStopTimesSortedByStopTimeOp(StopTimeOp.ARRIVAL);
    return h.getPreviousStopTime(stopTimesSortedByStopTimeOp, context,
        currentTime);
  }

  /**
   * Find the next closest {@link StopTimeInstanceProxy} (possibly more than
   * one) whose departure time is greater than or equal to the specified target
   * time.
   * 
   * @param context
   * @param targetTime
   * @param hint
   * @return
   */
  public static StopTimeIndexResult getNextStopTimeDeparture(
      StopTimeIndex index, StopTimeIndexContext context, long currentTime,
      Object hint) {
    HintImpl h = (HintImpl) hint;
    if (h == null)
      h = new HintImpl(StopTimeOp.DEPARTURE);
    Map<AgencyAndId, List<StopTimeEntry>> stopTimesSortedByStopTimeOp = index.getServiceIdAndStopTimesSortedByStopTimeOp(StopTimeOp.DEPARTURE);
    return h.getNextStopTime(stopTimesSortedByStopTimeOp, context, currentTime);
  }

  /**
   * Return an index into the {@link StopTimeEntry} list such that if a new
   * StopTimeEntry with the specified target time was inserted into the list,
   * the list would remain in sorted order with respect to the
   * {@link StopTimeOp}
   * 
   * @param stopTimes a list of {@link StopTimeEntry} objects, sorted in the
   *          order appropriate to the {@link StopTimeOp}
   * @param targetTime target time in seconds since midnight
   * @param stopTimeOp the {@link StopTimeOp} determining whether arrival or
   *          departure time is used
   * @return
   */
  public static int searchForStopTime(List<StopTimeEntry> stopTimes,
      int targetTime, StopTimeOp stopTimeOp) {
    return search(stopTimes, targetTime, stopTimeOp, 0, stopTimes.size());
  }

  /****
   * Private Methods
   ****/

  private static int search(List<StopTimeEntry> stopTimes, int target,
      StopTimeOp stopTimeOp, int fromIndex, int toIndex) {

    if (fromIndex == toIndex)
      return fromIndex;

    int midIndex = (fromIndex + toIndex) / 2;
    StopTimeEntry stopTime = stopTimes.get(midIndex);
    int t = stopTimeOp.getTime(stopTime);

    if (target < t) {
      return search(stopTimes, target, stopTimeOp, fromIndex, midIndex);
    } else if (target > t) {
      return search(stopTimes, target, stopTimeOp, midIndex + 1, toIndex);
    } else {
      return midIndex;
    }
  }
}
