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
package org.onebusaway.webapp.gwt.mobile_application.view;

import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationErrorEvent;
import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationEvent;
import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationHandler;
import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.mobile_application.control.Actions;
import org.onebusaway.webapp.gwt.mobile_application.control.LocationManager;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationCssResource;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;
import org.onebusaway.webapp.gwt.viewkit.BarButtonItem;
import org.onebusaway.webapp.gwt.viewkit.NavigationItem;
import org.onebusaway.webapp.gwt.viewkit.ViewController;
import org.onebusaway.webapp.gwt.viewkit.BarButtonItem.EBarButtonSystemItem;
import org.onebusaway.webapp.gwt.where_library.view.events.StopClickedEvent;
import org.onebusaway.webapp.gwt.where_library.view.events.StopClickedHandler;
import org.onebusaway.webapp.gwt.where_library.view.stops.PlaceClickHandler;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

public class MapViewController extends ViewController {

  private MobileApplicationCssResource _css = MobileApplicationResources.INSTANCE.getCSS();

  private LocationManager _locationManager = MobileApplicationContext.getLocationManager();

  private static int _zoom = 11;

  private FlowPanel _panel = new FlowPanel();

  private MapWidget _map;

  private TransitMapManager _transitMapManager;

  private Marker _currentLocationMarker = null;

  public void showStopsForRoute(RouteBean route, StopsForRouteBean stopsForRoute) {
    _transitMapManager.showStopsForRoute(route, stopsForRoute, true);
    LatLngBounds b = LatLngBounds.newInstance();
    for (StopBean stop : stopsForRoute.getStops())
      b.extend(LatLng.newInstance(stop.getLat(), stop.getLon()));
    if (!b.isEmpty()) {
      _map.setCenter(b.getCenter(), _map.getBoundsZoomLevel(b));
      _locationManager.setLastSearchLocation(b.getCenter());
    }
  }

  public void showPlace(Place place) {
    _transitMapManager.showPlace(place, true, null);
    _locationManager.setLastSearchLocation(place.getLocation());
  }

  public void showPlaces(List<Place> places) {
    _transitMapManager.showPlaces(places, false,
        new PlaceClickHandlerImpl(true));

    LatLngBounds b = LatLngBounds.newInstance();
    for (Place place : places)
      b.extend(place.getLocation());

    if (!b.isEmpty())
      _locationManager.setLastSearchLocation(b.getCenter());
  }

  /****
   * {@link ViewController} Interface
   ****/

  @Override
  protected void loadView() {
    super.loadView();

    System.out.println("loading...");

    NavigationItem navigationItem = getNavigationItem();
    navigationItem.setTitle("Map");
    navigationItem.setLeftBarButtonItem(new BarButtonItem(
        EBarButtonSystemItem.CROSS_HAIRS, new UseCurrentLocationClickHandler()));

    _locationManager.addGeoLocationHandler(new GeoLocationHandlerImpl());
    LatLng currentLocation = _locationManager.getCurrentSearchLocation();

    _map = new MapWidget(currentLocation, _zoom);

    _map.addControl(new LargeButtonMapControl());
    _map.addControl(new MapTypeControl());
    _map.addControl(new ScaleControl());
    _map.setScrollWheelZoomEnabled(true);

    _map.setHeight("100%");
    _map.setWidth("100%");

    _transitMapManager = new TransitMapManager(_map);
    _transitMapManager.addStopClickedHandler(new StopClickedHandlerImpl());
    _panel.addStyleName(_css.MapViewControllerContainer());

    if (_locationManager.hasPhysicalLocation())
      updateCurrentLocationMarker(currentLocation.getLatitude(),
          currentLocation.getLongitude());

    _view = _panel;
  }

  @Override
  public void viewDidAppear() {
    super.viewDidAppear();
    _panel.add(_map);
    System.out.println("check resize");
    _map.checkResizeAndCenter();
  }

  @Override
  public void viewWillDisappear() {
    super.viewWillDisappear();
    _panel.remove(_map);
  }

  @Override
  public void handleContext(List<String> path, Map<String, String> context) {

  }

  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {

  }

  /****
   * Private Methods
   ****/

  private void showPlaceInfoWindow(Place place, boolean includeSelectionLink) {
    LatLng p = place.getLocation();
    InfoWindow window = _map.getInfoWindow();

    FlowPanel panel = new FlowPanel();

    FlowPanel rowA = new FlowPanel();
    panel.add(rowA);
    rowA.add(new SpanWidget(place.getName()));

    if (includeSelectionLink) {
      FlowPanel rowB = new FlowPanel();
      panel.add(rowB);
      Anchor anchor = new Anchor("Show nearby stops");
      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {

        }
      });
      rowB.add(anchor);
    }

    window.open(p, new InfoWindowContent(panel));
  }

  private class StopClickedHandlerImpl implements StopClickedHandler {

    @Override
    public void handleStopClicked(StopClickedEvent event) {

      final StopBean stop = event.getStop();
      FlowPanel panel = new FlowPanel();

      panel.add(new DivWidget(stop.getName()));

      StringBuilder b = new StringBuilder();
      if (stop.getDirection() != null)
        b.append(stop.getDirection()).append(" - ");

      b.append("Routes: ");
      boolean first = true;
      for (RouteBean route : stop.getRoutes()) {
        if (!first)
          b.append(",");
        b.append(" ").append(route.getShortName());
      }

      panel.add(new DivWidget(b.toString()));

      Anchor anchor = new Anchor("Show Real-Time Arrivals");
      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          Actions.showArrivalsAndDeparturesForStop(getNavigationController(),
              stop.getId());
        }
      });

      DivPanel anchorRow = new DivPanel();
      panel.add(anchorRow);
      anchorRow.add(anchor);

      InfoWindow window = _map.getInfoWindow();
      LatLng point = LatLng.newInstance(stop.getLat(), stop.getLon());
      window.open(point, new InfoWindowContent(panel));
    }

  }

  private void updateCurrentLocationMarker(double lat, double lon) {

    if (_currentLocationMarker != null) {
      LatLng currentCenter = _currentLocationMarker.getLatLng();
      if (currentCenter.getLatitude() == lat
          && currentCenter.getLongitude() == lon)
        return;
      _map.removeOverlay(_currentLocationMarker);
      _currentLocationMarker = null;
    }

    LatLng center = LatLng.newInstance(lat, lon);
    _currentLocationMarker = new Marker(center);
    _map.addOverlay(_currentLocationMarker);

    _map.panTo(center);
  }

  private class UseCurrentLocationClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent arg0) {
      if (_locationManager.hasPhysicalLocation()) {
        _map.setCenter(_locationManager.getPhysicalLocation());
        _transitMapManager.showStopsInCurrentView();
      }
    }
  }

  private class GeoLocationHandlerImpl implements GeoLocationHandler {

    @Override
    public void handleLocation(GeoLocationEvent event) {
      double lat = event.getLat();
      double lon = event.getLon();

      updateCurrentLocationMarker(lat, lon);
    }

    @Override
    public void handleError(GeoLocationErrorEvent event) {
      System.out.println("error: code=" + event.getCode() + " message="
          + event.getMessage());
    }
  }

  private class PlaceClickHandlerImpl implements PlaceClickHandler {

    private boolean _includeSelectionLink;

    public PlaceClickHandlerImpl(boolean includeSelectionLink) {
      _includeSelectionLink = includeSelectionLink;
    }

    @Override
    public void onPlaceClicked(Place place) {
      showPlaceInfoWindow(place, _includeSelectionLink);
    }
  }

}
