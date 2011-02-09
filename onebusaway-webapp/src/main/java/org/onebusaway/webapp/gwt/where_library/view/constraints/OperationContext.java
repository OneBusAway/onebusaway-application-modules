package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.webapp.gwt.where_library.view.StopFinderInterface;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderWidget;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.ui.Panel;

public class OperationContext {

  private final StopFinderWidget _widget;

  private final boolean _locationSet;

  public OperationContext(StopFinderWidget widget, boolean locationSet) {
    _widget = widget;
    _locationSet = locationSet;
  }

  public StopFinderWidget getWidget() {
    return _widget;
  }

  public StopFinderInterface getStopFinder() {
    return _widget.getStopFinder();
  }

  public MapWidget getMap() {
    return _widget.getMapWidget();
  }

  public TransitMapManager getTransitMapManager() {
    return _widget.getTransitMapManager();
  }

  public Panel getPanel() {
    return _widget.getResultsPanel();
  }

  public boolean isLocationSet() {
    return _locationSet;
  }

}
