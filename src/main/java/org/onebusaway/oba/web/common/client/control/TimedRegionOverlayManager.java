package org.onebusaway.oba.web.common.client.control;

import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.common.web.common.client.model.ModelListener;
import org.onebusaway.oba.web.common.client.model.TimedRegion;
import org.onebusaway.oba.web.common.client.model.TimedRegionModel;
import org.onebusaway.oba.web.common.client.view.PatchMarker;

import com.google.gwt.dom.client.Style;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLngBounds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimedRegionOverlayManager implements ModelListener<TimedRegionModel> {

  private Map<TimedRegion, PatchMarker> _overlays = new HashMap<TimedRegion, PatchMarker>();

  private MapOverlayManager _mapOverlayManager;

  public void setMapOverlayManager(MapOverlayManager manager) {
    _mapOverlayManager = manager;
  }

  public void clear() {
    for (PatchMarker marker : _overlays.values())
      _mapOverlayManager.removeOverlay(marker);
    _overlays.clear();
  }

  public void handleUpdate(TimedRegionModel model) {

    clear();

    List<TimedRegion> regions = model.getRegions();
    double maxTime = model.getMaxTime();

    Set<TimedRegion> toRemove = new HashSet<TimedRegion>(_overlays.keySet());
    toRemove.removeAll(regions);

    for (TimedRegion region : toRemove) {
      PatchMarker marker = _overlays.remove(region);
      if (marker != null)
        _mapOverlayManager.removeOverlay(marker);
    }

    Set<TimedRegion> toAdd = new HashSet<TimedRegion>(regions);
    toAdd.removeAll(_overlays.keySet());

    LatLngBounds bounds = LatLngBounds.newInstance();

    for (TimedRegion region : toAdd) {

      double ratio = region.getTime() / maxTime;
      ratio = Math.min(ratio, 1.0);
      ratio = Math.max(ratio, 0.0);

      int r = (int) (ratio * 255);
      int g = 255 - r;
      int b = 255;
      String color = getColor(r, g, b);

      LatLngBounds regionBounds = region.getBounds();
      PatchMarker marker = new PatchMarker(regionBounds);
      marker.addStyleName("patch");
      Style style = marker.getStyle();
      style.setProperty("background-color", color);

      _overlays.put(region, marker);
      _mapOverlayManager.addOverlay(marker, 10, 15);

      bounds.extend(regionBounds.getNorthEast());
      bounds.extend(regionBounds.getSouthWest());
    }

    if (!bounds.isEmpty()) {
      MapWidget map = _mapOverlayManager.getMapWidget();
      int zoom = map.getBoundsZoomLevel(bounds);
      map.setCenter(bounds.getCenter(), zoom);
    }
  }

  private String getColor(int r, int g, int b) {
    return "#" + hex(r) + hex(g) + hex(b);
  }

  private String hex(int value) {
    String s = Integer.toHexString(value);
    if (s.length() == 1)
      s = "0" + s;
    return s;
  }
}
