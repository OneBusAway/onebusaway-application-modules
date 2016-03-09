/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nextbus.model.nextbus.Body;
import org.onebusaway.nextbus.model.nextbus.BodyError;
import org.onebusaway.nextbus.model.nextbus.Direction;
import org.onebusaway.nextbus.model.nextbus.DisplayRoute;
import org.onebusaway.nextbus.model.nextbus.DisplayStop;
import org.onebusaway.nextbus.model.nextbus.Path;
import org.onebusaway.nextbus.model.nextbus.Point;
import org.onebusaway.nextbus.model.nextbus.Route;
import org.onebusaway.nextbus.model.nextbus.Stop;
import org.onebusaway.nextbus.validation.ErrorMsg;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.comparators.AlphanumComparator;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class RouteConfigAction extends NextBusApiBase implements
    ModelDriven<Body<Route>> {

  private String agencyId;

  private String routeId;

  private static int MAX_ROUTES = 100;

  public String getA() {
    return agencyId;
  }

  public void setA(String agencyId) {
    this.agencyId = getMappedAgency(agencyId);
  }

  public String getR() {
    return routeId;
  }

  public void setR(String routeId) {
    this.routeId = _tdsMappingService.getRouteIdFromShortName(routeId);
  }

  public HttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }

  @Override
  public Body<Route> getModel() {

    Body<Route> body = new Body<Route>();

    if (isValid(body)) {

      List<String> agencyIds = processAgencyIds(agencyId);

      List<AgencyAndId> routeIds = new ArrayList<AgencyAndId>();

      List<RouteBean> routeBeans = new ArrayList<RouteBean>();

      int routes_count = 1;

      if (processRouteIds(routeId, routeIds, agencyIds, body)) {
        for (AgencyAndId routeId : routeIds) {
          routeBeans.add(_transitDataService.getRouteForId(routeId.toString()));
        }
      } else if (routeId == null) {
        routeBeans = _transitDataService.getRoutesForAgencyId(agencyId).getList();
      }

      Collections.sort(routeBeans, new Comparator<RouteBean>() {
        AlphanumComparator alphaComparator = new AlphanumComparator();

        public int compare(RouteBean arg0, RouteBean arg1) {
          return alphaComparator.compare(arg0.getId(), arg1.getId());
        }
      });

      for (RouteBean routeBean : routeBeans) {

        // Limit Number of Routes Returned
        if (routes_count > MAX_ROUTES)
          break;

        Route route = new Route();
        route.setTag(getIdNoAgency(routeBean.getId()));
        route.setTitle(route.getTag() + " " + routeBean.getLongName());
        route.setShortTitle(routeBean.getShortName());
        route.setColor(routeBean.getColor());
        route.setOppositeColor(routeBean.getTextColor());

        StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());

        // Stops
        for (StopBean stopBean : stopsForRoute.getStops()) {
          Stop stop = new Stop();
          stop.setTag(getIdNoAgency(stopBean.getId()));
          stop.setTitle(stopBean.getName());
          stop.setLat(stopBean.getLat());
          stop.setLon(stopBean.getLon());
          stop.setStopId(stopBean.getCode());
          route.getStops().add(stop);
        }

        // Directions
        for (StopGroupingBean stopGroupingBean : stopsForRoute.getStopGroupings()) {
          for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
            Direction direction = new Direction();
            direction.setTag(stopGroupBean.getId());
            direction.setTitle(stopGroupBean.getName().getName());
            for (String stopId : stopGroupBean.getStopIds()) {
              direction.getStops().add(new DisplayStop(getIdNoAgency(stopId)));
            }
            route.getDirections().add(direction);
          }
        }

        // PolyLines
        for (EncodedPolylineBean polyline : stopsForRoute.getPolylines()) {
          Path path = new Path();
          List<CoordinatePoint> coordinatePoints = PolylineEncoder.decode(polyline);
          for (CoordinatePoint coordinatePoint : coordinatePoints) {
            path.getPoints().add(
                new Point(coordinatePoint.getLat(), coordinatePoint.getLon()));
          }
          route.getPaths().add(path);
        }

        body.getResponse().add(route);
        routes_count++;
      }
    }
    return body;

  }

  private boolean isValid(Body body) {
    // Validate Agency
    if (!isValidAgency(body, agencyId))
      return false;

    // Validate Undefined Route
    if (getR() == null) {
      body.getErrors().add(
          new BodyError(ErrorMsg.ROUTE_LIMIT.getDescription(), MAX_ROUTES));
      return false;
    }

    // Validate Defined Route
    try {
      AgencyAndId agencyAndRouteId = AgencyAndId.convertFromString(routeId);
      if (!isValidRoute(agencyAndRouteId)) {
        body.getErrors().add(
            new BodyError(ErrorMsg.ROUTE_INVALID.getDescription(), routeId,
                getCachedAgencyBean(agencyId).getName()));
        return false;
      }
    } catch (IllegalArgumentException e) {
      AgencyAndId agencyAndRouteId = new AgencyAndId(agencyId, routeId);
      if (!this.isValidRoute(agencyAndRouteId)) {
        body.getErrors().add(
            new BodyError(ErrorMsg.ROUTE_INVALID.getDescription(), routeId,
                getCachedAgencyBean(agencyId).getName()));
        return false;
      }
    }

    return true;
  }
}
