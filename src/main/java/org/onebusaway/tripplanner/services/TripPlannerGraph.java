package org.onebusaway.tripplanner.services;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TripPlannerGraph {
  
  public Set<String> getStopIds();
  
  public Collection<String> getTripIds();

  public TripEntry getTripEntryByTripId(String id);

  public StopEntry getStopEntryByStopId(String id);

  public List<String> getStopsByLocation(Geometry boundary);
}