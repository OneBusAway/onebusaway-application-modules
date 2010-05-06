package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.List;

public interface TripEntry {
  
  public AgencyAndId getId();
  
  public AgencyAndId getRouteId();
  
  public AgencyAndId getRouteCollectionId();
  
  public AgencyAndId getServiceId();
  
  public List<StopTimeEntry> getStopTimes();

  public TripEntry getPrevTrip();

  public TripEntry getNextTrip();
}
