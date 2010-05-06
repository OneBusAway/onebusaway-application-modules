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
package org.onebusaway.where.web.common.impl;

import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.web.common.client.model.NameTreeBean;
import org.onebusaway.where.web.common.client.model.NearbyRoutesBean;
import org.onebusaway.where.web.common.client.model.StopScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.model.TripStatusBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchRouteServiceException;
import org.onebusaway.where.web.common.client.rpc.WhereService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class WhereServiceServletImpl extends RemoteServiceServlet implements WhereService {

  private static final long serialVersionUID = 1L;

  private WhereService _service;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  public void setOneBusAwayService(WhereService service) {
    _service = service;
  }

  public StopsBean getStopsByLocationAndAccuracy(double lat, double lon, int accuracy) throws ServiceException {
    return _service.getStopsByLocationAndAccuracy(lat, lon, accuracy);
  }

  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2, double lon2) throws ServiceException {
    return _service.getStopsByBounds(lat1, lon1, lat2, lon2);
  }

  public StopBean getStop(String stopId) throws ServiceException {
    return _service.getStop(stopId);
  }

  public StopWithArrivalsBean getArrivalsByStopId(String stopId) throws ServiceException {
    return _service.getArrivalsByStopId(stopId);
  }

  public NameTreeBean getStopByRoute(String route, List<Integer> selection) throws ServiceException {
    return _service.getStopByRoute(route, selection);
  }

  public List<StopSequenceBlockBean> getStopSequenceBlocksByRoute(String route) throws ServiceException {
    return _service.getStopSequenceBlocksByRoute(route);
  }

  public List<StopSequenceBean> getStopSequencesByRoute(String route) throws NoSuchRouteServiceException {
    return _service.getStopSequencesByRoute(route);
  }

  public StopScheduleBean getScheduleForStop(String stopId, Date date) throws ServiceException {
    return _service.getScheduleForStop(stopId, date);
  }

  public TripStatusBean getTripStatus(String tripId) throws ServiceException {
    return _service.getTripStatus(tripId);
  }

  public NearbyRoutesBean getNearbyRoutes(String stopId, double nearbyRouteSearchDistance) throws ServiceException {
    return _service.getNearbyRoutes(stopId, nearbyRouteSearchDistance);
  }

  public StopBean getStop(String stopId, double nearbyStopSearchDistance) throws ServiceException {
    return _service.getStop(stopId, nearbyStopSearchDistance);
  }
}
