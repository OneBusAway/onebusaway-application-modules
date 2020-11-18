/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.presentation.services.search.SearchResultFactory;
import org.onebusaway.presentation.services.search.SearchService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.enterprise.webapp.actions.api.model.GeocodeResult;
import org.onebusaway.enterprise.webapp.actions.api.model.RouteAtStop;
import org.onebusaway.enterprise.webapp.actions.api.model.RouteDirection;
import org.onebusaway.enterprise.webapp.actions.api.model.RouteInRegionResult;
import org.onebusaway.enterprise.webapp.actions.api.model.RouteResult;
import org.onebusaway.enterprise.webapp.actions.api.model.StopResult;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.util.services.configuration.ConfigurationService;

public class SearchResultFactoryImpl implements SearchResultFactory {

  private SearchService _searchService;

  private TransitDataService _transitDataService;

  private RealtimeService _realtimeService;

  private ConfigurationService _configService;

  public SearchResultFactoryImpl(SearchService searchService, TransitDataService transitDataService, RealtimeService realtimeService, ConfigurationService configService) {
    _searchService = searchService;
    _transitDataService = transitDataService;
    _realtimeService = realtimeService;
    _configService = configService;
  }

  @Override
  public SearchResult getRouteResultForRegion(RouteBean routeBean) {
    List<String> polylines = new ArrayList<String>();

    ServiceDate serviceDate = null;
    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    if (serviceDateFilterOn) serviceDate = new ServiceDate(new Date(SystemTime.currentTimeMillis()));

    StopsForRouteBean stopsForRoute;
    if (serviceDate == null)
      stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());
    else
      stopsForRoute = _transitDataService.getStopsForRouteForServiceDate(routeBean.getId(), serviceDate);

    List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
    for (StopGroupingBean stopGroupingBean : stopGroupings) {
      for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
        NameBean name = stopGroupBean.getName();
        String type = name.getType();

        if (!type.equals("destination"))
          continue;

        for(EncodedPolylineBean polyline : stopGroupBean.getPolylines()) {
          polylines.add(polyline.getPoints());
        }
      }
    }

    return new RouteInRegionResult(routeBean, polylines);
  }

  @Override
  public SearchResult getRouteResult(RouteBean routeBean) {
    List<RouteDirection> directions = new ArrayList<RouteDirection>();

    ServiceDate serviceDate = null;
    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    if (serviceDateFilterOn) serviceDate = new ServiceDate(new Date(SystemTime.currentTimeMillis()));

    StopsForRouteBean stopsForRoute;
    if (serviceDate != null) {
      stopsForRoute = _transitDataService.getStopsForRouteForServiceDate(routeBean.getId(), serviceDate);
    }
    else {
      stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());
    }

    List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
    for (StopGroupingBean stopGroupingBean : stopGroupings) {
      for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
        NameBean name = stopGroupBean.getName();
        String type = name.getType();

        if (!type.equals("destination"))
          continue;

        List<String> polylines = new ArrayList<String>();
        for(EncodedPolylineBean polyline : stopGroupBean.getPolylines()) {
          polylines.add(polyline.getPoints());
        }

        Boolean hasUpcomingScheduledService =
            _transitDataService.routeHasUpcomingScheduledService((routeBean.getAgency()!=null?routeBean.getAgency().getId():null), SystemTime.currentTimeMillis(), routeBean.getId(), stopGroupBean.getId());

        // if there are buses on route, always have "scheduled service"
        Boolean routeHasVehiclesInService =
      		  _realtimeService.getVehiclesInServiceForRoute(routeBean.getId(), stopGroupBean.getId(), SystemTime.currentTimeMillis());

        if(routeHasVehiclesInService) {
      	  hasUpcomingScheduledService = true;
        }

        directions.add(new RouteDirection(stopGroupBean, polylines, null, hasUpcomingScheduledService));
      }
    }

    return new RouteResult(routeBean, directions, stopsForRoute);
  }

  @Override
  public SearchResult getStopResult(StopBean stopBean, Set<RouteBean> routeFilter) {
    List<RouteAtStop> routesAtStop = new ArrayList<RouteAtStop>();

    List<StopsForRouteBean> fullStopList = new ArrayList<>();

    ServiceDate serviceDate = null;
    boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
    if (serviceDateFilterOn) serviceDate = new ServiceDate(new Date(SystemTime.currentTimeMillis()));

    for(RouteBean routeBean : stopBean.getRoutes()) {
      StopsForRouteBean stopsForRoute;
      if (serviceDate != null) {
        stopsForRoute = _transitDataService.getStopsForRouteForServiceDate(routeBean.getId(), serviceDate);
      }
      else {
        stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());
      }

      fullStopList.add(stopsForRoute);

      List<RouteDirection> directions = new ArrayList<RouteDirection>();
      List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
      for (StopGroupingBean stopGroupingBean : stopGroupings) {
        for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
          NameBean name = stopGroupBean.getName();
          String type = name.getType();

          if (!type.equals("destination"))
            continue;

          List<String> polylines = new ArrayList<String>();
          for(EncodedPolylineBean polyline : stopGroupBean.getPolylines()) {
            polylines.add(polyline.getPoints());
          }

          Boolean hasUpcomingScheduledService = null;

          // Only set hasUpcomingScheduledService if the current stopGroupBean (direction) contains the current stop.
          // In other words, only if the stop in question is served in the current direction.
          // We do this to prevent checking if there is service in a direction that does not even serve this stop.
          if (stopGroupBean.getStopIds().contains(stopBean.getId())) {
            hasUpcomingScheduledService =
                _transitDataService.stopHasUpcomingScheduledService((routeBean.getAgency()!=null?routeBean.getAgency().getId():null), SystemTime.currentTimeMillis(), stopBean.getId(),
                    routeBean.getId(), stopGroupBean.getId());

            // if there are buses on route, always have "scheduled service"
            Boolean routeHasVehiclesInService =
                _realtimeService.getVehiclesInServiceForStopAndRoute(stopBean.getId(), routeBean.getId(), SystemTime.currentTimeMillis());

            if(routeHasVehiclesInService) {
              hasUpcomingScheduledService = true;
            }
          }

          directions.add(new RouteDirection(stopGroupBean, polylines, null, hasUpcomingScheduledService));
        }
      }

      RouteAtStop routeAtStop = new RouteAtStop(routeBean, directions);
      routesAtStop.add(routeAtStop);
    }
    return new StopResult(stopBean, routesAtStop, fullStopList);
  }

  @Override
  public SearchResult getGeocoderResult(EnterpriseGeocoderResult geocodeResult, Set<RouteBean> routeBean) {
    List<SearchResult> routesNearby = null;

    if(geocodeResult.isRegion()) {
       routesNearby = _searchService.findRoutesStoppingWithinRegion(geocodeResult.getBounds(), this).getMatches();
    } else {
      routesNearby = _searchService.findRoutesStoppingNearPoint(geocodeResult.getLatitude(),
          geocodeResult.getLongitude(), this).getMatches();
    }

    return new GeocodeResult(geocodeResult, routesNearby);
  }

}
