package org.onebusaway.oba.web.standard.client.control;

import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.oba.web.common.client.model.LocationBounds;
import org.onebusaway.oba.web.common.client.model.MinTransitTimeResult;
import org.onebusaway.oba.web.common.client.model.TimedRegionModel;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MinTransitTimeResultHandler implements AsyncCallback<MinTransitTimeResult> {

  private MapOverlayManager _mapManager;

  private TimedRegionModel _timedRegionModel;

  private OneBusAwayStandardPresenter _presenter;

  public void setTimedRegionModel(TimedRegionModel model) {
    _timedRegionModel = model;
  }

  public void setMapOverlayManager(MapOverlayManager manager) {
    _mapManager = manager;
  }

  public void setPresenter(OneBusAwayStandardPresenter presenter) {
    _presenter = presenter;
  }

  public void onSuccess(MinTransitTimeResult result) {

    _timedRegionModel.setData(result.getTimeGrid(), result.getTimes());

    LatLngBounds fullBounds = _timedRegionModel.getBounds();

    if (!fullBounds.isEmpty()) {
      MapWidget map = _mapManager.getMapWidget();
      int zoom = map.getBoundsZoomLevel(fullBounds);
      map.setCenter(fullBounds.getCenter(), zoom);
    }

    if (false)
      showSearchGrid(result);

    _presenter.search(result.getResultId(), result.getSearchGrid());
  }

  public void onFailure(Throwable ex) {
    System.err.println("error performing OneBusAway query");
    ex.printStackTrace();
  }

  private void showSearchGrid(MinTransitTimeResult result) {
    for (final LocationBounds lb : result.getSearchGrid()) {
      LatLng a = LatLng.newInstance(lb.getLatMin(), lb.getLonMin());
      LatLng b = LatLng.newInstance(lb.getLatMax(), lb.getLonMin());
      LatLng c = LatLng.newInstance(lb.getLatMax(), lb.getLonMax());
      LatLng d = LatLng.newInstance(lb.getLatMin(), lb.getLonMax());
      LatLng[] points = {a, b, c, d, a};
      Polyline line = new Polyline(points);
      _mapManager.addOverlay(line, 10, 20);

      Marker marker = new Marker(b);
      marker.addMarkerClickHandler(new MarkerClickHandler() {
        public void onClick(MarkerClickEvent event) {
          System.out.println(lb.getLatMin() + " " + lb.getLonMin());
          System.out.println(lb.getLatMax() + " " + lb.getLonMax());
        }
      });

      _mapManager.addOverlay(marker, 10, 20);

    }
  }
}
