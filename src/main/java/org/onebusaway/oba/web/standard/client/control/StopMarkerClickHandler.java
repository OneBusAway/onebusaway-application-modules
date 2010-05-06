/**
 * 
 */
package org.onebusaway.oba.web.standard.client.control;

import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.FlowPanel;

import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.oba.web.common.client.model.TimedStopBean;

class StopMarkerClickHandler implements MarkerClickHandler {

  private TimedStopBean _bean;

  private LatLng _point;

  private MapOverlayManager _mapManager;

  public StopMarkerClickHandler(MapOverlayManager manager, TimedStopBean bean,
      LatLng point) {
    _mapManager = manager;
    _bean = bean;
    _point = point;
  }

  public void onClick(MarkerClickEvent event) {

    StopBean stop = _bean.getStop();

    FlowPanel panel = new FlowPanel();
    panel.add(new DivWidget(stop.getName()));
    panel.add(new DivWidget("Stop # " + stop.getId()));
    panel.add(new DivWidget("Travel Time: " + (_bean.getTime() / 60) + " mins"));

    MapWidget map = _mapManager.getMapWidget();
    InfoWindow window = map.getInfoWindow();
    window.open(_point, new InfoWindowContent(panel));
  }

}