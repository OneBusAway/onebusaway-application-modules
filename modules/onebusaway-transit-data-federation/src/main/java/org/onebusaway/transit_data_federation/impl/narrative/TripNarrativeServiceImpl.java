package org.onebusaway.transit_data_federation.impl.narrative;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.narrative.TripNarrativeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripNarrativeServiceImpl implements TripNarrativeService {

  private GtfsDao _dao;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }

  @Cacheable
  public TripNarrative getTripForId(AgencyAndId tripId) {

    Trip trip = _dao.getTripForId(tripId);

    TripNarrative.Builder r = TripNarrative.builder();
    r.setBlockId(trip.getBlockId());
    r.setBlockSequenceId(trip.getBlockSequenceId());
    r.setDirectionId(trip.getDirectionId());
    r.setId(tripId);
    r.setRouteId(trip.getRoute().getId());
    r.setRouteShortName(trip.getRouteShortName());
    r.setServiceId(trip.getServiceId());
    r.setShapeId(trip.getShapeId());
    r.setTripHeadsign(trip.getTripHeadsign());
    r.setTripShortName(trip.getTripShortName());

    return r.create();
  }

}
