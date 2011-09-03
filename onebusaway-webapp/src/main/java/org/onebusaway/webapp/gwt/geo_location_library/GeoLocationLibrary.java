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
package org.onebusaway.webapp.gwt.geo_location_library;

public class GeoLocationLibrary {

  private static boolean _checked = false;

  private static boolean _supported = false;
  
  public static boolean isSupported() {
    if (!_checked) {
      _supported = isGeoLocationSupported();
      _checked = true;
    }
    return _supported;
  }
  
  public static void queryLocation(GeoLocationHandler handler) {
    
    System.out.println("query");
    
    if (!isSupported()) {
      System.out.println("not supported");
      handler.handleError(new GeoLocationErrorEvent(-1, "geo location support not available"));
      return;
    }
    
    queryLocationNative(handler);
  }
  
  static GeoLocationEvent event(double timestamp, double lat, double lon) {
    return new GeoLocationEvent(timestamp, lat, lon);
  }
  
  static GeoLocationErrorEvent error(int code, String message){
    return new GeoLocationErrorEvent(code, message);
  }
  
  private static native boolean isGeoLocationSupported() /*-{  
    return $wnd.geo_position_js.init();
  }-*/;

  private static native void queryLocationNative(GeoLocationHandler handler) /*-{
    
    success_callback = function(p) {
      lat = p.coords.latitude;
      lon = p.coords.longitude;
      event = @org.onebusaway.webapp.gwt.geo_location_library.GeoLocationLibrary::event(DDD)(p.timestamp,lat,lon);
      handler.@org.onebusaway.webapp.gwt.geo_location_library.GeoLocationHandler::handleLocation(Lorg/onebusaway/webapp/gwt/geo_location_library/GeoLocationEvent;)(event);
    }
    
    error_callback = function(p) {
      event = @org.onebusaway.webapp.gwt.geo_location_library.GeoLocationLibrary::error(ILjava/lang/String;)(p.code,p.message);
      handler.@org.onebusaway.webapp.gwt.geo_location_library.GeoLocationHandler::handleError(Lorg/onebusaway/webapp/gwt/geo_location_library/GeoLocationErrorEvent;)(event);
    }
    
    try {
      $wnd.geo_position_js.getCurrentPosition(success_callback,error_callback,{enableHighAccuracy:true});
    }
    catch(p){
      alert(p);
    }    
  }-*/;
}
