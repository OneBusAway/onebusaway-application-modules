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

import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationErrorEvent;
import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationEvent;
import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationHandler;
import org.onebusaway.webapp.gwt.geo_location_library.GeoLocationLibrary;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Timer;

public class LocationManager {

  private GeoLocationHandlerImpl _geoLocation = new GeoLocationHandlerImpl();

  private LatLng _lastSearchLocation = null;

  private LatLng _physicalLocation = null;

  private boolean _usePhysicalLocation = true;

  private HandlerManager _handlerManager = null;

  public LocationManager() {
    _geoLocation.run();
    _geoLocation.schedule(30 * 1000);
  }

  public LatLng getCurrentSearchLocation() {
    if( _usePhysicalLocation && _physicalLocation != null)
      return _physicalLocation;
    if( _lastSearchLocation != null)
      return _lastSearchLocation;
    return LatLng.newInstance(47.601533, -122.32933);
  }
  
  public LatLng getLastSearchLocation() {
    return _lastSearchLocation;
  }

  public void setLastSearchLocation(LatLng searchLocation) {
    _lastSearchLocation = searchLocation;
    _usePhysicalLocation = false;
  }

  public boolean hasPhysicalLocation() {
    return _physicalLocation != null;
  }
  
  public LatLng getPhysicalLocation() {
    return _physicalLocation;
  }
  
  public void setUsePhysicalLocation(boolean usePhysicalLocation) {
    _usePhysicalLocation = usePhysicalLocation;
  }

  public void addGeoLocationHandler(GeoLocationHandler handler) {
    handlers().addHandler(GeoLocationEvent.TYPE, handler);
  }

  private HandlerManager handlers() {
    return _handlerManager == null ? _handlerManager = new HandlerManager("")
        : _handlerManager;
  }

  private class GeoLocationHandlerImpl extends Timer implements
      GeoLocationHandler {

    @Override
    public void run() {
      if (!GeoLocationLibrary.isSupported())
        System.out.println("geo location not supported...");
      else
        GeoLocationLibrary.queryLocation(this);
    }

    @Override
    public void handleLocation(GeoLocationEvent event) {
      _physicalLocation = LatLng.newInstance(event.getLat(), event.getLon());
      handlers().fireEvent(event);
    }

    @Override
    public void handleError(GeoLocationErrorEvent event) {
      System.err.println("error: code=" + event.getCode() + " message="
          + event.getMessage());
    }
  }
}
