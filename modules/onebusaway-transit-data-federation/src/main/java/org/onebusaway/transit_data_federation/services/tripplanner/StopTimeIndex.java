package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopTimeIndex {

  public Set<AgencyAndId> getServiceIds();
  
  public Map<AgencyAndId,List<StopTimeEntry>> getServiceIdAndStopTimesSortedByStopTimeOp(StopTimeOp stopTimeOp);
  
  public List<StopTimeEntry> getStopTimesForServiceIdSortedByArrival(AgencyAndId serviceId);
  
  public List<StopTimeEntry> getStopTimesForServiceIdSortedByDeparture(AgencyAndId serviceId);
}
