package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.List;

public interface GeospatialBeanService {
  
  public List<AgencyAndId> getStopsByLocation(double lat, double lon, double radius);

  public List<AgencyAndId> getStopsByBounds(double lat1, double lon1, double lat2, double lon2);
}
