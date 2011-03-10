package org.onebusaway.transit_data_federation.services;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.Agency;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import java.util.Map;
import java.util.TimeZone;

/**
 * Methods for querying {@link Agency} information.
 * 
 * @author bdferris
 * 
 */
public interface AgencyService {

  /**
   * See {@link Agency#getTimezone()}.
   * 
   * @param agencyId the id of the target agency
   * @return the instantiated timezone object for the specified agencyId
   */
  public TimeZone getTimeZoneForAgencyId(String agencyId);

  /**
   * For each agency in a transit bundle, computes the center point of all the
   * stops operated by that agency.
   * 
   * @return the center point of all stops operated by each agency, keyed by
   *         agency id
   */
  public Map<String, CoordinatePoint> getAgencyIdsAndCenterPoints();

  /**
   * For each agency in a transit bundle, computes the coordinate bounds of all
   * the stops operated by that agency.
   * 
   * @return the coordinate bounds of all stops operated by each agency, keyed
   *         by agency id
   */
  public Map<String, CoordinateBounds> getAgencyIdsAndCoverageAreas();
}
