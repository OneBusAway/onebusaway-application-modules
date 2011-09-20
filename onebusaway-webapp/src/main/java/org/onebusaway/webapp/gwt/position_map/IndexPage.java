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
package org.onebusaway.webapp.gwt.position_map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.where_library.pages.WhereCommonPage;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class IndexPage extends WhereCommonPage {

  private static PositionMapCssResource _css = PositionMapResources.INSTANCE.getCSS();

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  private MapWidget _map;

  private MapOverlayManager _overlayManager;

  private TripPositionHandler _handler = new TripPositionHandler();

  public IndexPage(ContextManager contextManager) {

  }

  public Widget create(final Context context) throws PageException {
    _map = new MapWidget(_center, _zoom);
    _map.addStyleName(_css.map());
    _map.addControl(new LargeMapControl());
    _map.addControl(new MapTypeControl());
    _map.addControl(new ScaleControl());
    _map.setScrollWheelZoomEnabled(true);
    _map.addMapMoveEndHandler(new OurMapMoveHandler());

    _overlayManager = new MapOverlayManager(_map);

    return _map;
  }

  @Override
  public Widget update(Context context) throws PageException {
    return null;
  }

  private void refreshView() {
    _overlayManager.clear();
    if (_map.getZoomLevel() < 16)
      return;
    LatLngBounds llBounds = _map.getBounds();
    LatLng a = llBounds.getNorthEast();
    LatLng b = llBounds.getSouthWest();
    CoordinateBounds bounds = new CoordinateBounds(a.getLatitude(),
        a.getLongitude(), b.getLatitude(), b.getLongitude());

    TripsForBoundsQueryBean query = new TripsForBoundsQueryBean();
    query.setBounds(bounds);
    query.setTime(System.currentTimeMillis());
    query.getInclusion().setIncludeTripBean(true);

    WebappServiceAsync service = WebappServiceAsync.SERVICE;
    service.getTripsForBounds(query, _handler);
  }

  private class OurMapMoveHandler implements MapMoveEndHandler {

    @Override
    public void onMoveEnd(MapMoveEndEvent event) {
      refreshView();
    }
  }

  private class TripPositionHandler implements
      AsyncCallback<ListBean<TripDetailsBean>> {

    @Override
    public void onSuccess(ListBean<TripDetailsBean> result) {

      for (final TripDetailsBean bean : result.getList()) {
        TripStatusBean status = bean.getStatus();
        CoordinatePoint position = status.getLocation();
        final LatLng point = LatLng.newInstance(position.getLat(),
            position.getLon());
        Marker marker = new Marker(point);

        _overlayManager.addOverlay(marker);

        marker.addMarkerClickHandler(new MarkerClickHandler() {
          @Override
          public void onClick(MarkerClickEvent event) {
            FlowPanel panel = new FlowPanel();

            TripBean trip = bean.getTrip();
            RouteBean route = trip.getRoute();

            panel.add(new DivWidget("Route: " + route.getShortName()));
            panel.add(new DivWidget("Trip: " + trip.getId()));
            panel.add(new DivWidget("Destination: "
                + bean.getTrip().getTripHeadsign()));

            InfoWindow window = _map.getInfoWindow();
            window.open(point, new InfoWindowContent(panel));
          }
        });

      }
    }

    @Override
    public void onFailure(Throwable caught) {

    }
  }

}
