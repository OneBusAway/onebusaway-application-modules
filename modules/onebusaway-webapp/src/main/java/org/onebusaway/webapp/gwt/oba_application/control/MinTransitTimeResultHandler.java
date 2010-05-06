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

    _timedPolygonModel.setData(result.getTimePolygons(), result.getTimes(),
        result.isComplete());

    if (result.isComplete()) {
      _control.search(result.getResultId(), result.getSearchGrid());

      if (false)
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
