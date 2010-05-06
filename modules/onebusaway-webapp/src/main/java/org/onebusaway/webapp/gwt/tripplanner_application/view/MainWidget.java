/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.webapp.gwt.tripplanner_application.view;

import org.onebusaway.webapp.gwt.common.widgets.MapWidgetMaxSizeResizeHandler;
import org.onebusaway.webapp.gwt.tripplanner_library.view.TripPlanResultTablePresenter;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainWidget extends HorizontalPanel {

  private SearchWidget _searchWidget;

  private MapWidget _map;

  private TripPlanResultTablePresenter _resultsWidget;

  public void setSearchWidget(SearchWidget searchWidget) {
    _searchWidget = searchWidget;
  }

  public void setResultsWidget(TripPlanResultTablePresenter resultsWidget) {
    _resultsWidget = resultsWidget;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void initialize() {

    addStyleName("HorizontalPanel");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("LeftPanel");
    add(leftPanel);

    leftPanel.add(_searchWidget);
    leftPanel.add(_resultsWidget.getWidget());

    FlowPanel rightPanel = new FlowPanel();
    rightPanel.addStyleName("RightPanel");
    add(rightPanel);

    Widget mapWrapper = MapWidgetMaxSizeResizeHandler.registerHandler(rightPanel, _map);
    rightPanel.add(mapWrapper);
  }
}
