package org.onebusaway.oba.web.commute.client.control;

import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.oba.web.common.client.control.TimedRegionOverlayManager;
import org.onebusaway.oba.web.common.client.model.MinTransitTimeResult;
import org.onebusaway.oba.web.common.client.model.TimedRegionModel;
import org.onebusaway.oba.web.common.client.rpc.OneBusAwayWebServiceAsync;
import org.onebusaway.oba.web.commute.client.model.CommuteConstraints;

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

  private TimedRegionModel _model;

  private MapOverlayManager _mapOverlayManager;

  private TimedRegionOverlayManager _timedRegionOverlayManager;

  public ControlImpl() {
    LatLngBounds view = LatLngBounds.newInstance();
    view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
    view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));
    _geocoder.setViewport(view);
  }

  public void addListener(ControlListener listener) {
    _listeners.add(listener);
  }

  public void setModel(TimedRegionModel model) {
    _model = model;
  }

  public void setMapOverlayManager(MapOverlayManager mapOverlayManager) {
    _mapOverlayManager = mapOverlayManager;
    _timedRegionOverlayManager = new TimedRegionOverlayManager();
    _timedRegionOverlayManager.setMapOverlayManager(_mapOverlayManager);
  }

  public void performQuery(String address, CommuteConstraints constraints) {
    System.out.println("query=" + address);
    _geocoder.getLocations(address, new GeocoderHandler(address, constraints));
  }

  public void performQuery(Placemark location, CommuteConstraints constraints) {
    OneBusAwayWebServiceAsync service = OneBusAwayWebServiceAsync.SERVICE;
    LatLng p = location.getPoint();
    System.out.println("address=" + location.getAddress());
    service.getMinTravelTimeToStopsFrom(p.getLatitude(), p.getLongitude(), constraints, new OneBusAwayResultHandler());
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

  private class OneBusAwayResultHandler implements AsyncCallback<MinTransitTimeResult> {

    public void onSuccess(MinTransitTimeResult result) {
      System.out.println("here we go...");
      _model.setData(result.getTimeGrid(), result.getTimes());
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }
  }
}
