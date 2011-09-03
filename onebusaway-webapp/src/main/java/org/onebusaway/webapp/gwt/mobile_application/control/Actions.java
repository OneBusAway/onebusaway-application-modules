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
package org.onebusaway.webapp.gwt.mobile_application.control;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.control.PlacemarkPlaceImpl;
import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.mobile_application.view.MapViewController;
import org.onebusaway.webapp.gwt.mobile_application.view.StopWithArrivalsAndDeparturesViewController;
import org.onebusaway.webapp.gwt.viewkit.NavigationController;
import org.onebusaway.webapp.gwt.viewkit.TabBarController;
import org.onebusaway.webapp.gwt.viewkit.ViewController;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Actions {

  private static WebappServiceAsync _service = WebappServiceAsync.SERVICE;

  public static StopWithArrivalsAndDeparturesViewController showArrivalsAndDeparturesForStop(
      NavigationController controller, String stopId) {

    StopWithArrivalsAndDeparturesViewController view = new StopWithArrivalsAndDeparturesViewController(
        stopId);

    controller.pushViewController(view);

    return view;
  }

  public static ViewController ensureStopIsSelected(ViewController controller,
      String stopId) {

    NavigationController nav = controller.getNavigationController();
    ViewController next = nav.getNextController(controller);

    if (next != null) {

      String currentStopId = Actions.getStopIdForViewController(next);
      if (currentStopId != null && currentStopId.equals(stopId))
        return next;

      // Otherwise, clear the existing view stack
      nav.popToViewController(controller);
    }

    return Actions.showArrivalsAndDeparturesForStop(nav, stopId);
  }

  public static String getStopIdForViewController(ViewController viewController) {
    if (!(viewController instanceof StopWithArrivalsAndDeparturesViewController))
      return null;

    StopWithArrivalsAndDeparturesViewController vc = (StopWithArrivalsAndDeparturesViewController) viewController;
    return vc.getStopId();
  }

  public static void searchForRoute(String route) {

    switchToMapView();

    CoordinateBounds bounds = getSearchBounds(20 * 1000);

    SearchQueryBean query = new SearchQueryBean();
    query.setQuery(route);
    query.setMaxCount(10);
    query.setBounds(bounds);
    query.setType(EQueryType.BOUNDS_OR_CLOSEST);

    _service.getRoutes(query, new RoutesHandler());
  }

  public static void searchForAddress(String address) {

    switchToMapView();

    Geocoder geocoder = new Geocoder();
    geocoder.setViewport(getSearchBoundsAsLatLngBounds(20 * 1000));
    geocoder.getLocations(address, new AddressHandler());
  }

  public void searchForStop(String stopCode) {

  }

  /****
   * Private Methods
   ****/

  private static void switchToMapView() {
    TabBarController rootController = MobileApplicationContext.getRootController();
    rootController.setSelectedIndex(0);
  }

  private static LatLngBounds getSearchBoundsAsLatLngBounds(double radius) {

    CoordinateBounds bounds = getSearchBounds(radius);

    LatLngBounds b = LatLngBounds.newInstance();
    b.extend(LatLng.newInstance(bounds.getMinLat(), bounds.getMinLon()));
    b.extend(LatLng.newInstance(bounds.getMaxLat(), bounds.getMaxLon()));
    return b;
  }

  private static CoordinateBounds getSearchBounds(double radius) {

    LocationManager locationManager = MobileApplicationContext.getLocationManager();
    LatLng p = locationManager.getCurrentSearchLocation();

    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(p.getLatitude(),
        p.getLongitude(), radius);
    System.out.println(bounds);
    return bounds;
  }

  private static class RoutesHandler implements AsyncCallback<RoutesBean> {

    @Override
    public void onSuccess(RoutesBean routesBean) {
      List<RouteBean> routes = routesBean.getRoutes();
      if (routes.size() == 1) {
        RouteBean route = routes.get(0);
        System.out.println("route found: " + route.getShortName() + " "
            + route.getLongName());
        _service.getStopsForRoute(route.getId(),
            new StopsForRouteHandler(route));
      } else {
        TabBarController rootController = MobileApplicationContext.getRootController();
        rootController.setSelectedIndex(0);
      }
    }

    @Override
    public void onFailure(Throwable arg0) {

    }
  }

  private static class StopsForRouteHandler implements
      AsyncCallback<StopsForRouteBean> {

    private RouteBean _route;

    public StopsForRouteHandler(RouteBean route) {
      _route = route;
    }

    @Override
    public void onSuccess(StopsForRouteBean stopsForRoute) {

      TabBarController rootController = MobileApplicationContext.getRootController();
      rootController.setSelectedIndex(0);

      System.out.println("showing stops for route");

      MapViewController map = MobileApplicationContext.getMapViewController();
      map.showStopsForRoute(_route, stopsForRoute);
    }

    @Override
    public void onFailure(Throwable arg0) {

    }
  }

  private static class AddressHandler implements LocationCallback {

    @Override
    public void onSuccess(JsArray<Placemark> locations) {

      List<Place> places = new ArrayList<Place>();
      for (int i = 0; i < locations.length(); i++)
        places.add(new PlacemarkPlaceImpl(locations.get(i)));

      MapViewController map = MobileApplicationContext.getMapViewController();

      if (places.size() == 0) {

      } else if (places.size() == 1) {
        map.showPlace(places.get(0));
      } else {
        map.showPlaces(places);
      }

    }

    @Override
    public void onFailure(int statusCode) {

    }
  }

}
