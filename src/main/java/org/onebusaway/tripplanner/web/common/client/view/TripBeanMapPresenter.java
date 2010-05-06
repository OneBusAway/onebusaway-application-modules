package org.onebusaway.tripplanner.web.common.client.view;

import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.widgets.SpanPanel;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.tripplanner.web.common.client.model.DepartureSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.LocationSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.RideSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripSegmentBean;
import org.onebusaway.tripplanner.web.common.client.model.WalkSegmentBean;
import org.onebusaway.tripplanner.web.common.client.resources.TripPlannerResources;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.overlay.PolyStyleOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.ui.Image;

import java.util.List;

public class TripBeanMapPresenter {

  private MapWidget _map;

  private boolean _centerOnTrip = true;

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void setCenterOnTrip(boolean centerOnTrip) {
    _centerOnTrip = centerOnTrip;
  }

  public void displayTrip(TripBean trip, List<Overlay> resultingOverlays) {

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
        String routeName = depart.getRouteName();

        TripPlannerResources resources = TripPlannerResources.INSTANCE;
        SpanPanel w = new SpanPanel();
        w.addStyleName("RouteTinyInfoWindow");
        Image image = new Image(resources.getBus14x14().getUrl());
        image.addStyleName("RouteModeIcon");
        w.add(image);
        SpanWidget name = new SpanWidget(routeName);
        name.setStyleName("RouteName");
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

  private Polyline getPathAsPolyline(PathBean path) {
    double[] lat = path.getLat();
    double[] lon = path.getLon();
    LatLng[] points = new LatLng[lat.length];
    for (int i = 0; i < lat.length; i++)
      points[i] = LatLng.newInstance(lat[i], lon[i]);
    return new Polyline(points);
  }

  private void addBoundsToBounds(LatLngBounds source, LatLngBounds dest) {
    dest.extend(source.getNorthEast());
    dest.extend(source.getSouthWest());
  }
}
