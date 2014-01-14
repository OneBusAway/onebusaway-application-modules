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
package org.onebusaway.webapp.gwt.common;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Overlay;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MapOverlayManager {

  private Map<Overlay, ZoomCheck> _zoomByOverlay = new HashMap<Overlay, ZoomCheck>();

  private Map<ZoomCheck, Set<Overlay>> _overlaysByZoom = new HashMap<ZoomCheck, Set<Overlay>>();

  private MapWidget _map;

  public MapOverlayManager() {

  }

  public MapOverlayManager(MapWidget map) {
    _map = map;
    _map.addMapZoomEndHandler(new MapZoomHandler());
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
    map.addMapZoomEndHandler(new MapZoomHandler());
  }

  public MapWidget getMapWidget() {
    return _map;
  }

  public void addOverlay(Overlay overlay) {
    addOverlay(overlay, 0, 20);
  }

  public void addOverlay(Overlay overlay, int minZoom, int maxZoom) {
    addOverlay(overlay, new ZoomRange(minZoom, maxZoom));
  }

  public void setCenter(LatLng center) {
    _map.setCenter(center);
  }

  public void setCenterAndZoom(LatLng center, int zoomLevel) {
    _map.setCenter(center, zoomLevel);
  }

  public void setCenterAndZoom(LatLngBounds bounds) {
    _map.setCenter(bounds.getCenter(), _map.getBoundsZoomLevel(bounds));
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void addOverlay(Overlay overlay, ZoomCheck check) {

    ZoomCheck current = _zoomByOverlay.get(overlay);
    if (current != null) {
      if (current.equals(check))
        return;
      removeOverlay(overlay);
    }

    _zoomByOverlay.put(overlay, check);

    Set<Overlay> overlays = _overlaysByZoom.get(check);

    if (overlays == null) {
      overlays = new LinkedHashSet<Overlay>();
      _overlaysByZoom.put(check, overlays);
    }
    overlays.add(overlay);
    if (check.isVisible(_map.getZoomLevel()))
      _map.addOverlay(overlay);
  }

  public void removeOverlay(Overlay overlay) {
    ZoomCheck zoom = _zoomByOverlay.remove(overlay);
    if (zoom != null) {
      Set<Overlay> overlays = _overlaysByZoom.get(zoom);
      if (overlays != null) {
        overlays.remove(overlay);
      }
      if (zoom.isVisible(_map.getZoomLevel()))
        _map.removeOverlay(overlay);
    }
  }

  public void clear() {
    for (Map.Entry<ZoomCheck, Set<Overlay>> entry : _overlaysByZoom.entrySet()) {
      ZoomCheck check = entry.getKey();
      if (check.isVisible(_map.getZoomLevel())) {
        for (Overlay overlay : entry.getValue())
          _map.removeOverlay(overlay);
      }
    }
    _overlaysByZoom.clear();
  }

  private class MapZoomHandler implements MapZoomEndHandler {
    public void onZoomEnd(MapZoomEndEvent event) {
      for (Map.Entry<ZoomCheck, Set<Overlay>> entry : _overlaysByZoom.entrySet()) {
        ZoomCheck check = entry.getKey();
        boolean then = check.isVisible(event.getOldZoomLevel());
        boolean now = check.isVisible(event.getNewZoomLevel());
        if (!then && now) {
          for (Overlay overlay : entry.getValue())
            _map.addOverlay(overlay);
        }

        if (then && !now) {
          for (Overlay overlay : entry.getValue())
            _map.removeOverlay(overlay);
        }
      }
    }
  }

  private interface ZoomCheck {
    public boolean isVisible(int zoomLevel);
  }

  private class ZoomRange implements ZoomCheck {

    private int _from;

    private int _to;

    public ZoomRange(int from, int to) {
      _from = Math.min(from, to);
      _to = Math.max(from, to);
    }

    public boolean isVisible(int zoomLevel) {
      return _from <= zoomLevel && zoomLevel < _to;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof ZoomRange))
        return false;
      ZoomRange r = (ZoomRange) obj;
      return _from == r._from && _to == r._to;
    }

    @Override
    public int hashCode() {
      return _from * 3 + _to * 7;
    }
  }
}
