package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.LocationBookmarks;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceBlock;
import org.onebusaway.transit_data_federation.model.predictions.TripTimePrediction;

import java.util.List;

public interface TransitDataFederationDao {

  public LocationBookmarks getBookmarksByUserId(String userId);

  public List<RouteCollection> getAllRouteCollections();

  public RouteCollection getRouteCollectionForId(AgencyAndId id);

  public List<RouteCollection> getRouteCollectionsForStop(Stop stop);
  
  public RouteCollection getRouteCollectionsForRoute(Route route);

  public List<StopSequence> getAllStopSequences();

  public List<StopSequence> getStopSequencesByRoute(Route route);

  public List<StopSequence> getStopSequencesByRouteAndDirectionId(Route route, String directionId);

  public List<StopSequence> getStopSequencesByStop(Stop stop);

  public StopSequence getStopSequenceForTrip(Trip trip);

  public List<StopSequenceBlock> getStopSequenceBlocksByRoute(Route route);

  public List<StopSequenceBlock> getStopSequenceBlocksByStop(Stop stop);
  
  public List<TripTimePrediction> getTripTimePredictionsForTripServiceDateAndTimeRange(AgencyAndId tripId, long serviceDate, long fromTime, long toTime);
}
