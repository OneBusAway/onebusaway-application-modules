package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;

public class StopTimeIndexImpl implements StopTimeIndex, Serializable {

  private static final long serialVersionUID = 1L;

  private ServiceIdIntervals _intervals = new ServiceIdIntervals();

  private Map<LocalizedServiceId, List<StopTimeEntry>> _stopTimesByServiceIdAndArrivalTime = new HashMap<LocalizedServiceId, List<StopTimeEntry>>();

  private Map<LocalizedServiceId, List<StopTimeEntry>> _stopTimesByServiceIdAndDepartureTime = new HashMap<LocalizedServiceId, List<StopTimeEntry>>();

  public void addStopTime(StopTimeEntry stopTime, LocalizedServiceId serviceId) {

    addStopTimeToMap(_stopTimesByServiceIdAndArrivalTime, stopTime, serviceId);
    addStopTimeToMap(_stopTimesByServiceIdAndDepartureTime, stopTime, serviceId);

    _intervals.addStopTime(serviceId, stopTime.getArrivalTime(),
        stopTime.getDepartureTime());
  }

  public void sort() {
    for (List<StopTimeEntry> stopTimes : _stopTimesByServiceIdAndArrivalTime.values())
      Collections.sort(stopTimes, StopTimeOp.ARRIVAL);
    for (List<StopTimeEntry> stopTimes : _stopTimesByServiceIdAndDepartureTime.values())
      Collections.sort(stopTimes, StopTimeOp.DEPARTURE);
  }

  public List<StopTimeEntry> getAllStopTimes() {
    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    for (List<StopTimeEntry> sts : _stopTimesByServiceIdAndArrivalTime.values())
      stopTimes.addAll(sts);
    return stopTimes;
  }

  /*****************************************************************************
   * {@link StopTimeIndex} Interface
   ****************************************************************************/

  @Override
  public Set<LocalizedServiceId> getServiceIds() {
    return _stopTimesByServiceIdAndDepartureTime.keySet();
  }

  @Override
  public ServiceIdIntervals getServiceIdIntervals() {
    return _intervals;
  }

  @Override
  public Map<LocalizedServiceId, List<StopTimeEntry>> getServiceIdAndStopTimesSortedByStopTimeOp(
      StopTimeOp stopTimeOp) {
    if (stopTimeOp == StopTimeOp.ARRIVAL)
      return _stopTimesByServiceIdAndArrivalTime;
    else
      return _stopTimesByServiceIdAndDepartureTime;
  }

  @Override
  public List<StopTimeEntry> getStopTimesForServiceIdSortedByArrival(
      LocalizedServiceId serviceId) {
    return new StopTimeEntryList(
        _stopTimesByServiceIdAndArrivalTime.get(serviceId));
  }

  @Override
  public List<StopTimeEntry> getStopTimesForServiceIdSortedByDeparture(
      LocalizedServiceId serviceId) {
    return new StopTimeEntryList(
        _stopTimesByServiceIdAndDepartureTime.get(serviceId));
  }

  /****
   * Private Methods
   ****/

  private void addStopTimeToMap(
      Map<LocalizedServiceId, List<StopTimeEntry>> stopTimesByServiceId,
      StopTimeEntry stopTime, LocalizedServiceId serviceId) {
    List<StopTimeEntry> list = stopTimesByServiceId.get(serviceId);
    if (list == null) {
      list = new ArrayList<StopTimeEntry>();
      stopTimesByServiceId.put(serviceId, list);
    }
    list.add(stopTime);
  }

  private static class StopTimeEntryList extends AbstractList<StopTimeEntry> {

    private List<StopTimeEntry> _proxies;

    public StopTimeEntryList(List<StopTimeEntry> proxies) {
      _proxies = proxies;
    }

    @Override
    public StopTimeEntry get(int index) {
      return _proxies.get(index);
    }

    @Override
    public int size() {
      return _proxies.size();
    }
  }
}
