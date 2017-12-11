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
package org.onebusaway.enterprise.webapp.actions.where;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;

@Results( {@Result(type = "redirectAction", name = "singleRouteFound", params = {
    "actionName", "stops-for-route", "id", "${route.id}", "parse", "true"})})
public class RoutesAction extends AbstractWhereAction {

  private static final long serialVersionUID = 1L;

  private String _query;

  private RouteBean _route;

  private List<RouteBean> _routes;

  public void setQuery(String query) {
    _query = query;
  }
  
  public List<RouteBean> getRoutes() {
    return _routes;
  }

  public RouteBean getRoute() {
    return _route;
  }

  @Override
  @Actions( {
      @Action(value = "/where/iphone/routes")
  })
  public String execute() throws ServiceException {
    
    if( _query == null || _query.length() == 0)
      return INPUT;

    CoordinateBounds bounds = getServiceArea();

    if (bounds == null) {
      pushNextAction("routes", "query", _query);
      return "query-default-search-location";
    }

    SearchQueryBean query = new SearchQueryBean();
    query.setBounds(bounds);
    query.setMaxCount(5);
    query.setQuery(_query);
    query.setType(EQueryType.BOUNDS_OR_CLOSEST);
    RoutesBean routesResult = _transitDataService.getRoutes(query);

    _routes = routesResult.getRoutes();

    if (_routes.size() == 0) {
      return "noRoutesFound";
    } else if (_routes.size() > 1) {
      return "multipleRoutesFound";
    } else {
      _route = _routes.get(0);
      return "singleRouteFound";
    }
  }
}
