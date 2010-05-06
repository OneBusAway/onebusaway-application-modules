package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;

import java.util.Date;
import java.util.List;

public interface StopTimeService {

  public List<StopTimeInstanceProxy> getStopTimeInstancesInTimeRange(
      AgencyAndId stopId, Date from, Date to);
}
