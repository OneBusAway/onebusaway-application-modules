package org.onebusaway.common.web.common.client.control;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class GeocoderHelper {

  private GeocoderResultListener _listener;

  private LatLngBounds _view;

  public void setListener(GeocoderResultListener listener) {
    _listener = listener;
  }

  public void setViewport(LatLngBounds view) {
    _view = view;
  }

  public void query(String address) {
    Geocoder geocoder = new Geocoder();
    if (_view == null)
      geocoder.setViewport(_view);
    geocoder.getLocations(address, new LocationHandler());
  }

  private class LocationHandler implements LocationCallback {

    public void onSuccess(JsArray<Placemark> placemarks) {

      List<Placemark> inBounds = new ArrayList<Placemark>(placemarks.length());

      for (int i = 0; i < placemarks.length(); i++) {
        Placemark mark = placemarks.get(i);
        System.out.println("  " + mark.getAddress());
        if (_view == null || _view.containsLatLng(mark.getPoint()))
          inBounds.add(mark);
      }

      if (inBounds.size() == 0) {
        _listener.setNoQueryLocations();
      } else if (inBounds.size() == 1) {
        Placemark mark = inBounds.get(0);
        LatLng point = mark.getPoint();
        _listener.setQueryLocation(point);
      } else {
        _listener.setTooManyQueryLocations(inBounds);
      }
    }

    public void onFailure(int statusCode) {
      _listener.setErrorOnQueryLocation();
    }
  }
}
