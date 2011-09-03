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
package org.onebusaway.webapp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

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
import org.onebusaway.webapp.gwt.where_library.rpc.WebappService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;

public class WebappServiceServletImpl extends RemoteServiceServlet implements
    WebappService {

  private static final long serialVersionUID = 1L;

  private WebappService _service;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  protected SerializationPolicy doGetSerializationPolicy(
      HttpServletRequest request, String moduleBaseURL, String strongName) {

    return customLoadSerializationPolicy(this, request, moduleBaseURL,
        strongName);
    // return super.doGetSerializationPolicy(request, moduleBaseURL,
    // strongName);
  }

  @Override
  public List<AgencyWithCoverageBean> getAgencies() throws ServiceException {
    return _service.getAgencies();
  }

  @Autowired
  public void setService(WebappService service) {
    _service = service;
  }

  @Override
  public StopWithArrivalsAndDeparturesBean getArrivalsByStopId(String stopId)
      throws ServiceException {
    return _service.getArrivalsByStopId(stopId);
  }

  @Override
  public UserBean getCurrentUser() {
    return _service.getCurrentUser();
  }

  @Override
  public UserBean setDefaultLocationForUser(String locationName, double lat,
      double lon) {
    return _service.setDefaultLocationForUser(locationName, lat, lon);
  }

  @Override
  public UserBean clearDefaultLocationForUser() {
    return _service.clearDefaultLocationForUser();
  }

  @Override
  public MinTransitTimeResult getMinTravelTimeToStopsFrom(
      CoordinatePoint location, long time,
      TransitShedConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException {
    return _service.getMinTravelTimeToStopsFrom(location, time, constraints,
        timeSegmentSize);
  }

  @Override
  public RouteBean getRouteForId(String routeId) throws ServiceException {
    return _service.getRouteForId(routeId);
  }

  @Override
  public RoutesBean getRoutes(SearchQueryBean query) throws ServiceException {
    return _service.getRoutes(query);
  }

  @Override
  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException {
    return _service.getScheduleForStop(stopId, date);
  }

  @Override
  public RoutesAndStopsBean getRoutesAndStops(SearchQueryBean query)
      throws ServiceException {
    return _service.getRoutesAndStops(query);
  }

  @Override
  public StopsBean getStops(SearchQueryBean query) throws ServiceException {
    return _service.getStops(query);
  }

  @Override
  public StopBean getStop(String stopId) throws ServiceException {
    return _service.getStop(stopId);
  }

  @Override
  public StopsForRouteBean getStopsForRoute(String routeId)
      throws ServiceException {
    return _service.getStopsForRoute(routeId);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    return _service.getTripsForBounds(query);
  }


  @Override
  public ItinerariesBean getTripsBetween(CoordinatePoint from,
      CoordinatePoint to, long time, ConstraintsBean constraints)
      throws ServiceException {
    return _service.getTripsBetween(from, to, time, constraints);
  }

  @Override
  public List<TimedPlaceBean> getLocalPathsToStops(ConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {
    return _service.getLocalPathsToStops(constraints, travelTimes, localResults);
  }

  /****
   * 
   ****/

  /**
   * Used by HybridServiceServlet.
   */
  private static SerializationPolicy customLoadSerializationPolicy(
      HttpServlet servlet, HttpServletRequest request, String moduleBaseURL,
      String strongName) {

    // The request can tell you the path of the web app relative to the
    // container root.
    String contextPath = request.getContextPath();

    String modulePath = null;
    if (moduleBaseURL != null) {
      try {
        modulePath = new URL(moduleBaseURL).getPath();
      } catch (MalformedURLException ex) {
        // log the information, we will default
        servlet.log("Malformed moduleBaseURL: " + moduleBaseURL, ex);
      }
    }

    SerializationPolicy serializationPolicy = null;

    /*
     * Check that the module path must be in the same web app as the servlet
     * itself. If you need to implement a scheme different than this, override
     * this method.
     */
    if (modulePath == null) {
      String message = "ERROR: The module path requested, "
          + modulePath
          + ", is not in the same web application as this servlet, "
          + contextPath
          + ".  Your module may not be properly configured or your client and server code maybe out of date.";
      servlet.log(message, null);
    } else {

      // TODO : Hack for Nokia demo
      // /Users/bdferris/Documents/Aptana%20Studio%20Workspace/nokia-hello-world/
      if (modulePath.endsWith("nokia-hello-world/"))
        modulePath = "/where/mobile/";

      if (!modulePath.startsWith(contextPath))
        modulePath = contextPath + modulePath;

      // Strip off the context path from the module base URL. It should be a
      // strict prefix.
      String contextRelativePath = modulePath.substring(contextPath.length());

      String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(contextRelativePath
          + strongName);

      // Open the RPC resource file and read its contents.
      InputStream is = servlet.getServletContext().getResourceAsStream(
          serializationPolicyFilePath);
      try {
        if (is != null) {
          try {
            serializationPolicy = SerializationPolicyLoader.loadFromStream(is,
                null);
          } catch (ParseException e) {
            servlet.log("ERROR: Failed to parse the policy file '"
                + serializationPolicyFilePath + "'", e);
          } catch (IOException e) {
            servlet.log("ERROR: Could not read the policy file '"
                + serializationPolicyFilePath + "'", e);
          }
        } else {
          String message = "ERROR: The serialization policy file '"
              + serializationPolicyFilePath
              + "' was not found; did you forget to include it in this deployment?";
          servlet.log(message, null);
        }
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
            // Ignore this error
          }
        }
      }
    }

    return serializationPolicy;
  }

}
