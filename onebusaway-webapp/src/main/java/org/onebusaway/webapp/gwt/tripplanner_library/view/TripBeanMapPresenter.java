package org.onebusaway.webapp.gwt.tripplanner_library.view;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.tripplanner.DepartureSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.LocationSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.RideSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripSegmentBean;
import org.onebusaway.transit_data.model.tripplanner.WalkSegmentBean;
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

import java.util.List;

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

  public void displayTrip(TripPlanBean trip, List<Overlay> resultingOverlays) {

    resultingOverlays.clear();

    LatLngBounds bounds = LatLngBounds.newInstance();

    for (TripSegmentBean segment : trip.getSegments()) {

      if (segment instanceof LocationSegmentBean) {

        LocationSegmentBean lsb = (LocationSegmentBean) segment;
        LatLng p = LatLng.newInstance(lsb.getLat(), lsb.getLon());
        bounds.extend(p);

      } else if (segment instanceof DepartureSegmentBean) {

        DepartureSegmentBean depart = (DepartureSegmentBean) segment;
        StopBean stop = depart.getStop();
        String routeName = depart.getRouteShortName();

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

      } else if (segment instanceof WalkSegmentBean) {
        WalkSegmentBean walk = (WalkSegmentBean) segment;
        Polyline line = getPathAsPolyline(walk.getPath());
        PolyStyleOptions style = PolyStyleOptions.newInstance("#000000", 4, 0.8);
        line.setStrokeStyle(style);
        resultingOverlays.add(line);

        addBoundsToBounds(line.getBounds(), bounds);

      } else if (segment instanceof RideSegmentBean) {
        RideSegmentBean ride = (RideSegmentBean) segment;
        Polyline line = getPathAsPolyline(ride.getPath());
        PolyStyleOptions style = PolyStyleOptions.newInstance("#0000FF", 4, 0.5);
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
    dest.extend(source.getNorthEast());
    dest.extend(source.getSouthWest());
  }
}
