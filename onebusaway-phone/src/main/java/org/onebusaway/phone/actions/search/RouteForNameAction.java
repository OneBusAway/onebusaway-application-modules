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
package org.onebusaway.phone.actions.search;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.phone.actions.AbstractAction;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;

public class RouteForNameAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _routeName;

  private RouteBean _route;

  private List<RouteBean> _routes;
  
  public void setRouteName(String routeName) {
    _routeName = routeName;
  }

  public String getRouteName() {
    return _routeName;
  }

  public RouteBean getRoute() {
    return _route;
  }

  public List<RouteBean> getRoutes() {
    return _routes;
  }
  
  @Override
  public String execute() throws Exception {

    CoordinateBounds bounds = getDefaultSearchArea();
    
    if( bounds == null)
      return NEEDS_DEFAULT_SEARCH_LOCATION;
    
    if( _routeName == null || _routeName.length() == 0)
      return INPUT;

    SearchQueryBean routesQuery = new SearchQueryBean();
    routesQuery.setBounds(bounds);
    routesQuery.setMaxCount(10);
    routesQuery.setQuery(_routeName);
    routesQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
    
    RoutesBean routesBean = _transitDataService.getRoutes(routesQuery);
    List<RouteBean> routes = routesBean.getRoutes();
    
    if (routes.size() == 0) {
      return "noRoutesFound";
    } else if (routes.size() == 1 ) {
      _route = routes.get(0);
      return SUCCESS;
    } else {
      _routes = routes;
      return "multipleRoutesFound";
    }
  }
}
