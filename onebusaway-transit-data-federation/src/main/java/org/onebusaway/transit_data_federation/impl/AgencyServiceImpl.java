package org.onebusaway.transit_data_federation.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.AgencyService;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgencyServiceImpl implements AgencyService {

  private static Logger _log = LoggerFactory.getLogger(AgencyServiceImpl.class);

  @Autowired
  private GtfsRelationalDao _dao;

  @Autowired
  private TransitGraphDao _graph;

  @Cacheable
  public TimeZone getTimeZoneForAgencyId(String agencyId) {
    Agency agency = _dao.getAgencyForId(agencyId);
    if (agency == null)
      return null;
    return TimeZone.getTimeZone(agency.getTimezone());
  }

  @Cacheable
  public Map<String, CoordinatePoint> getAgencyIdsAndCenterPoints() {

    Map<String, StopsCenterOfMass> stopsByAgencyId = new HashMap<String, StopsCenterOfMass>();

    for (TripEntry trip : _graph.getAllTrips()) {

      AgencyAndId id = trip.getId();
      String agencyId = id.getAgencyId();

      StopsCenterOfMass stops = stopsByAgencyId.get(agencyId);

      if (stops == null) {
        stops = new StopsCenterOfMass();
        stopsByAgencyId.put(agencyId, stops);
      }

      for (StopTimeEntry stopTime : trip.getStopTimes()) {
        StopEntry stop = stopTime.getStop();
        stops.lats += stop.getStopLat();
        stops.lons += stop.getStopLon();
        stops.count++;
      }
    }

    Map<String, CoordinatePoint> centersByAgencyId = new HashMap<String, CoordinatePoint>();

    for (Agency agency : _dao.getAllAgencies()) {

      StopsCenterOfMass stops = stopsByAgencyId.get(agency.getId());

      if (stops == null || stops.count == 0) {
        _log.warn("Agency has no service: " + agency);

      } else {
        double lat = stops.lats / stops.count;
        double lon = stops.lons / stops.count;
        centersByAgencyId.put(agency.getId(), new CoordinatePoint(lat, lon));
      }
    }

    return centersByAgencyId;
  }

  @Cacheable
  public Map<String, CoordinateBounds> getAgencyIdsAndCoverageAreas() {

    Map<String, CoordinateBounds> boundsByAgencyId = new HashMap<String, CoordinateBounds>();

    for (TripEntry trip : _graph.getAllTrips()) {

      AgencyAndId id = trip.getId();
      String agencyId = id.getAgencyId();

      CoordinateBounds bounds = boundsByAgencyId.get(agencyId);

      if (bounds == null) {
        bounds = new CoordinateBounds();
        boundsByAgencyId.put(agencyId, bounds);
      }

      for (StopTimeEntry stopTime : trip.getStopTimes()) {
        StopEntry stop = stopTime.getStop();
        bounds.addPoint(stop.getStopLat(), stop.getStopLon());
      }
    }

    for (Agency agency : _dao.getAllAgencies()) {

      CoordinateBounds bounds = boundsByAgencyId.get(agency.getId());

      if (bounds == null || bounds.isEmpty()) {
        _log.warn("Agency has no service: " + agency);
        boundsByAgencyId.remove(agency.getId());
      }
    }

    return boundsByAgencyId;
  }

  private static class StopsCenterOfMass {
    public double lats = 0;
    public double lons = 0;
    public double count = 0;
  }
}
