package org.onebusaway.webapp.gwt.geo_location_library;

import com.google.gwt.event.shared.GwtEvent;

public class GeoLocationEvent extends GwtEvent<GeoLocationHandler> {

  public static final Type<GeoLocationHandler> TYPE = new Type<GeoLocationHandler>();

  private double _timestamp;

  private double _lat;

  private double _lon;

  public GeoLocationEvent(double timestamp, double lat, double lon) {
    _timestamp = timestamp;
    _lat = lat;
    _lon = lon;
  }

  public double getTimestamp() {
    return _timestamp;
  }

  public double getLat() {
    return _lat;
  }

  public double getLon() {
    return _lon;
  }

  @Override
  protected void dispatch(GeoLocationHandler handler) {
    handler.handleLocation(this);
  }

  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<GeoLocationHandler> getAssociatedType() {
    return TYPE;
  }
}
