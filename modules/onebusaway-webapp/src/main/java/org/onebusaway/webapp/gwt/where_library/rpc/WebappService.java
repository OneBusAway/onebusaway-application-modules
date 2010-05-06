/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.webapp.gwt.where_library.rpc;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopScheduleBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.model.TripDetailsBean;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.users.client.model.UserBean;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.Date;
import java.util.List;

public interface WebappService extends RemoteService {

  public UserBean getCurrentUser();

  public void setDefaultLocationForUser(String locationName, double lat,
      double lon);

  public RoutesBean getRoutes(RoutesQueryBean query) throws ServiceException;

  public RouteBean getRouteForId(String routeId);

  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException;

  public StopsBean getStopsByBounds(CoordinateBounds bounds, int maxCount)
      throws ServiceException;

  public StopBean getStop(String stopId) throws ServiceException;

  public StopWithArrivalsAndDeparturesBean getArrivalsByStopId(String stopId)
      throws ServiceException;

  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException;

  public TripDetailsBean getTripStatus(String tripId) throws ServiceException;

  public ListBean<TripStatusBean> getTripsForBounds(CoordinateBounds bounds,
      long time);

  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException;

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException;

  public List<TimedPlaceBean> getLocalPathsToStops(String resultId,
      List<LocalSearchResult> localResults) throws ServiceException;

  public void clearCurrentMinTravelTimeResults();
}
