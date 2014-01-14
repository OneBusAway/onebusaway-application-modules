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
package org.onebusaway.webapp.gwt.oba_library.view;

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;

import com.google.gwt.dom.client.Style;
import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;

public class PatchMarker extends Overlay {

  private LatLng _northWestCorner;
  private LatLng _southEastCorner;
  private MapWidget _map;
  private DivWidget _widget;

  public PatchMarker(LatLngBounds bounds) {
    this(getNorthWestCorner(bounds), getSouthEastCorner(bounds));
  }

  private PatchMarker(LatLng northWestCorner, LatLng southEastCorner) {
    _northWestCorner = northWestCorner;
    _southEastCorner = southEastCorner;
    _widget = new DivWidget("");
  }

  public void addStyleName(String name) {
    _widget.addStyleName(name);
    
  }

  public void setStyleName(String name) {
    _widget.setStyleName(name);
  }
  
  public Style getStyle() {
    return _widget.getElement().getStyle();
  }

  public void setVisible(boolean visible) {
    _widget.setVisible(visible);
  }

  @Override
  protected Overlay copy() {
    return new PatchMarker(_northWestCorner, _southEastCorner);
  }

  @Override
  protected void initialize(MapWidget map) {
    _map = map;
    _map.getPane(MapPaneType.MARKER_SHADOW_PANE).add(_widget);
  }

  @Override
  protected void redraw(boolean force) {

    Point point = _map.convertLatLngToDivPixel(_northWestCorner);
    int x = point.getX();
    int y = point.getY();

    _map.getPane(MapPaneType.MARKER_SHADOW_PANE).setWidgetPosition(_widget, x,
        y);

    Point point2 = _map.convertLatLngToDivPixel(_southEastCorner);
    int x2 = point2.getX();
    int y2 = point2.getY();

    _widget.setWidth((x2 - x) + "px");
    _widget.setHeight((y2 - y) + "px");
  }

  @Override
  protected void remove() {
    _map.getPane(MapPaneType.MARKER_SHADOW_PANE).remove(_widget);
  }

  private static LatLng getNorthWestCorner(LatLngBounds bounds) {
    LatLng ne = bounds.getNorthEast();
    LatLng sw = bounds.getSouthWest();
    return LatLng.newInstance(ne.getLatitude(), sw.getLongitude());
  }

  private static LatLng getSouthEastCorner(LatLngBounds bounds) {
    LatLng ne = bounds.getNorthEast();
    LatLng sw = bounds.getSouthWest();
    return LatLng.newInstance(sw.getLatitude(), ne.getLongitude());
  }

}
