package org.onebusaway.webapp.gwt.where_library;

import org.onebusaway.geospatial.model.CoordinateBounds;

import com.google.gwt.core.client.JavaScriptObject;

public class OBAConfig extends JavaScriptObject {

  private static native OBAConfig getInternalConfig() /*-{ return $wnd.OBA.Config }-*/;

  private static OBAConfig _instance = getInternalConfig();

  public static OBAConfig getConfig() {
    return _instance;
  }

  protected OBAConfig() {

  }

  public final native boolean hasDefaultServiceArea() /*-{ return this.hasDefaultServiceArea; }-*/;

  public final native double getCenterLat() /*-{ return this.centerLat; }-*/;

  public final native double getCenterLon() /*-{ return this.centerLon; }-*/;

  public final native double getSpanLat() /*-{ return this.spanLat; }-*/;

  public final native double getSpanLon() /*-{ return this.spanLon; }-*/;

  public final CoordinateBounds getBounds() {
    double lat = getCenterLat();
    double lon = getCenterLon();
    double latSpan = getSpanLat() / 2;
    double lonSpan = getSpanLon() / 2;
    return new CoordinateBounds(lat - latSpan, lon - lonSpan, lat + latSpan,
        lon + lonSpan);
  }
}
