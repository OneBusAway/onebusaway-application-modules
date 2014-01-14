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
package org.onebusaway.webapp.gwt.where_library.rpc;

import java.util.Date;
import java.util.List;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesAndStopsBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.users.client.model.UserBean;

import com.google.gwt.user.client.rpc.RemoteService;

public interface WebappService extends RemoteService {

  public UserBean getCurrentUser();

  public UserBean setDefaultLocationForUser(String locationName, double lat,
      double lon);

  public UserBean clearDefaultLocationForUser();

  public List<AgencyWithCoverageBean> getAgencies() throws ServiceException;

  public RoutesAndStopsBean getRoutesAndStops(SearchQueryBean query)
      throws ServiceException;

  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException;

  public RouteBean getRouteForId(String routeId) throws ServiceException;

  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException;

  public StopsBean getStops(SearchQueryBean query) throws ServiceException;

  public StopBean getStop(String stopId) throws ServiceException;

  public StopWithArrivalsAndDeparturesBean getArrivalsByStopId(String stopId)
      throws ServiceException;

  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException;

  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query);

  public ItinerariesBean getTripsBetween(CoordinatePoint from,
      CoordinatePoint to, long time, ConstraintsBean constraints)
      throws ServiceException;

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException;

  public List<TimedPlaceBean> getLocalPathsToStops(
      ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException;
}
