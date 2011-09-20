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
package org.onebusaway.webapp.gwt.where_refineview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.stop_and_route_selection.AbstractStopAndRouteSelectionWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public class RefineViewWidget extends AbstractStopAndRouteSelectionWidget {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  Anchor _showCustomViewAnchor;

  public RefineViewWidget() {
    initWidget(_uiBinder.createAndBindUi(this));
    initialize();
    _showCustomViewAnchor.setHref("#showCustomView");
    initialStopsAndRoutes();
  }

  @UiHandler("_showCustomViewAnchor")
  void handleShowCustomViewClick(ClickEvent e) {
    e.preventDefault();

    Set<String> routeIds = new HashSet<String>();
    boolean allRoutesSelected = true;

    for (StopBean stop : _stopsById.values()) {
      for (RouteBean route : stop.getRoutes()) {
        Boolean selected = _routeSelectionById.get(route.getId());
        if (selected != null && selected)
          routeIds.add(route.getId());
        else
          allRoutesSelected = false;
      }
    }

    String path = Location.getPath();
    path = path.replaceAll("refineview.html", "");

    UrlBuilder b = Location.createUrlBuilder();

    b.setPath(path + "stop.action");

    b.setParameter("id", _stopsById.keySet().toArray(new String[0]));

    if (!allRoutesSelected)
      b.setParameter("route", routeIds.toArray(new String[0]));

    Location.replace(b.buildString());
  }

  /****
   *
   ****/
  
  private void initialStopsAndRoutes() {
    Map<String, List<String>> map = Location.getParameterMap();
    
    List<String> stopIds = new ArrayList<String>();
    List<String> routeIds = new ArrayList<String>();
    
    if (map.containsKey("id"))
      stopIds = map.get("id");
    
    if (map.containsKey("route"))
      routeIds = map.get("route");

    setStopsAndRoutes(stopIds, routeIds);
  }

  interface MyUiBinder extends UiBinder<Widget, RefineViewWidget> {
  }
}
