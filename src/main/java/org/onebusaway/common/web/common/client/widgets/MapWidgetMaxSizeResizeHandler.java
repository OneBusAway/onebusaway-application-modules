package org.onebusaway.common.web.common.client.widgets;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.Widget;

public class MapWidgetMaxSizeResizeHandler implements WindowResizeListener {

  private Widget _parent;

  private Widget _mapPanel;

  private MapWidget _map;

  public static Widget registerHandler(Widget parent, MapWidget map) {

    DivPanel mapPanel = new DivPanel();
    mapPanel.setSize("100%", "100%");
    mapPanel.add(map);

    map.setSize("100%", "100%");

    final MapWidgetMaxSizeResizeHandler handler = new MapWidgetMaxSizeResizeHandler(parent, mapPanel, map);
    DeferredCommand.addCommand(new Command() {

      public void execute() {
        handler.onWindowResized(Window.getClientWidth(), Window.getClientHeight());
        Window.addWindowResizeListener(handler);
      }
    });

    return mapPanel;
  }

  private MapWidgetMaxSizeResizeHandler(Widget parent, Widget mapPanel, MapWidget map) {
    _parent = parent;
    _mapPanel = mapPanel;
    _map = map;
  }

  public void onWindowResized(int x, int y) {

    System.out.println("GO! w=" + _parent.getOffsetWidth() + " h=" + _parent.getOffsetHeight());

    _mapPanel.setWidth(_parent.getOffsetWidth() + "px");
    _mapPanel.setHeight(_parent.getOffsetHeight() + "px");

    _map.checkResizeAndCenter();
  }
}
