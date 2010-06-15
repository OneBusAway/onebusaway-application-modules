package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.trips.TripBean;

public interface TripBeanService {
  public TripBean getTripForId(AgencyAndId trip);
}
