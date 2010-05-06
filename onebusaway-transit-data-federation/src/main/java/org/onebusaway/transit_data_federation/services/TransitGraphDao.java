package org.onebusaway.transit_data_federation.services;

import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

public interface TransitGraphDao {

  public Iterable<StopEntry> getAllStops();

  public StopEntry getStopEntryForId(AgencyAndId id);

  public List<StopEntry> getStopsByLocation(CoordinateRectangle bounds);

  public Iterable<TripEntry> getAllTrips();

  public TripEntry getTripEntryForId(AgencyAndId id);

  public List<TripEntry> getTripsForBlockId(AgencyAndId blockId);

  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId);
}
