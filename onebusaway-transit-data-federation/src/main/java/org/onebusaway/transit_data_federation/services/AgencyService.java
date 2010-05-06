package org.onebusaway.transit_data_federation.services;

import org.onebusaway.geospatial.model.CoordinateBounds;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.util.Map;
import java.util.TimeZone;

public interface AgencyService {
  public TimeZone getTimeZoneForAgencyId(String agencyId);
  public Map<String,CoordinatePoint> getAgencyIdsAndCenterPoints();
  public Map<String,CoordinateBounds> getAgencyIdsAndCoverageAreas();
}
