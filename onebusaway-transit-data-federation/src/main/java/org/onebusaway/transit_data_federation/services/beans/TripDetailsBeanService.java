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
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;

/**
 * Service methods for querying the real-time status and position of a
 * particular trip.
 * 
 * @author bdferris
 * @see TripDetailsBean
 */
public interface TripDetailsBeanService {

  /**
   * There are potential multiple matching trips for this method. In such a
   * case, we'll return the trip with the first vehicle id, as determined by
   * string sorted order.
   * 
   * @param query
   * @return trip details for the trip matching the specified query criteria
   *         (tripId, service, and optional vehicleId), or null if none found
   */
  public TripDetailsBean getTripForId(TripDetailsQueryBean query);

  /**
   * 
   * @param query
   * @return trip details for the trips matching the specified query criteria
   *         (tripId, service, and optional vehicleId)
   */
  public ListBean<TripDetailsBean> getTripsForId(TripDetailsQueryBean query);

  /**
   * 
   * @param vehicleId
   * @param time
   * @param tripDetailsInclusionBean controls what will be included in the
   *          response
   * @return trip details for the trip operated by the specified vehicle at the
   *         specified time, or null if not found
   */
  public TripDetailsBean getTripForVehicle(AgencyAndId vehicleId, long time,
      TripDetailsInclusionBean tripDetailsInclusionBean);

  /**
   * @param query
   * @return the list of active trips matching the query criteria
   */
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query);

  /**
   * 
   * @param query
   * @return the list of active trips matching the route query criteria
   */
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query);

  /**
   * 
   * @param query
   * @return the list of active trips matching agency query criteria
   */
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query);

  /**
   * Convert a {@link BlockLocation} object into a trip status bean
   * @param blockLocation
   * @param time TODO
   * @return a trip status bean representation of a block location object
   */
  public TripStatusBean getBlockLocationAsStatusBean(BlockLocation blockLocation, long time);

}
