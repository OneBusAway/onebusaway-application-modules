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
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.trips.TripBean;

/**
 * Service methods to lookup a {@link TripBean} representation of a {@link Trip}
 * object.
 * 
 * @author bdferris
 * @see TripBean
 * @see Trip
 */
public interface TripBeanService {

  /**
   * @param tripId see {@link Trip#getId()}
   * @return retrieve a bean representation of the specified trip, or null if
   *         not found
   */
  public TripBean getTripForId(AgencyAndId tripId);
}
