package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.services.StopTimeIndex;
import org.onebusaway.tripplanner.services.StopTimeIndexContext;
import org.onebusaway.tripplanner.services.StopTimeIndexResult;

import edu.emory.mathcs.backport.java.util.Collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StopTimeIndexImpl implements StopTimeIndex, Serializable {

  private static final long serialVersionUID = 1L;

  private Map<String, List<StopTimeProxyImpl>> _stopTimesByServiceIdAndArrivalTime = new HashMap<String, List<StopTimeProxyImpl>>();

  private Map<String, List<StopTimeProxyImpl>> _stopTimesByServiceIdAndDepartureTime = new HashMap<String, List<StopTimeProxyImpl>>();

  public void addStopTime(StopTimeProxyImpl stopTime) {
    addStopTimeToMap(_stopTimesByServiceIdAndArrivalTime, stopTime);
    addStopTimeToMap(_stopTimesByServiceIdAndDepartureTime, stopTime);
  }

  public void sort() {
    for (List<StopTimeProxyImpl> list : _stopTimesByServiceIdAndArrivalTime.values())
      Collections.sort(list, TimeOp.ARRIVAL);

    for (List<StopTimeProxyImpl> list : _stopTimesByServiceIdAndDepartureTime.values())
      Collections.sort(list, TimeOp.DEPARTURE);
  }

  public List<StopTimeProxyImpl> getAllStopTimes() {
    List<StopTimeProxyImpl> stopTimes = new ArrayList<StopTimeProxyImpl>();
    for (List<StopTimeProxyImpl> sts : _stopTimesByServiceIdAndArrivalTime.values())
      stopTimes.addAll(sts);
    return stopTimes;
  }

  /*****************************************************************************
   * {@link StopTimeIndex} Interface
   ****************************************************************************/

  public StopTimeIndexResult getPreviousStopTimeArrival(StopTimeIndexContext context, long currentTime, Object hint) {
    HintImpl h = (HintImpl) hint;
    if (h == null)
      h = new HintImpl(TimeOp.ARRIVAL);
    return h.getPreviousStopTime(_stopTimesByServiceIdAndArrivalTime, context, currentTime);
  }

  public StopTimeIndexResult getNextStopTimeDeparture(StopTimeIndexContext context, long currentTime, Object hint) {
    HintImpl h = (HintImpl) hint;
    if (h == null)
      h = new HintImpl(TimeOp.DEPARTURE);
    return h.getNextStopTime(_stopTimesByServiceIdAndDepartureTime, context, currentTime);
  }

  private void addStopTimeToMap(Map<String, List<StopTimeProxyImpl>> stopTimesByServiceId, StopTimeProxyImpl stopTime) {
    List<StopTimeProxyImpl> list = stopTimesByServiceId.get(stopTime.getServiceId());
    if (list == null) {
      list = new ArrayList<StopTimeProxyImpl>();
      stopTimesByServiceId.put(stopTime.getServiceId(), list);
    }
    list.add(stopTime);
  }

}
