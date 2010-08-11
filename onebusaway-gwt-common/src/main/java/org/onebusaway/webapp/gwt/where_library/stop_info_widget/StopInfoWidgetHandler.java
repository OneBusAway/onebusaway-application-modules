package org.onebusaway.webapp.gwt.where_library.stop_info_widget;

import org.onebusaway.transit_data.model.RouteBean;

public interface StopInfoWidgetHandler {

  public void handleRouteClicked(RouteBean route);

  public void handleRealTimeLinkClicked();

  public void handleScheduleLinkClicked();
}
