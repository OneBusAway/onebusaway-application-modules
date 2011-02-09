package org.onebusaway.webapp.gwt.geo_location_library;

import com.google.gwt.event.shared.EventHandler;

public interface GeoLocationHandler extends EventHandler {
  public void handleLocation(GeoLocationEvent event);

  public void handleError(GeoLocationErrorEvent geoLocationErrorEvent);
}
