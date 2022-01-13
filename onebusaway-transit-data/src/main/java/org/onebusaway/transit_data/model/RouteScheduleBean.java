/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data.model;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.trips.TripBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Schedule info for a route.  Inspired by StopRouteScheduleBean.
 *
 * Ultimate goal of:
 *   "entry": {
 *       "routeId": "40_100479",
 *       "serviceIds": ["SERVICEIDVALUE1","SERVICEIDVALUE2"],
 *       "scheduleDate": 1609315200,
 *       "stopTripGroupings": [
 *         {
 *           "directionId": 0,
 *           "tripHeadsign": "University of Washington Station",
 *           "stopIds": ["STOPID1", "STOPID2"],
 *           "tripIds": ["TRIPID1", "TRIPID2"]
 *         },
 *         {
 *           "directionId": 1,
 *           "tripHeadsign": "Angle Lake Station",
 *           "stopIds": ["STOPID2", "STOPID3"],
 *           "tripIds": ["TRIPID3", "TRIPID4"]
 *         }
 *       ]
 *     },
 *     "references": {
 *       "agencies": [.....],
 *       "routes": [.....],
 *       "situations": [.....],
 *       "stops": [.....],
 *       "trips": [.....]
 *     },
 */
public class RouteScheduleBean extends ApplicationBean {

  private static final long serialVersionUID = 1L;

  private AgencyAndId routeId;
  private List<AgencyAndId> serviceIds;
  private ServiceDate scheduleDate;
  private boolean outOfServiceBounds;
  private List<AgencyBean> agencies = new ArrayList<>();
  private List<RouteBean> routes = new ArrayList<>();
  private List<TripBean> trips = new ArrayList<>();
  private List<StopBean> stops = new ArrayList<>();
  private List<StopTimeInstanceBeanExtendedWithStopId> stopTimes = new ArrayList<>();
  private List<StopsAndTripsForDirectionBean> stopTripDirections = new ArrayList<>();
  private List<ServiceAlertBean> serviceAlertBeans = new ArrayList<>();

  public AgencyAndId getRouteId() {
    return routeId;
  }

  public void setRouteId(AgencyAndId routeId) {
    this.routeId = routeId;
  }

  public List<AgencyAndId> getServiceIds() {
    return serviceIds;
  }

  public void setServiceIds(List<AgencyAndId> serviceIds) {
    this.serviceIds = serviceIds;
  }

  public ServiceDate getScheduleDate() {
    return scheduleDate;
  }

  public void setScheduleDate(ServiceDate scheduleDate) {
    this.scheduleDate = scheduleDate;
  }

  public List<StopsAndTripsForDirectionBean> getStopTripDirections() {
    return stopTripDirections;
  }
  public List<AgencyBean> getAgencies() {
    return agencies;
  }
  public List<RouteBean> getRoutes() {
    return routes;
  }
  public List<TripBean> getTrips() {
    return trips;
  }
  public List<StopBean> getStops() {
    return stops;
  }
  public List<StopTimeInstanceBeanExtendedWithStopId> getStopTimes() {
    return stopTimes;
  }

  public void setServiceAlerts(List<ServiceAlertBean> serviceAlerts) {
    serviceAlertBeans = serviceAlerts;
  }

  public List<ServiceAlertBean> getServiceAlerts() {
    return serviceAlertBeans;
  }

  public boolean getOutOfServiceBounds() {return outOfServiceBounds;}

  public void setOutOfServiceBounds(boolean outOfServiceBounds){this.outOfServiceBounds = outOfServiceBounds;}
}
