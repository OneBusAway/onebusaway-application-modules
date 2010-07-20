package org.onebusaway.transit_data_federation.services;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.LocationBookmarks;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceBlock;

public interface TransitDataFederationDao {

  public LocationBookmarks getBookmarksByUserId(String userId);

  public List<RouteCollection> getAllRouteCollections();

  public RouteCollection getRouteCollectionForId(AgencyAndId id);

  public List<RouteCollection> getRouteCollectionsForStop(Stop stop);
  
  public List<AgencyAndId> getRouteCollectionIdsForServiceId(AgencyAndId serviceId);
  
  public List<AgencyAndId> getTripIdsForServiceIdAndRouteCollectionId(AgencyAndId serviceId, AgencyAndId routeCollectionId);
  
  public RouteCollection getRouteCollectionForRoute(Route route);

  public List<StopSequence> getAllStopSequences();

  public List<StopSequence> getStopSequencesByRoute(Route route);

  public List<StopSequence> getStopSequencesByRouteAndDirectionId(Route route, String directionId);

  public List<StopSequence> getStopSequencesByStop(Stop stop);

  public StopSequence getStopSequenceForTrip(Trip trip);

  public List<StopSequenceBlock> getStopSequenceBlocksByRoute(Route route);

  public List<StopSequenceBlock> getStopSequenceBlocksByStop(Stop stop);
  
  public List<AgencyAndId> getShapeIdsForStop(Stop stop);
}
