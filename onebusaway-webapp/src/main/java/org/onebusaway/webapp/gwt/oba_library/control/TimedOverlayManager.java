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
package org.onebusaway.webapp.gwt.oba_library.control;

import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.oba_application.model.QueryModel;
import org.onebusaway.webapp.gwt.oba_library.model.TimedPolygon;
import org.onebusaway.webapp.gwt.oba_library.model.TimedPolygonModel;
import org.onebusaway.webapp.gwt.oba_library.model.TimedRegion;
import org.onebusaway.webapp.gwt.oba_library.model.TimedRegionModel;
import org.onebusaway.webapp.gwt.oba_library.view.PatchMarker;

import com.google.gwt.dom.client.Style;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.PolyStyleOptions;
import com.google.gwt.maps.client.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

public class TimedOverlayManager {

  private List<Polygon> _overlays = new ArrayList<Polygon>();

  private MapOverlayManager _mapOverlayManager;

  public double[][] COOL_RGB = {
      {0, 1.0, 1.0}, {0.1111, 0.8889, 1.0}, {0.2222, 0.7778, 1.0}, {0.3333, 0.6667, 1.0}, {0.4444, 0.5556, 1.0},
      {0.5556, 0.4444, 1.0}, {0.6667, 0.3333, 1.0}, {0.7778, 0.2222, 1.0}, {0.8889, 0.1111, 1.0}, {1.0000, 0, 1.0}};
  public double[] COOL_ALPHA = {0.3};

  public double[][] RAINBOW_RGB = {
      {0.22, 0.08, 0.69}, {0.11, 0.11, 0.70}, {0.07, 0.25, 0.67}, {0.04, 0.38, 0.64}, {0, 0.60, 0.60}, {0, 0.69, 0.39},
      {0, 0.8, 0}, {0.4, 0.89, 0}, {0.62, 0.93, 0}, {0.8, 0.96, 0}, {1, 1, 0}, {1, .91, 0}, {1, 0.83, 0}, {1, 0.75, 0},
      {1, 0.67, 0}, {1, 0.57, 0}, {1, 0.45, 0}, {1, 0.29, 0}, {1, 0.0, 0}};
  public double[] RAINBOW_ALPHA = {0.3};

  public double[][] RED_RGB = {{1, 0, 0}};
  public double[] RED_ALPHA = {0.5, 0.45, 0.4, 0.35, 0.3, 0.25, 0.2};

  private Colormap _colormap = new Colormap(RAINBOW_RGB, RAINBOW_ALPHA);

  private QueryModel _queryModel;

  private ColorGradientControl _colorGradientControl;

  public void setMapOverlayManager(MapOverlayManager manager) {
    _mapOverlayManager = manager;
  }

  public void setColorGradientControl(ColorGradientControl control) {
    _colorGradientControl = control;
  }

  public void setQueryModel(QueryModel queryModel) {
    _queryModel = queryModel;
  }

  public ModelListener<TimedPolygonModel> getPolygonModelListener() {
    return new PolygonHandler();
  }

  public ModelListener<TimedRegionModel> getRegionModelListener() {
    return new RegionHandler();
  }

  public void clear() {

    for (Polygon marker : _overlays) {
      marker.setVisible(false);
      _mapOverlayManager.removeOverlay(marker);
    }
    _overlays.clear();
  }

  public void setVisible(boolean visible) {
    for( Polygon marker : _overlays)
      marker.setVisible(visible);
  }

  private class PolygonHandler implements ModelListener<TimedPolygonModel> {

    public void handleUpdate(TimedPolygonModel model) {

      clear();

      LatLngBounds bounds = LatLngBounds.newInstance();

      double maxTime = model.getMaxTime();

      for (TimedPolygon tp : model.getPolygons()) {

        Polygon poly = tp.getPolyline();

        String color = getColorForTime(tp.getTime(), maxTime);
        double opacity = getOpacityForTime(tp.getTime(), maxTime);

        poly.setFillStyle(PolyStyleOptions.newInstance(color, 1, opacity));
        poly.setStrokeStyle(PolyStyleOptions.newInstance(color, 1, opacity));

        _mapOverlayManager.addOverlay(poly);
        _overlays.add(poly);
        LatLngBounds b = poly.getBounds();
        bounds.extend(b.getNorthEast());
        bounds.extend(b.getSouthWest());
      }

      if (!bounds.isEmpty()) {
        MapWidget map = _mapOverlayManager.getMapWidget();
        if (model.isComplete()) {
          int zoom = map.getBoundsZoomLevel(bounds);
          map.setCenter(bounds.getCenter(), zoom);

          List<String> colors = new ArrayList<String>();
          for (int i = 0; i <= 6; i++) {
            double ratio = i / 6.0;
            colors.add(_colormap.getColor(ratio));
          }
          String fromLabel = "0 mins";
          String toLabel = model.getMaxTime() + " mins";
          _colorGradientControl.setGradient(colors, fromLabel, toLabel);
        } else {
          map.setCenter(_queryModel.getLocation());
        }

      }
    }

  }

  private class RegionHandler implements ModelListener<TimedRegionModel> {

    public void handleUpdate(TimedRegionModel model) {

      clear();

      double maxTime = model.getMaxTime();

      LatLngBounds bounds = LatLngBounds.newInstance();

      for (TimedRegion region : model.getRegions()) {

        String color = getColorForTime(region.getTime(), maxTime);

        LatLngBounds regionBounds = region.getBounds();
        PatchMarker marker = new PatchMarker(regionBounds);
        Style style = marker.getStyle();
        style.setProperty("backgroundColor", color);
        marker.addStyleName("patch");

        // _overlays.add(marker);
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
  }

  private String getColorForTime(int time, double maxTime) {
    double ratio = time / maxTime;
    ratio = Math.min(ratio, 1.0);
    ratio = Math.max(ratio, 0.0);
    return _colormap.getColor(ratio);
  }

  private double getOpacityForTime(int time, double maxTime) {

    double ratio = time / maxTime;
    ratio = Math.min(ratio, 1.0);
    ratio = Math.max(ratio, 0.0);

    return _colormap.getOpacity(ratio);
  }
}
