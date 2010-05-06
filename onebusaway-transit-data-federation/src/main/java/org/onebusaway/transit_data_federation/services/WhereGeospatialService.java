package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import java.util.List;

public interface WhereGeospatialService {
  
  public List<AgencyAndId> getStopsByLocation(CoordinatePoint location, double radius);

  public List<AgencyAndId> getStopsByLocation(double lat, double lon, double radius);

  public List<AgencyAndId> getStopsByBounds(CoordinateRectangle bounds);
}
