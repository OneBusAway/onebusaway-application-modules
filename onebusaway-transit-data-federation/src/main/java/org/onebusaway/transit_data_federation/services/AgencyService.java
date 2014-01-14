/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.services;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.Agency;

/**
 * Methods for querying {@link Agency} information.
 * 
 * @author bdferris
 * 
 */
public interface AgencyService {
  
  public List<String> getAllAgencyIds();

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
