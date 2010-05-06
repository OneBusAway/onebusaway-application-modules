package org.onebusaway.webapp.gwt.tripplanner_library.view;

import org.onebusaway.webapp.gwt.common.widgets.SpanPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerCssResource;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;

import com.google.gwt.maps.client.MapPaneType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class TinyInfoWindowMarker extends Overlay {

  private static TripPlannerCssResource _css = TripPlannerResources.INSTANCE.getCSS();
  
  private LatLng _point;
  private Widget _content;
  private MapWidget _map;
  private FlowPanel _panel;

  public TinyInfoWindowMarker(LatLng point, Widget content) {
    _point = point;
    _content = content;
  }

  @Override
  protected Overlay copy() {
    return new TinyInfoWindowMarker(_point, _content);
  }

  @Override
  protected void initialize(MapWidget map) {

    _panel = new FlowPanel();
    _panel.addStyleName(_css.tinyInfoWindow());

    SpanPanel leftSpan = new SpanPanel();
    leftSpan.addStyleName(_css.tinyInfoWindowLeftPanel());
    leftSpan.add(_content);
    _panel.add(leftSpan);

    SpanWidget rightSpan = new SpanWidget("");
    rightSpan.addStyleName(_css.tinyInfoWindowRightPanel());
    _panel.add(rightSpan);

    _map = map;
    _map.getPane(MapPaneType.MARKER_PANE).add(_panel);
  }

  @Override
  protected void redraw(boolean force) {
    Point point = _map.convertLatLngToDivPixel(_point);
    int x = point.getX();
    int y = point.getY();
    _map.getPane(MapPaneType.MARKER_PANE).setWidgetPosition(_panel, x, y);
  }

  @Override
  protected void remove() {
    _map.getPane(MapPaneType.MARKER_PANE).remove(_panel);
  }
}
