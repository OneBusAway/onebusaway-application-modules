package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;

public interface TripStatusBeanService {

  public TripStatusBean getTripStatusForTripId(AgencyAndId tripId,
      long serviceDate, long time);

  public TripDetailsBean getTripStatusForVehicleAndTime(AgencyAndId vehicleId,
      long time, TripDetailsInclusionBean tripDetailsInclusionBean);

  public ListBean<TripDetailsBean> getActiveTripForBounds(
      TripsForBoundsQueryBean query);
}
