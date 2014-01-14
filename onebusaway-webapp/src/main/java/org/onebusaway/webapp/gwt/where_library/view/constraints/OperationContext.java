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
