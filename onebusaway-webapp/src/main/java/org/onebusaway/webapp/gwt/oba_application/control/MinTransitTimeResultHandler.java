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
package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.oba_library.model.TimedPolygonModel;

import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MinTransitTimeResultHandler implements
    AsyncCallback<MinTransitTimeResult> {

  private MapOverlayManager _mapManager;

  // private TimedRegionModel _timedRegionModel;

  private TimedPolygonModel _timedPolygonModel;

  private CommonControl _control;
  
  private boolean _showSearchGrid = false;

  /*
   * public void setTimedRegionModel(TimedRegionModel model) { _timedRegionModel
   * = model; }
   */

  public void setTimedPolygonModel(TimedPolygonModel model) {
    _timedPolygonModel = model;
  }

  public void setMapOverlayManager(MapOverlayManager manager) {
    _mapManager = manager;
  }

  public void setControl(CommonControl presenter) {
    _control = presenter;
  }

  public void onSuccess(MinTransitTimeResult result) {
    
    System.out.println("yeah: " + result.isComplete());

    _timedPolygonModel.setData(result.getTimePolygons(), result.getTimes(),
        result.isComplete());

    if (result.isComplete()) {
      _control.search(result);

      if (_showSearchGrid)
        showSearchGrid(result);
    }
  }

  public void onFailure(Throwable ex) {
    System.err.println("error performing OneBusAway query");
    ex.printStackTrace();
  }

  private void showSearchGrid(MinTransitTimeResult result) {
    for (final CoordinateBounds lb : result.getSearchGrid()) {
      LatLng a = LatLng.newInstance(lb.getMinLat(), lb.getMinLon());
      LatLng b = LatLng.newInstance(lb.getMaxLat(), lb.getMinLon());
      LatLng c = LatLng.newInstance(lb.getMaxLat(), lb.getMaxLon());
      LatLng d = LatLng.newInstance(lb.getMinLat(), lb.getMaxLon());
      LatLng[] points = {a, b, c, d, a};
      Polyline line = new Polyline(points);
      _mapManager.addOverlay(line, 10, 20);

      Marker marker = new Marker(b);
      marker.addMarkerClickHandler(new MarkerClickHandler() {
        public void onClick(MarkerClickEvent event) {
          System.out.println(lb.getMinLat() + " " + lb.getMinLon());
          System.out.println(lb.getMaxLat() + " " + lb.getMaxLon());
        }
      });

      _mapManager.addOverlay(marker, 10, 20);

    }
  }

}
