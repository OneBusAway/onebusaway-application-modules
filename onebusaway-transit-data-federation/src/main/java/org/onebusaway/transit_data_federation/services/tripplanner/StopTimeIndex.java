package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;

public interface StopTimeIndex {

  public Set<LocalizedServiceId> getServiceIds();

  public ServiceIdIntervals getServiceIdIntervals();

  public Map<LocalizedServiceId, List<StopTimeEntry>> getServiceIdAndStopTimesSortedByStopTimeOp(
      StopTimeOp stopTimeOp);

  public List<StopTimeEntry> getStopTimesForServiceIdSortedByArrival(
      LocalizedServiceId serviceId);

  public List<StopTimeEntry> getStopTimesForServiceIdSortedByDeparture(
      LocalizedServiceId serviceId);
}
