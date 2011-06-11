package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ServiceDateSummary;

public interface StopScheduleService {
  public List<ServiceDateSummary> getServiceDateSummariesForStop(
      AgencyAndId stopId, boolean includePrivateService);
}
