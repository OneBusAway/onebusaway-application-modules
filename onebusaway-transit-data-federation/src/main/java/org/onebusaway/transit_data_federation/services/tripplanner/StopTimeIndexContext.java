package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;
import org.onebusaway.gtfs.model.calendar.ServiceIdIntervals;

public interface StopTimeIndexContext {

  public Map<LocalizedServiceId, List<Date>> getNextServiceDates(ServiceIdIntervals serviceIdIntervals, long targetTime);

  public Map<LocalizedServiceId, List<Date>> getPreviousServiceDates(ServiceIdIntervals serviceIdIntervals, long targetTime);
}
