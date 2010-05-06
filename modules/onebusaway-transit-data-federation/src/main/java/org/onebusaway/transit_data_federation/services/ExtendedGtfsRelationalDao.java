package org.onebusaway.transit_data_federation.services;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ExtendedGtfsRelationalDao extends GtfsRelationalDao {

  public Map<String, CoordinateBounds> getAgencyIdsAndBounds();
  
  public List<Route> getRoutesForStop(Stop stop);
  
  public List<Trip> getTripsForBlockId(AgencyAndId blockIds);
  
  public List<StopTime> getStopTimesForBlockId(AgencyAndId blockId);

  public List<AgencyAndId> getShapePointIdsForRoutes(Collection<Route> routes);
}
