package org.onebusaway.webapp.gwt.where_library.stop_info_widget;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

public interface StopInfoWidgetHandler {

  public void handleRouteClicked(RouteBean route);

  public void handleRealTimeLinkClicked(StopBean stop);

  public void handleScheduleLinkClicked(StopBean stop);
}
