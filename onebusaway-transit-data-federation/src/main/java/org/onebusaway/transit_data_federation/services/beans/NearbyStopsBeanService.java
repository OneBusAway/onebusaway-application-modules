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
package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.StopBean;

import java.util.List;

/**
 * Service for explicity finding stops near a target stop.
 * 
 * @author bdferris
 */
public interface NearbyStopsBeanService {

  /**
   * @param stopBean the target stop to search for nearby stops
   * @param radius the search radius in meters around the stop location
   * @return ids of stops within the radius of the target stop (but excludes the
   *         target stop id itself)
   */
  public List<AgencyAndId> getNearbyStops(StopBean stopBean, double radius);
}
