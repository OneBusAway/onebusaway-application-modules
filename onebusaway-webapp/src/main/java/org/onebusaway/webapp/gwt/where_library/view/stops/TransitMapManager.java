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
package org.onebusaway.webapp.gwt.where_library.view.stops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.resources.map.StopIconFactory;
import org.onebusaway.webapp.gwt.common.resources.map.StopIconFactory.ESize;
import org.onebusaway.webapp.gwt.where_library.GeocoderAccuracyToBounds;
import org.onebusaway.webapp.gwt.where_library.impl.StopsForRegionServiceImpl;
import org.onebusaway.webapp.gwt.where_library.view.events.StopClickedEvent;
import org.onebusaway.webapp.gwt.where_library.view.events.StopClickedHandler;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.EncodedPolyline;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TransitMapManager {

  private StopsForRegionServiceImpl _stopsForRegionService = new StopsForRegionServiceImpl();

  private Map<String, StopAndOverlays> _visibleStopsById = new HashMap<String, StopAndOverlays>();

  private StopsHandler _stopsHandler = new StopsHandler();

  private Set<String> _stopIdsToAlwaysShow = new HashSet<String>();

  private Set<String> _selectedStopIds = new HashSet<String>();

  private List<Overlay> _otherOverlays = new ArrayList<Overlay>();

  private boolean _showStopsInCurrentView = true;

  private MapWidget _map;

  private MapOverlayManager _manager;

  private HandlerManager _handlerManager;

  private RouteBean _selectedRoute = null;

  public TransitMapManager(MapWidget map) {
    _map = map;
    _manager = new MapOverlayManager(_map);

    _map.addMapMoveEndHandler(new MapMoveEndHandlerImpl());
  }

  public HandlerRegistration addStopClickedHandler(StopClickedHandler handler) {
    return ensureHandlers().addHandler(StopClickedEvent.TYPE, handler);
  }

  public MapWidget getMap() {
    return _map;
  }

  public RouteBean getSelectedRoute() {
    return _selectedRoute;
  }

  /****
   * Visualization Modes
   ****/

  public void showStopsInCurrentView() {
    reset(true);
  }

  public void showStopsAtLocation(LatLng point, int zoomLevel) {
    _map.setCenter(point, zoomLevel);
    reset(true);
  }

  public void showStops(List<StopBean> stops) {
    reset(false, stops);
  }

  public void showStopsForRoute(RouteBean route,
      StopsForRouteBean stopsForRoute, boolean centerViewOnRoute) {

    reset(false, stopsForRoute.getStops());
    _selectedRoute = route;

    for (EncodedPolylineBean path : stopsForRoute.getPolylines()) {

      EncodedPolyline epl = EncodedPolyline.newInstance(path.getPoints(), 32,
          path.getLevels(3), 4);
      epl.setColor("#4F64BA");
      epl.setWeight(3);
      epl.setOpacity(1.0);
      Polyline polyline = Polyline.fromEncoded(epl);
      _manager.addOverlay(polyline);
      _otherOverlays.add(polyline);
    }

    if (centerViewOnRoute) {
      System.out.println("center on route");
      // Center the map on the bounds of the route
      LatLngBounds bounds = LatLngBounds.newInstance();
      for (StopBean stop : stopsForRoute.getStops())
        bounds.extend(LatLng.newInstance(stop.getLat(), stop.getLon()));

      if (!bounds.isEmpty()) {
        int zoomLevel = _map.getBoundsZoomLevel(bounds);
        _map.setCenter(bounds.getCenter(), zoomLevel);
      }
    }
  }

  public void showStop(StopBean stop, boolean showStopsInCurrentView) {
    LatLng point = LatLng.newInstance(stop.getLat(), stop.getLon());
    _map.setCenter(point, 17);
    Set<String> selectedStopIds = new HashSet<String>();
    selectedStopIds.add(stop.getId());
    reset(showStopsInCurrentView, Arrays.asList(stop), selectedStopIds);
  }

  public void showPlace(Place place, boolean showStopsInCurrentView,
      PlaceClickHandler clickHandler) {
    System.out.println("accuracy=" + place.getAccuracy());
    int zoomLevel = GeocoderAccuracyToBounds.getZoomLevelForAccuracy(place.getAccuracy());
    _map.setCenter(place.getLocation(), zoomLevel);
    reset(true);
    addPlaceToMap(place, clickHandler);
  }

  public void showPlaces(List<Place> places, boolean showStopsInCurrentView,
      PlaceClickHandler clickHandler) {

    LatLngBounds bounds = LatLngBounds.newInstance();
    for (Place place : places)
      bounds.extend(place.getLocation());
    if (!bounds.isEmpty())
      _map.setCenter(bounds.getCenter(), _map.getBoundsZoomLevel(bounds));

    reset(showStopsInCurrentView);
    for (Place place : places)
      addPlaceToMap(place, clickHandler);
  }

  /****
   * Private Methods
   ****/

  private HandlerManager ensureHandlers() {
    return _handlerManager == null ? _handlerManager = new HandlerManager(this)
        : _handlerManager;
  }

  private void reset(boolean showStopsInCurrentView) {
    reset(showStopsInCurrentView, new ArrayList<StopBean>());
  }

  private void reset(boolean showStopsInCurrentView,
      List<StopBean> stopsToAlwaysShow) {
    reset(showStopsInCurrentView, stopsToAlwaysShow, new HashSet<String>());
  }

  private void reset(boolean showStopsInCurrentView,
      List<StopBean> stopsToAlwaysShow, Set<String> selectedStopIds) {

    _showStopsInCurrentView = showStopsInCurrentView;

    _stopIdsToAlwaysShow.clear();
    for (StopBean stop : stopsToAlwaysShow)
      _stopIdsToAlwaysShow.add(stop.getId());

    for (Overlay overlay : _otherOverlays)
      _manager.removeOverlay(overlay);
    _otherOverlays.clear();

    for (StopAndOverlays stopAndOverlay : _visibleStopsById.values()) {
      List<Overlay> overlays = stopAndOverlay.getOverlays();
      for (Overlay overlay : overlays)
        _manager.removeOverlay(overlay);
    }

    _visibleStopsById.clear();
    _selectedStopIds.clear();
    _selectedStopIds.addAll(selectedStopIds);

    _stopsHandler.onSuccess(stopsToAlwaysShow);

    _selectedRoute = null;

    refresh();
  }

  private void refresh() {

    if (_showStopsInCurrentView) {

      int zoom = _map.getZoomLevel();

      if (zoom < 17) {
        return;
      }

      final CoordinateBounds bounds = computeBounds();

      for (Iterator<Map.Entry<String, StopAndOverlays>> it = _visibleStopsById.entrySet().iterator(); it.hasNext();) {
        Entry<String, StopAndOverlays> entry = it.next();
        StopAndOverlays stopAndOverlays = entry.getValue();
        StopBean stop = stopAndOverlays.getStop();
        if (!bounds.contains(stop.getLat(), stop.getLon())) {
          it.remove();
          for (Overlay overlay : stopAndOverlays.getOverlays())
            _manager.removeOverlay(overlay);
        }
      }

      // Update the map
      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          _stopsForRegionService.getStopsForRegion(bounds, _stopsHandler);
        }
      });

    } else {

      for (StopAndOverlays stopAndOverlays : _visibleStopsById.values()) {
        StopBean stop = stopAndOverlays.getStop();
        if (_stopIdsToAlwaysShow.contains(stop.getId()))
          continue;
        for (Overlay overlay : stopAndOverlays.getOverlays())
          _manager.removeOverlay(overlay);
      }
      _visibleStopsById.keySet().retainAll(_stopIdsToAlwaysShow);
    }
  }

  private CoordinateBounds computeBounds() {
    LatLngBounds r = _map.getBounds();
    LatLng ne = r.getNorthEast();
    LatLng sw = r.getSouthWest();

    double latMin = sw.getLatitude();
    double lonMin = sw.getLongitude();
    double latMax = ne.getLatitude();
    double lonMax = ne.getLongitude();

    return new CoordinateBounds(latMin, lonMin, latMax, lonMax);
  }

  private MarkerClickHandler getClickHandlerForStop(final StopBean stop) {
    MarkerClickHandler clickHandler = new MarkerClickHandler() {
      public void onClick(MarkerClickEvent event) {
        ensureHandlers().fireEvent(new StopClickedEvent(stop));
      }
    };
    return clickHandler;
  }

  private Marker getStopMarker(final StopBean stop, final LatLng p, ESize size) {

    MarkerOptions opts = MarkerOptions.newInstance();
    boolean isSelected = false;

    Icon icon = StopIconFactory.getStopIcon(stop, size, isSelected);
    opts.setIcon(icon);
    return new Marker(p, opts);
  }

  private void addOverlayAtZoom(Overlay overlay, int from, int to) {
    _manager.addOverlay(overlay, from, to);
  }

  private void addPlaceToMap(final Place place,
      final PlaceClickHandler clickHandler) {
    LatLng point = place.getLocation();
    Marker marker = new Marker(point);

    if (clickHandler != null) {
      marker.addMarkerClickHandler(new MarkerClickHandler() {
        @Override
        public void onClick(MarkerClickEvent event) {
          clickHandler.onPlaceClicked(place);
        }
      });
    }
    _manager.addOverlay(marker);
    _otherOverlays.add(marker);
  }

  /****
   * Internal Classes
   ****/

  private class MapMoveEndHandlerImpl implements MapMoveEndHandler {
    @Override
    public void onMoveEnd(MapMoveEndEvent event) {
      refresh();
    }
  }

  private class StopsHandler implements AsyncCallback<List<StopBean>> {

    public void onSuccess(List<StopBean> stops) {

      for (StopBean stop : stops) {

        if (_visibleStopsById.containsKey(stop.getId()))
          continue;

        StopAndOverlays stopAndOverlays = new StopAndOverlays(stop);
        _visibleStopsById.put(stop.getId(), stopAndOverlays);

        LatLng p = LatLng.newInstance(stop.getLat(), stop.getLon());

        MarkerClickHandler clickHandler = getClickHandlerForStop(stop);

        // Show some close up stops by default
        Marker largerMarker = getStopMarker(stop, p, ESize.LARGE);

        largerMarker.addMarkerClickHandler(clickHandler);

        addOverlayAtZoom(largerMarker, 17, 20);

        stopAndOverlays.addOverlays(largerMarker);

        // If we're not showing stops in our current view, it must mean we're in
        // route view, so we show the stops more zoomed out
        if (!_showStopsInCurrentView) {

          Marker mediumMarker = getStopMarker(stop, p, ESize.MEDIUM);
          mediumMarker.addMarkerClickHandler(clickHandler);
          addOverlayAtZoom(mediumMarker, 16, 17);

          Marker smallMarker = getStopMarker(stop, p, ESize.SMALL);
          smallMarker.addMarkerClickHandler(clickHandler);
          addOverlayAtZoom(smallMarker, 13, 16);

          Marker tinyMarker = getStopMarker(stop, p, ESize.TINY);
          tinyMarker.addMarkerClickHandler(clickHandler);
          addOverlayAtZoom(tinyMarker, 9, 13);

          stopAndOverlays.addOverlays(mediumMarker, smallMarker, tinyMarker);
        }

        if (_selectedStopIds.contains(stop.getId())) {
          Marker big = StopIconFactory.getStopSelectionCircle(p, true);
          Marker small = StopIconFactory.getStopSelectionCircle(p, false);
          addOverlayAtZoom(small, 16, 17);
          addOverlayAtZoom(big, 17, 20);
          stopAndOverlays.addOverlays(big, small);
        }
      }
    }

    @Override
    public void onFailure(Throwable arg0) {
      arg0.printStackTrace();
    }
  }

  private class StopAndOverlays {

    private StopBean _stop;

    private List<Overlay> _overlays = new ArrayList<Overlay>();

    public StopAndOverlays(StopBean stop) {
      _stop = stop;
    }

    public StopBean getStop() {
      return _stop;
    }

    public List<Overlay> getOverlays() {
      return _overlays;
    }

    public void addOverlays(Overlay... overlays) {
      for (Overlay overlay : overlays)
        _overlays.add(overlay);
    }
  }

}
