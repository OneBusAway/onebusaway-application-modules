package org.onebusaway.webapp.gwt.commute_calculator_application.control;

import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.commute_calculator_application.model.CommuteConstraints;
import org.onebusaway.webapp.gwt.oba_library.control.TimedOverlayManager;
import org.onebusaway.webapp.gwt.oba_library.model.TimedPolygonModel;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

public class ControlImpl implements Control {

  private Geocoder _geocoder = new Geocoder();

  private List<ControlListener> _listeners = new ArrayList<ControlListener>();

  @SuppressWarnings("unused")
  private TimedPolygonModel _model;

  private MapOverlayManager _mapOverlayManager;

  private TimedOverlayManager _timedRegionOverlayManager;

  public ControlImpl() {
    LatLngBounds view = LatLngBounds.newInstance();
    view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
    view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));
    _geocoder.setViewport(view);
  }

  public void addListener(ControlListener listener) {
    _listeners.add(listener);
  }

  public void setModel(TimedPolygonModel model) {
    _model = model;
  }

  public void setMapOverlayManager(MapOverlayManager mapOverlayManager) {
    _mapOverlayManager = mapOverlayManager;
    _timedRegionOverlayManager = new TimedOverlayManager();
    _timedRegionOverlayManager.setMapOverlayManager(_mapOverlayManager);
  }

  public void performQuery(String address, CommuteConstraints constraints) {
    System.out.println("query=" + address);
    _geocoder.getLocations(address, new GeocoderHandler(address, constraints));
  }

  public void performQuery(Placemark location, CommuteConstraints constraints) {
    WebappServiceAsync service = WebappServiceAsync.SERVICE;
    LatLng p = location.getPoint();
    System.out.println("address=" + location.getAddress());
    int time = 5;
    if (constraints.getMaxTripDuration() > 30)
      time = 10;
    service.getMinTravelTimeToStopsFrom(p.getLatitude(), p.getLongitude(),
        constraints, time, new OneBusAwayResultHandler());
  }

  private class GeocoderHandler implements LocationCallback {

    private CommuteConstraints _constraints;

    public GeocoderHandler(String address, CommuteConstraints constraints) {
      _constraints = constraints;
    }

    public void onSuccess(JsArray<Placemark> locations) {

      if (locations.length() == 1) {
        performQuery(locations.get(0), _constraints);
      } else {
        System.out.println("results=" + locations.length());
      }
    }

    public void onFailure(int statusCode) {
      System.err.println("error=" + statusCode);
    }
  }

  private class OneBusAwayResultHandler implements
      AsyncCallback<MinTransitTimeResult> {

    public void onSuccess(MinTransitTimeResult result) {
      System.out.println("here we go... BROKEN!");
      // _model.setData(result.getTimePolygons(),result.getTimes());
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }
  }
}
