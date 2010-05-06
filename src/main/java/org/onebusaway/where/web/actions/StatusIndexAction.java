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
package org.onebusaway.where.web.actions;

import org.onebusaway.common.web.common.client.model.RouteBean;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.where.services.StatusService;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.washington.cs.rse.text.NaturalStringOrder;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class StatusIndexAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private static SortByRoute _sort = new SortByRoute();

  @Autowired
  private StatusService _statusService;

  private List<RouteBean> _cancelledRoutes = new ArrayList<RouteBean>();
  
  private List<RouteBean> _reroutedRoutes = new ArrayList<RouteBean>();

  public List<RouteBean> getCancelledRoutes() {
    return _cancelledRoutes;
  }
  
  public List<RouteBean> getReroutedRoutes() {
    return _reroutedRoutes;
  }

  @Override
  public String execute() throws ServiceException {

    Set<Route> cancelledRoutes = _statusService.getCancelledRoutes();
    for (Route route : cancelledRoutes)
      _cancelledRoutes.add(getRouteAsBean(route));
    
    Set<Route> reroutedRoutes = _statusService.getReroutedRoutes();
    for (Route route : reroutedRoutes)
      _reroutedRoutes.add(getRouteAsBean(route));

    Collections.sort(_cancelledRoutes, _sort);
    Collections.sort(_reroutedRoutes, _sort);
    

    return SUCCESS;
  }

  private RouteBean getRouteAsBean(Route route) {
    RouteBean bean = new RouteBean();
    bean.setId(route.getId());
    bean.setNumber(route.getShortName());
    return bean;
  }

  private static class SortByRoute implements Comparator<RouteBean> {

    public int compare(RouteBean o1, RouteBean o2) {
      return NaturalStringOrder.compareNatural(o1.getNumber(), o2.getNumber());
    }

  }
}
