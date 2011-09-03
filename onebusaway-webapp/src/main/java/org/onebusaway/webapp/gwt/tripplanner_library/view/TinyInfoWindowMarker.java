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
package org.onebusaway.webapp.gwt.tripplanner_library.view;

import org.onebusaway.webapp.gwt.common.widgets.SpanPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerCssResource;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;

import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class TinyInfoWindowMarker extends Overlay {

  private static TripPlannerCssResource _css = TripPlannerResources.INSTANCE.getCss();
  
  private LatLng _point;
  private Widget _content;
  private MapWidget _map;
  private FlowPanel _panel;

  public TinyInfoWindowMarker(LatLng point, Widget content) {
    _point = point;
    _content = content;
  }

  @Override
  protected Overlay copy() {
    return new TinyInfoWindowMarker(_point, _content);
  }

  @Override
  protected void initialize(MapWidget map) {

    _panel = new FlowPanel();
    _panel.addStyleName(_css.tinyInfoWindow());

    SpanPanel leftSpan = new SpanPanel();
    leftSpan.addStyleName(_css.tinyInfoWindowLeftPanel());
    leftSpan.add(_content);
    _panel.add(leftSpan);

    SpanWidget rightSpan = new SpanWidget("");
    rightSpan.addStyleName(_css.tinyInfoWindowRightPanel());
    _panel.add(rightSpan);

    _map = map;
    _map.getPane(MapPaneType.MARKER_PANE).add(_panel);
  }

  @Override
  protected void redraw(boolean force) {
    Point point = _map.convertLatLngToDivPixel(_point);
    int x = point.getX();
    int y = point.getY();
    _map.getPane(MapPaneType.MARKER_PANE).setWidgetPosition(_panel, x, y);
  }

  @Override
  protected void remove() {
    _map.getPane(MapPaneType.MARKER_PANE).remove(_panel);
  }
}
