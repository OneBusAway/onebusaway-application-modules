package org.onebusaway.transit_data_federation.impl.narrative;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.narrative.StopTimeNarrativeService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopTimeNarrativeServiceImpl implements StopTimeNarrativeService {

  private GtfsDao _dao;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _dao = dao;
  }

  @Cacheable
  public StopTimeNarrative getStopTimeForEntry(StopTimeEntry entry) {

    int id = entry.getId();
    StopTime stopTime = _dao.getStopTimeForId(id);

    StopTimeNarrative.Builder r = StopTimeNarrative.builder();
    r.setArrivalTime(entry.getArrivalTime());
    r.setDepartureTime(entry.getDepartureTime());
    r.setDropOffType(entry.getDropOffType());
    r.setId(id);
    r.setPickupType(entry.getPickupType());
    r.setRouteShortName(stopTime.getRouteShortName());
    r.setShapeDistTraveled(stopTime.getShapeDistTraveled());
    r.setStopHeadsign(stopTime.getStopHeadsign());
    r.setStopId(entry.getStop().getId());
    r.setTripId(entry.getTrip().getId());
    r.setStopSequence(entry.getSequence());

    return r.create();
  }

}
