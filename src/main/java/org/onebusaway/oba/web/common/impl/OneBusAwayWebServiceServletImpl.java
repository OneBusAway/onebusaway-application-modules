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
package org.onebusaway.oba.web.common.impl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.oba.web.common.client.model.MinTransitTimeResult;
import org.onebusaway.oba.web.common.client.model.TimedPlaceBean;
import org.onebusaway.oba.web.common.client.rpc.OneBusAwayWebService;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class OneBusAwayWebServiceServletImpl extends RemoteServiceServlet implements OneBusAwayWebService {

  private static final long serialVersionUID = 1L;

  private OneBusAwayWebService _service;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  public void setOneBusAwayService(OneBusAwayWebService service) {
    _service = service;
  }

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat, double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException {
    return _service.getMinTravelTimeToStopsFrom(lat, lon, constraints);
  }

  public List<TimedPlaceBean> getLocalPaths(String resultId, List<LocalSearchResult> localResults)
      throws ServiceException {
    return _service.getLocalPaths(resultId, localResults);
  }

  public TripBean getTripPlan(String resultId, TimedPlaceBean destination) throws ServiceException {
    return _service.getTripPlan(resultId, destination);
  }

}
