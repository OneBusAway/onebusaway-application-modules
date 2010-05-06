package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripsForBoundsQueryBean;

public interface TripStatusBeanService {

  public TripStatusBean getTripStatusForTripId(AgencyAndId tripId,
      long serviceDate, long time);

  public ListBean<TripDetailsBean> getActiveTripForBounds(
      TripsForBoundsQueryBean query);
}
