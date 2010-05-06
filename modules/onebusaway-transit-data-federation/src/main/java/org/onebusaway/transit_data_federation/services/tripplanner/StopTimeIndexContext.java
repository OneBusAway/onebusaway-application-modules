package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StopTimeIndexContext {

  public Map<AgencyAndId, List<Date>> getNextServiceDates(Set<AgencyAndId> keySet, long targetTime);

  public Map<AgencyAndId, List<Date>> getPreviousServiceDates(Set<AgencyAndId> keySet, long targetTime);
}
