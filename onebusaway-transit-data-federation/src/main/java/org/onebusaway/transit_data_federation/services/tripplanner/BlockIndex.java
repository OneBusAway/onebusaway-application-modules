package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;

public interface BlockIndex {
  public Map<LocalizedServiceId, List<StopTimeEntry>> getServiceIdAndStopTimesSortedByStopTimeOp(
      StopTimeOp stopTimeOp);
}
