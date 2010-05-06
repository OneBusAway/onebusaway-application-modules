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
package org.onebusaway.oba.web.standard.client.view;

import org.onebusaway.common.web.common.client.AbstractPageSource;
import org.onebusaway.common.web.common.client.PageException;
import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.MapWidgetMaxSizeResizeHandler;
import org.onebusaway.oba.web.standard.client.control.OneBusAwayStandardPresenter;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class MainPage extends AbstractPageSource {

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private OneBusAwayStandardPresenter _presenter;

  /*****************************************************************************
   * Widgets
   ****************************************************************************/

  private MapWidget _map;

  private SearchWidget _searchWidget;

  private Widget _addressLookupWidget;

  private Widget _resultsTableWidget;

  private Widget _activeResultWidget;

  private Widget _plansWidget;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public MainPage() {

  }

  public void setPresenter(OneBusAwayStandardPresenter presenter) {
    _presenter = presenter;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void setSearchWidget(SearchWidget searchWidget) {
    _searchWidget = searchWidget;
  }

  public void setAddressLookupWidget(Widget widget) {
    _addressLookupWidget = widget;
  }

  public void setResultsTableWidget(Widget resultsTableWidget) {
    _resultsTableWidget = resultsTableWidget;
  }

  public void setActiveResultWidget(Widget activeResultWidget) {
    _activeResultWidget = activeResultWidget;
  }

  public void setTripPlanResultTable(Widget plansWidget) {
    _plansWidget = plansWidget;
  }

  public Widget create(final Context context) throws PageException {

    System.out.println("=================== creating results page...");

    FlowPanel panel = new FlowPanel();

    panel.addStyleName("OneBusAway");

    FlowPanel topPanel = new FlowPanel();
    panel.add(topPanel);

    topPanel.add(_searchWidget);

    Grid hp = new Grid(1, 2);
    hp.addStyleName("OneBusAway-MainPanel");
    panel.add(hp);

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("OneBusAway-LeftPanel");
    hp.setWidget(0, 0, leftPanel);
    hp.getCellFormatter().addStyleName(0, 0, "OneBusAway-MainPanel-LeftPanel");
    hp.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);

    leftPanel.add(_addressLookupWidget);
    leftPanel.add(_resultsTableWidget);
    leftPanel.add(_activeResultWidget);
    leftPanel.add(_plansWidget);

    DivPanel mapPanel = new DivPanel();
    mapPanel.addStyleName("OneBusAway-MapPanel");
    hp.setWidget(0, 1, mapPanel);
    hp.getCellFormatter().addStyleName(0, 1, "OneBusAway-MainPanel-RightPanel");

    Widget mapWrapper = MapWidgetMaxSizeResizeHandler.registerHandler(mapPanel, _map);
    mapPanel.add(mapWrapper);

    Window.setTitle("One Bus Away");

    update(context);

    // return view;
    return panel;
  }

  @Override
  public Widget update(Context context) throws PageException {
    _presenter.handleContext(context);
    return null;
  }
}
