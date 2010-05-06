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
package org.onebusaway.tripplanner.web.common.impl;

import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlannerConstraintsBean;
import org.onebusaway.tripplanner.web.common.client.rpc.TripPlannerWebService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class TripPlannerWebServiceServletImpl extends RemoteServiceServlet implements TripPlannerWebService {

  private static final long serialVersionUID = 1L;

  private TripPlannerWebService _service;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  public void setTripPlannerWebService(TripPlannerWebService service) {
    _service = service;
  }

  public List<TripBean> getTripsBetween(double latFrom, double lonFrom, double latTo, double lonTo,
      TripPlannerConstraintsBean constraints) throws ServiceException {
    return _service.getTripsBetween(latFrom, lonFrom, latTo, lonTo, constraints);
  }
}
