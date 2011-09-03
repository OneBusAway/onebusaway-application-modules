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

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.tripplanning.ItineraryBean;
import org.onebusaway.transit_data.model.tripplanning.LegBean;
import org.onebusaway.transit_data.model.tripplanning.StreetLegBean;
import org.onebusaway.transit_data.model.tripplanning.TransitLegBean;
import org.onebusaway.webapp.gwt.common.widgets.SpanPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerCssResource;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.EncodedPolyline;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.PolyStyleOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.ui.Image;

public class TripBeanMapPresenter {

  private static TripPlannerCssResource _css = TripPlannerResources.INSTANCE.getCss();

  private MapWidget _map;

  private boolean _centerOnTrip = true;

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void setCenterOnTrip(boolean centerOnTrip) {
    _centerOnTrip = centerOnTrip;
  }

  public void displayTrip(ItineraryBean trip, List<Overlay> resultingOverlays) {

    resultingOverlays.clear();

    LatLngBounds bounds = LatLngBounds.newInstance();

    for (LegBean segment : trip.getLegs()) {

      String mode = segment.getMode();

      if (mode.equals("transit")) {

        TransitLegBean leg = segment.getTransitLeg();
        String path = leg.getPath();

        if (path != null) {

          List<CoordinatePoint> points = PolylineEncoder.decode(path);
          EncodedPolylineBean bean = PolylineEncoder.createEncodings(points);
          
          Polyline line = getPathAsPolyline(bean);
          PolyStyleOptions style = PolyStyleOptions.newInstance("#0000FF", 4,
              0.5);
          line.setStrokeStyle(style);
          resultingOverlays.add(line);

          addBoundsToBounds(line.getBounds(), bounds);
        }

        StopBean stop = leg.getFromStop();

        if (stop != null) {
          String routeName = leg.getRouteShortName();

          TripPlannerResources resources = TripPlannerResources.INSTANCE;
          SpanPanel w = new SpanPanel();
          w.addStyleName(_css.routeTinyInfoWindow());
          Image image = new Image(resources.getBus14x14().getUrl());
          image.addStyleName(_css.routeModeIcon());
          w.add(image);
          SpanWidget name = new SpanWidget(routeName);
          name.setStyleName(_css.routeName());
          w.add(name);

          LatLng point = LatLng.newInstance(stop.getLat(), stop.getLon());
          TinyInfoWindowMarker marker = new TinyInfoWindowMarker(point, w);
          resultingOverlays.add(marker);

          bounds.extend(point);
        }
      } else if (mode.equals("walk")) {
        List<StreetLegBean> streetLegs = segment.getStreetLegs();
        List<CoordinatePoint> allPoints = new ArrayList<CoordinatePoint>();
        for (StreetLegBean streetLeg : streetLegs) {
          String path = streetLeg.getPath();
          List<CoordinatePoint> points = PolylineEncoder.decode(path);
          allPoints.addAll(points);
        }
        EncodedPolylineBean polyline = PolylineEncoder.createEncodings(allPoints);
        Polyline line = getPathAsPolyline(polyline);
        PolyStyleOptions style = PolyStyleOptions.newInstance("#000000", 4, 0.8);
        line.setStrokeStyle(style);
        resultingOverlays.add(line);

        addBoundsToBounds(line.getBounds(), bounds);
      }
    }

    for (Overlay overlay : resultingOverlays)
      _map.addOverlay(overlay);

    if (_centerOnTrip && !bounds.isEmpty()) {
      _map.setCenter(bounds.getCenter());
      int zoom = _map.getBoundsZoomLevel(bounds);
      _map.setCenter(bounds.getCenter(), zoom);
    }
  }

  private Polyline getPathAsPolyline(EncodedPolylineBean path) {
    EncodedPolyline epl = EncodedPolyline.newInstance(path.getPoints(), 32,
        path.getLevels(3), 4);
    return Polyline.fromEncoded(epl);
  }

  private void addBoundsToBounds(LatLngBounds source, LatLngBounds dest) {
    if (source == null || source.isEmpty()) {
      System.out.println("empty bounds");
    } else {
      dest.extend(source.getNorthEast());
      dest.extend(source.getSouthWest());
    }
  }
}
