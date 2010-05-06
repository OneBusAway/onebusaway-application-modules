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

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;

import com.google.gwt.maps.client.overlay.EncodedPolyline;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;

public class RouteConstraint extends AbstractConstraint {

  private String _routeId;

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  public RouteConstraint(String routeId) {
    _routeId = routeId;
  }

  public void update(Context context) {
    _service.getRouteForId(_routeId, new RouteHandler());
    _service.getStopsForRoute(_routeId, new StopsForRouteHandler());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteConstraint))
      return false;

    RouteConstraint rc = (RouteConstraint) obj;
    return _routeId.equals(rc._routeId);
  }

  @Override
  public int hashCode() {
    return _routeId.hashCode();
  }

  /****
   * Private Methods
   ****/

  /***************************************************************************
   * Internal Classes
   **************************************************************************/

  private class RouteHandler implements AsyncCallback<RouteBean> {

    public void onSuccess(RouteBean route) {
      _resultsPanel.clear();

      DivPanel resultPanel = new DivPanel();
      _resultsPanel.add(resultPanel);

      _stopFinder.setSearchText(route.getShortName());

      String name = RoutesConstraint.getRouteName(route);
      DivWidget routeRow = new DivWidget(name);
      resultPanel.add(routeRow);

      DivPanel agencyRow = new DivPanel();
      resultPanel.add(agencyRow);

      AgencyBean agency = route.getAgency();
      String url = agency.getUrl();
      if (url != null) {
        agencyRow.add(new SpanWidget("Operated by "));
        agencyRow.add(new Anchor(agency.getName(), url));
      } else {
        agencyRow.add(new SpanWidget("Operated by " + agency.getName()));
      }
    }

    public void onFailure(Throwable ex) {

    }

  }

  private class StopsForRouteHandler implements
      AsyncCallback<StopsForRouteBean> {

    public void onSuccess(StopsForRouteBean bean) {

      StopsBean stopsBean = new StopsBean();
      stopsBean.setStopBeans(bean.getStops());
      _stopFinder.setShowStopsInCurrentView(false);
      _stopFinder.showStops(stopsBean.getStops());

      for (EncodedPolylineBean polyline : bean.getPolylines()) {
        EncodedPolyline ep = EncodedPolyline.newInstance();
        ep.setPoints(polyline.getPoints());
        ep.setLevels(polyline.getLevels(3));
        ep.setZoomFactor(32);
        ep.setNumLevels(4);
        ep.setColor("#4F64BA");
        ep.setWeight(3);
        ep.setOpacity(1.0);
        _map.addOverlay(Polyline.fromEncoded(ep));
      }
    }

    public void onFailure(Throwable ex) {

    }
  }
}