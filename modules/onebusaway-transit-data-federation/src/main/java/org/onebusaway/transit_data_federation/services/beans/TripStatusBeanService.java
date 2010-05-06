package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.TripDetailsBean;

public interface TripStatusBeanService {
  public TripDetailsBean getTripStatus(AgencyAndId tripId);
}
