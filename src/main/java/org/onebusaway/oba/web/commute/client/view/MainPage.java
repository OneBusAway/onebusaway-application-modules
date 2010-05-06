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
package org.onebusaway.oba.web.commute.client.view;

import org.onebusaway.common.web.common.client.AbstractPageSource;
import org.onebusaway.common.web.common.client.PageException;
import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.widgets.MapWidgetMaxSizeResizeHandler;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainPage extends AbstractPageSource {

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private SearchWidget _searchWidget;

  private MapWidget _map;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void setSearchWidget(SearchWidget searchWidget) {
    _searchWidget = searchWidget;
  }

  @Override
  public Widget create(Context context) throws PageException {

    FlowPanel page = new FlowPanel();

    FlowPanel topPanel = new FlowPanel();
    topPanel.addStyleName("TopPanel");
    page.add(topPanel);

    topPanel.add(_searchWidget);

    HorizontalPanel mainPanel = new HorizontalPanel();
    mainPanel.addStyleName("HorizontalPanel");
    page.add(mainPanel);

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("LeftPanel");
    mainPanel.add(leftPanel);

    FlowPanel rightPanel = new FlowPanel();
    rightPanel.addStyleName("RightPanel");
    mainPanel.add(rightPanel);

    Widget mapPanel = MapWidgetMaxSizeResizeHandler.registerHandler(rightPanel, _map);
    rightPanel.add(mapPanel);
    
    _searchWidget.initialize();

    return page;
  }

  @Override
  public Widget update(Context context) throws PageException {
    return null;
  }
}
