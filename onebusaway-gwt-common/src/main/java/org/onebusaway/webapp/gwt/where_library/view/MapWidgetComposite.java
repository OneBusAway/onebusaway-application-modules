package org.onebusaway.webapp.gwt.where_library.view;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;

public class MapWidgetComposite extends Composite implements RequiresResize {

  public MapWidgetComposite(MapWidget widget) {
    initWidget(widget);
  }

  public void onResize() {
    System.out.println("resize!");
    MapWidget widget = (MapWidget) getWidget();
    widget.checkResizeAndCenter();
  }
}
