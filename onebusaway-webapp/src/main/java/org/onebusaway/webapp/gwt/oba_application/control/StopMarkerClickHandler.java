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
/**
 * 
 */
package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.oba.TimedStopBean;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;

import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.FlowPanel;

class StopMarkerClickHandler implements MarkerClickHandler {

  private TimedStopBean _bean;

  private LatLng _point;

  private MapOverlayManager _mapManager;

  public StopMarkerClickHandler(MapOverlayManager manager, TimedStopBean bean,
      LatLng point) {
    _mapManager = manager;
    _bean = bean;
    _point = point;
  }

  public void onClick(MarkerClickEvent event) {

    StopBean stop = _bean.getStop();

    FlowPanel panel = new FlowPanel();
    panel.add(new DivWidget(stop.getName()));
    panel.add(new DivWidget("Stop # " + stop.getId()));
    panel.add(new DivWidget("Travel Time: " + (_bean.getTime() / 60) + " mins"));

    MapWidget map = _mapManager.getMapWidget();
    InfoWindow window = map.getInfoWindow();
    window.open(_point, new InfoWindowContent(panel));
  }

}