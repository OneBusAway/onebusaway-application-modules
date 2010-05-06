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
package org.onebusaway.oba.impl;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.onebusaway.oba.web.standard.client.model.TimedStopBean;
import org.onebusaway.oba.web.standard.client.rpc.OneBusAwayService;
import org.onebusaway.where.web.common.client.rpc.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class OneBusAwayServiceServletImpl extends RemoteServiceServlet
    implements OneBusAwayService {

  private static final long serialVersionUID = 1L;

  private OneBusAwayService _service;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    context.getAutowireCapableBeanFactory().autowireBean(this);
  }

  @Autowired
  public void setOneBusAwayService(OneBusAwayService service) {
    _service = service;
  }

  public List<TimedStopBean> getStops(double lat, double lon)
      throws ServiceException {
    return _service.getStops(lat, lon);
  }
}
