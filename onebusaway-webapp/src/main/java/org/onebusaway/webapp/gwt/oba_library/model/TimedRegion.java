package org.onebusaway.webapp.gwt.oba_library.model;

import com.google.gwt.maps.client.geom.LatLngBounds;

public class TimedRegion {

  private LatLngBounds _bounds;

  private int _time;

  public TimedRegion(LatLngBounds bounds, int time) {
    _bounds = bounds;
    _time = time;
  }

  public LatLngBounds getBounds() {
    return _bounds;
  }

  public int getTime() {
    return _time;
  }
}
