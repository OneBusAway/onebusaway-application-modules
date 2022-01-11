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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.presentation.impl.service_alerts.ServiceAlertsHelper;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.api.model.RouteAtStop;
import org.onebusaway.enterprise.webapp.actions.api.model.RouteDirection;
import org.onebusaway.enterprise.webapp.actions.api.model.StopResult;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.Siri;
import uk.org.siri.siri.StopMonitoringDeliveryStructure;

public class StopForIdAction extends OneBusAwayEnterpriseActionSupport {
    
  private static final long serialVersionUID = 1L;

  @Autowired
  private RealtimeService _realtimeService;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private ConfigurationService _configService;

  private ObjectMapper _mapper = new ObjectMapper();    

  private ServiceAlertsHelper _serviceAlertsHelper = new ServiceAlertsHelper();

  private Siri _response = null;

  private StopResult _result = null;
  
  private String _stopId = null;

  public void setStopId(String stopId) {
    _stopId = stopId;
  }

  private static Logger _log = LoggerFactory.getLogger(StopForIdAction.class);

  
  @Override
  public String execute() {
    try {
      if (_stopId == null) {
        return SUCCESS;
      }

      boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
      StopBean stop;

      if (serviceDateFilterOn) {
        stop = _transitDataService.getStopForServiceDate(_stopId, new ServiceDate(new Date(SystemTime.currentTimeMillis())));
      } else {
        stop = _transitDataService.getStop(_stopId);
      }

      if (stop == null) {
        return SUCCESS;
      }

      List<RouteAtStop> routesAtStop = new ArrayList<RouteAtStop>();

      for (RouteBean routeBean : stop.getRoutes()) {
        StopsForRouteBean stopsForRoute;
        if (serviceDateFilterOn) {
          stopsForRoute = _transitDataService.getStopsForRouteForServiceDate(routeBean.getId(), new ServiceDate(new Date(SystemTime.currentTimeMillis())));
        } else {
          stopsForRoute = _transitDataService.getStopsForRoute(routeBean.getId());
        }

        List<RouteDirection> routeDirections = new ArrayList<RouteDirection>();
        List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
        for (StopGroupingBean stopGroupingBean : stopGroupings) {
          for (StopGroupBean stopGroupBean : stopGroupingBean.getStopGroups()) {
            if (_transitDataService.stopHasRevenueServiceOnRoute((routeBean.getAgency() != null ? routeBean.getAgency().getId() : null),
                _stopId, routeBean.getId(), stopGroupBean.getId())) {

              NameBean name = stopGroupBean.getName();
              String type = name.getType();

              if (!type.equals("destination"))
                continue;

              // filter out route directions that don't stop at this stop
              if (!stopGroupBean.getStopIds().contains(_stopId))
                continue;

              Boolean hasUpcomingScheduledService =
                  _transitDataService.stopHasUpcomingScheduledService((routeBean.getAgency() != null ? routeBean.getAgency().getId() : null), SystemTime.currentTimeMillis(), stop.getId(),
                      routeBean.getId(), stopGroupBean.getId());

              // if there are buses on route, always have "scheduled service"
              Boolean routeHasVehiclesInService = true;
              //_realtimeService.getVehiclesInServiceForStopAndRoute(stop.getId(), routeBean.getId(), SystemTime.currentTimeMillis());

              if (routeHasVehiclesInService) {
                hasUpcomingScheduledService = true;
              }

              routeDirections.add(new RouteDirection(stopGroupBean, null, null, hasUpcomingScheduledService));
            }
          }
        }

        RouteAtStop routeAtStop = new RouteAtStop(routeBean, routeDirections);
        routesAtStop.add(routeAtStop);
      }

      _result = new StopResult(stop, routesAtStop);

      List<MonitoredStopVisitStructure> visits =
          _realtimeService.getMonitoredStopVisitsForStop(_stopId, 0, SystemTime.currentTimeMillis());

      _response = generateSiriResponse(visits, AgencyAndIdLibrary.convertFromString(_stopId));

      return SUCCESS;
    } catch (Exception e) {
      _log.error("Error processing stop for id action: ", e);
    }
    return SUCCESS;
  }   
  
  private Siri generateSiriResponse(List<MonitoredStopVisitStructure> visits, AgencyAndId stopId) {
    
    List<AgencyAndId> stopIds = new ArrayList<AgencyAndId>();
    if (stopId != null) stopIds.add(stopId);
    
    ServiceDelivery serviceDelivery = new ServiceDelivery();
    try {
      StopMonitoringDeliveryStructure stopMonitoringDelivery = new StopMonitoringDeliveryStructure();
      stopMonitoringDelivery.setResponseTimestamp(new Date(getTime()));
      
      Calendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTimeInMillis(getTime());
      gregorianCalendar.add(Calendar.MINUTE, 1);
      stopMonitoringDelivery.setValidUntil(gregorianCalendar.getTime());
      
      stopMonitoringDelivery.getMonitoredStopVisit().addAll(visits);

      serviceDelivery.setResponseTimestamp(new Date(getTime()));
      serviceDelivery.getStopMonitoringDelivery().add(stopMonitoringDelivery);

      _serviceAlertsHelper.addSituationExchangeToSiriForStops(serviceDelivery, visits, _transitDataService, stopIds);
      _serviceAlertsHelper.addGlobalServiceAlertsToServiceDelivery(serviceDelivery, _realtimeService);
    } catch (RuntimeException e) {
      throw e;
    }

    Siri siri = new Siri();
    siri.setServiceDelivery(serviceDelivery);
    
    return siri;
  }
  
  /** 
   * VIEW METHODS
   */
  public String getStopMonitoring() {
    try {
      return _realtimeService.getSiriJsonSerializer().getJson(_response, null);
    } catch(Exception e) {
      return e.getMessage();
    }
  }
  
  public String getStopMetadata() throws Exception {
    return _mapper.writeValueAsString(_result);
  }

}
