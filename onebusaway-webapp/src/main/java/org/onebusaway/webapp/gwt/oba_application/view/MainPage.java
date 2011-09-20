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
package org.onebusaway.webapp.gwt.oba_application.view;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextListener;
import org.onebusaway.webapp.gwt.oba_application.control.CommonControl;
import org.onebusaway.webapp.gwt.where_library.view.MapWidgetComposite;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ResizableDockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainPage extends Composite implements ContextListener {

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private CommonControl _control;

  /*****************************************************************************
   * Widgets
   ****************************************************************************/

  private MapWidget _map;

  @UiField
  ResizableDockLayoutPanel _dockLayoutPanel;

  @UiField(provided = true)
  SearchWidget _searchWidget;

  @UiField
  FlowPanel _resultsPanel;

  @UiField
  MapWidgetComposite _mapPanel;

  private List<Widget> _resultPanelWidgets = new ArrayList<Widget>();

  /*
   * private Widget _welcomeWidget;
   * 
   * private Widget _addressLookupWidget;
   * 
   * private Widget _resultsTableWidget;
   * 
   * private Widget _activeResultWidget;
   * 
   * private Widget _plansWidget;
   */

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public MainPage() {

  }

  public void setControl(CommonControl control) {
    _control = control;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void setSearchWidget(SearchWidget searchWidget) {
    _searchWidget = searchWidget;
  }

  public void addResultsPanelWidget(Widget widget) {
    _resultPanelWidgets.add(widget);
  }

  public void initialize() {

    System.out.println("=================== creating results page...");

    // initWidget(_uiBinder.createAndBindUi(this));

    _dockLayoutPanel = new ResizableDockLayoutPanel(Unit.EM);
    _resultsPanel = new FlowPanel();
    _mapPanel = makeMapPanel();

    _dockLayoutPanel.addNorth(_searchWidget, 4);
    _dockLayoutPanel.addWest(_resultsPanel, 18);
    _dockLayoutPanel.add(_mapPanel);

    initWidget(_dockLayoutPanel);

    _searchWidget.setDockLayoutPanelParent(_dockLayoutPanel);

    for (Widget widget : _resultPanelWidgets)
      _resultsPanel.add(widget);

    _map.addControl(new LargeMapControl());
    _map.addControl(new MapTypeControl());
    _map.addControl(new ScaleControl());
    _map.setScrollWheelZoomEnabled(true);

    // We delay initialization of the map
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        _map.checkResizeAndCenter();
      }
    });
  }

  @Override
  public void onContextChanged(Context context) {
    _control.handleContext(context);
  }

  @UiFactory
  MapWidgetComposite makeMapPanel() {
    return new MapWidgetComposite(_map);
  }
}
