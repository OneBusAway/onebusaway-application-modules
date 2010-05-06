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
package edu.washington.cs.rse.transit.web.oba.standard.client.pages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.overlay.Overlay;

public class MyMarkerManager {

    private Map<ZoomCheck, Set<Overlay>> _overlaysByZoom = new HashMap<ZoomCheck, Set<Overlay>>();

    private MapWidget _map;

    public MyMarkerManager(MapWidget map) {
        _map = map;
        map.addMapZoomEndHandler(new MapZoomHandler());
    }

    public void addOverlay(Overlay overlay, int minZoom, int maxZoom) {
        addOverlay(overlay, new ZoomRange(minZoom, maxZoom));
    }

    private void addOverlay(Overlay overlay, ZoomCheck check) {

        Set<Overlay> overlays = _overlaysByZoom.get(check);

        if (overlays == null) {
            overlays = new HashSet<Overlay>();
            _overlaysByZoom.put(check, overlays);
        }
        overlays.add(overlay);
        if (check.isVisible(_map.getZoomLevel()))
            _map.addOverlay(overlay);
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
