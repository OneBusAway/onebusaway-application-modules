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
package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.webapp.gwt.common.AbstractPageSource;
import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.layout.Box;
import org.onebusaway.webapp.gwt.common.layout.BoxLayoutManager;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.oba_application.control.CommonControl;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends AbstractPageSource {

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private CommonControl _control;

  private BoxLayoutManager _panelLayout;

  /*****************************************************************************
   * Widgets
   ****************************************************************************/

  private MapWidget _map;

  private Widget _searchWidget;

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

  private FlowPanel _panel;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public MainPage() {

  }

  public void setControl(CommonControl control) {
    _control = control;
  }

  public void setLayoutManager(BoxLayoutManager layoutManager) {
    _panelLayout = layoutManager;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void setSearchWidget(Widget searchWidget) {
    _searchWidget = searchWidget;
  }

  public void addResultsPanelWidget(Widget widget) {
    _resultPanelWidgets.add(widget);
  }

  public Widget create(final Context context) throws PageException {

    System.out.println("=================== creating results page...");

    _panel = new FlowPanel();

    _panel.addStyleName("OneBusAway");

    FlowPanel topPanel = new FlowPanel();
    _panel.add(topPanel);

    topPanel.add(_searchWidget);

    FlowPanel mainPanel = new FlowPanel();
    mainPanel.addStyleName(getStyleName("Wrapper"));
    _panel.add(mainPanel);

    final DivPanel mainMapPanel = new DivPanel();
    mainMapPanel.addStyleName(getStyleName("MainMapPanel"));
    mainPanel.add(mainMapPanel);

    final DivPanel mapWrapper = new DivPanel();
    mapWrapper.addStyleName(getStyleName("MapWrapper"));
    mainMapPanel.add(mapWrapper);

    FlowPanel resultsWrapper = new FlowPanel();
    resultsWrapper.addStyleName(getStyleName("ResultsWrapper"));
    mainPanel.add(resultsWrapper);

    FlowPanel resultsPanel = new FlowPanel();
    resultsPanel.addStyleName(getStyleName("ResultsPanel"));
    resultsWrapper.add(resultsPanel);

    for (Widget widget : _resultPanelWidgets)
      resultsPanel.add(widget);

    Box lowerBound = getLowerBoundForWidget();
    Box target = Box.minus(lowerBound, Box.bottom(topPanel));
    _panelLayout.addSetHeightConstraint(target, mapWrapper, resultsWrapper);
    _panelLayout.refresh();

    // We delay initialization of the map
    DeferredCommand.addCommand(new Command() {
      public void execute() {

        _map.addStyleName(getStyleName("Map"));
        _map.addControl(new LargeMapControl());
        _map.addControl(new MapTypeControl());
        _map.addControl(new ScaleControl());
        mapWrapper.add(_map);

        _panelLayout.addMapWidget(_map);
        _panelLayout.refresh();
      }
    });

    Window.setTitle("One Bus Away");

    update(context);

    // return view;
    return _panel;
  }

  @Override
  public Widget update(Context context) throws PageException {
    _control.handleContext(context);
    return null;
  }

  protected String getStyleName(String name) {
    return "OneBusAway-" + name;
  }

  protected Box getLowerBoundForWidget() {
    try {
      Dictionary dictionary = Dictionary.getDictionary("OneBusAwayConfig");
      if (dictionary != null) {
        String elementId = dictionary.get("BoundingElementId");
        if (elementId != null) {
          Document doc = Document.get();
          Element element = doc.getElementById(elementId);
          if (element != null) {
            return Box.bottom(element);
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println("no config specified: using default");
    return Box.bottom(_panel.getElement());
  }

}
