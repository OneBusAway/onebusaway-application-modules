package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripBeanServiceImpl implements TripBeanService {

  private GtfsRelationalDao _dao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao dao) {
    _dao = dao;
  }

  @Cacheable
  public TripBean getTripForId(AgencyAndId tripId) {

    Trip trip = _dao.getTripForId(tripId);
    
    if( trip == null)
      return null;

    TripBean tripBean = new TripBean();

    tripBean.setId(ApplicationBeanLibrary.getId(tripId));
    tripBean.setRouteShortName(trip.getRouteShortName());
    tripBean.setTripShortName(trip.getTripShortName());
    tripBean.setTripHeadsign(trip.getTripHeadsign());

    return tripBean;
  }
}
