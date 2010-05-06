package org.onebusaway.webapp.gwt.geo_location_library;

import com.google.gwt.event.shared.GwtEvent;

public class GeoLocationErrorEvent extends GwtEvent<GeoLocationHandler> {

  public static final Type<GeoLocationHandler> TYPE = new Type<GeoLocationHandler>();

  private int _code;

  private String _message;

  public GeoLocationErrorEvent(int code, String message) {
    _code = code;
    _message = message;
  }

  public int getCode() {
    return _code;
  }

  public String getMessage() {
    return _message;
  }

  @Override
  protected void dispatch(GeoLocationHandler handler) {
    handler.handleError(this);
  }

  @Override
  public final com.google.gwt.event.shared.GwtEvent.Type<GeoLocationHandler> getAssociatedType() {
    return TYPE;
  }
}
