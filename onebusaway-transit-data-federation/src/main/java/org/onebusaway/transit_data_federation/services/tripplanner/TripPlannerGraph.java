package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import java.util.List;

public interface TripPlannerGraph {
  
  public Iterable<StopEntry> getAllStops();
 
  public Iterable<TripEntry> getAllTrips();
  
  public List<StopEntry> getStopsByLocation(CoordinateRectangle bounds);
  
  public TripEntry getTripEntryForId(AgencyAndId id);
  
  public StopEntry getStopEntryForId(AgencyAndId id);
  
  public List<TripEntry> getTripsForBlockId(AgencyAndId blockId);
}