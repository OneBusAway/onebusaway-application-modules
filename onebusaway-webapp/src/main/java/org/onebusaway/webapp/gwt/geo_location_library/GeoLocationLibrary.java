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
