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
/**
 * 
 */
package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.RoutesQueryBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;

import java.util.List;

public class RoutesConstraint extends AbstractConstraint {

  private String _routeQuery;

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  public RoutesConstraint(String routeQuery) {
    _routeQuery = routeQuery;
  }

  public void update(Context context) {
    _stopFinder.setSearchText(_routeQuery);
    LatLng center = _map.getCenter();
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        center.getLatitude(), center.getLongitude(), 10000);
    
    RoutesQueryBean query = new RoutesQueryBean();
    query.setBounds(bounds);
    query.setMaxCount(10);
    query.setQuery(_routeQuery);
    
    _service.getRoutes(query, new RoutesHandler());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RoutesConstraint))
      return false;

    RoutesConstraint rc = (RoutesConstraint) obj;
    return _routeQuery.equals(rc._routeQuery);
  }

  @Override
  public int hashCode() {
    return _routeQuery.hashCode();
  }

  /***************************************************************************
   * Internal Classes
   **************************************************************************/

  public static String getRouteName(RouteBean route) {
    if (route.getShortName() == null)
      return route.getLongName();
    if (route.getLongName() == null)
      return route.getShortName();
    return route.getShortName() + " - " + route.getLongName();
  }

  private class RoutesHandler implements AsyncCallback<RoutesBean> {

    public void onSuccess(RoutesBean routesBean) {
      List<RouteBean> routes = routesBean.getRoutes();
      if (routes.size() == 1) {
        RouteBean route = routes.get(0);
        _stopFinder.queryRoute(route.getId());
      } else {
        _resultsPanel.clear();
        _resultsPanel.add(new DivWidget("Did you mean:"));

        for (final RouteBean route : routes) {

          DivPanel resultPanel = new DivPanel();
          _resultsPanel.add(resultPanel);

          DivPanel routeRow = new DivPanel();
          resultPanel.add(routeRow);

          String name = getRouteName(route);
          Anchor anchor = new Anchor(name);
          anchor.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
              _stopFinder.queryRoute(route.getId());
            }
          });
          routeRow.add(anchor);

          DivPanel agencyRow = new DivPanel();
          resultPanel.add(agencyRow);

          AgencyBean agency = route.getAgency();
          agencyRow.add(new SpanWidget("Operated by " + agency.getName()));
        }
      }
    }

    public void onFailure(Throwable ex) {

    }
  }
}