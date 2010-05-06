package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopTimeIndexImpl implements StopTimeIndex, Serializable {

  private static final long serialVersionUID = 1L;

  private Map<AgencyAndId, List<StopTimeEntry>> _stopTimesByServiceIdAndArrivalTime = new HashMap<AgencyAndId, List<StopTimeEntry>>();

  private Map<AgencyAndId, List<StopTimeEntry>> _stopTimesByServiceIdAndDepartureTime = new HashMap<AgencyAndId, List<StopTimeEntry>>();

  public void addStopTime(StopTimeEntry stopTime, AgencyAndId serviceId) {
    addStopTimeToMap(_stopTimesByServiceIdAndArrivalTime, stopTime, serviceId);
    addStopTimeToMap(_stopTimesByServiceIdAndDepartureTime, stopTime, serviceId);
  }

  public void sort() {
    for (List<StopTimeEntry> list : _stopTimesByServiceIdAndArrivalTime.values())
      Collections.sort(list, StopTimeOp.ARRIVAL);

    for (List<StopTimeEntry> list : _stopTimesByServiceIdAndDepartureTime.values())
      Collections.sort(list, StopTimeOp.DEPARTURE);
  }

  public List<StopTimeEntry> getAllStopTimes() {
    List<StopTimeEntry> stopTimes = new ArrayList<StopTimeEntry>();
    for (List<StopTimeEntry> sts : _stopTimesByServiceIdAndArrivalTime.values())
      stopTimes.addAll(sts);
    return stopTimes;
  }

  public Set<AgencyAndId> getDepartureServiceIds() {
    return _stopTimesByServiceIdAndDepartureTime.keySet();
  }

  public Set<AgencyAndId> getArrivalServiceIds() {
    return _stopTimesByServiceIdAndArrivalTime.keySet();
  }

  /*****************************************************************************
   * {@link StopTimeIndex} Interface
   ****************************************************************************/

  public Set<AgencyAndId> getServiceIds() {
    return Collections.unmodifiableSet(_stopTimesByServiceIdAndDepartureTime.keySet());
  }

  @Override
  public Map<AgencyAndId, List<StopTimeEntry>> getServiceIdAndStopTimesSortedByStopTimeOp(
      StopTimeOp stopTimeOp) {
    if (stopTimeOp == StopTimeOp.ARRIVAL)
      return _stopTimesByServiceIdAndArrivalTime;
    else
      return _stopTimesByServiceIdAndDepartureTime;
  }

  public List<StopTimeEntry> getStopTimesForServiceIdSortedByArrival(
      AgencyAndId serviceId) {
    return new StopTimeEntryList(
        _stopTimesByServiceIdAndArrivalTime.get(serviceId));

  }

  public List<StopTimeEntry> getStopTimesForServiceIdSortedByDeparture(
      AgencyAndId serviceId) {
    return new StopTimeEntryList(
        _stopTimesByServiceIdAndDepartureTime.get(serviceId));
  }

  public List<StopTimeEntry> getStopTimesForServiceId(AgencyAndId serviceId) {
    return new ArrayList<StopTimeEntry>(
        _stopTimesByServiceIdAndDepartureTime.get(serviceId));
  }

  private void addStopTimeToMap(
      Map<AgencyAndId, List<StopTimeEntry>> stopTimesByServiceId,
      StopTimeEntry stopTime, AgencyAndId serviceId) {
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
