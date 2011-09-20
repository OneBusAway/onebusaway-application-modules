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
package org.onebusaway.webapp.gwt.common.control;

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
    if (_view != null)
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
