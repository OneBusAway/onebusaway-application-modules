package org.onebusaway.webapp.gwt.oba_library.model;

import com.google.gwt.maps.client.overlay.Polygon;

public class TimedPolygon {

  private Polygon _polygon;

  private int _time;

  public TimedPolygon(Polygon polygon, int time) {
    _polygon = polygon;
    _time = time;
  }

  public Polygon getPolyline() {
    return _polygon;
  }

  public int getTime() {
    return _time;
  }
}
