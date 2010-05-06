package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.TripStopTimesBean;

public interface TripStopTimesBeanService {

  public TripStopTimesBean getStopTimesForTrip(AgencyAndId tripId);

}
