package org.onebusaway.transit_data_federation.services;

import org.onebusaway.geospatial.model.CoordinateBounds;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.util.Map;

public interface AgencyService {
  public Map<String,CoordinatePoint> getAgencyIdsAndCenterPoints();
  public Map<String,CoordinateBounds> getAgencyIdsAndCoverageAreas();
}
