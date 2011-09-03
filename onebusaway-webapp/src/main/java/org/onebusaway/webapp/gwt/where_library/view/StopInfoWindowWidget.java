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
package org.onebusaway.webapp.gwt.where_library.view;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.client.StopPresenter;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class StopInfoWindowWidget extends Composite {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  DivElement _stopName;

  @UiField
  DivElement _stopDescription;

  @UiField
  FlowPanel _stopLinks;

  @UiField
  FlowPanel _routesPanel;

  @UiField(provided=true)
  final StopFinderCssResource _css;

  private StopFinderInterface _stopFinder;

  private TransitMapManager _transitMapManager;

  public StopInfoWindowWidget(StopFinderInterface stopFinder,
      TransitMapManager transitMapManager, StopBean stop, StopFinderCssResource css) {
    _css = css;
    _stopFinder = stopFinder;
    _transitMapManager = transitMapManager;
    
    initWidget(_uiBinder.createAndBindUi(this));

    handleTitleForStop(stop);
    handleLinksForStopInfoWindow(stop);
    handleRoutesForStopInfoWindow(stop);
  }

  protected void handleTitleForStop(StopBean stop) {

    String name = stop.getName();
    _stopName.setInnerText(name);

    String description = "Stop # " + StopPresenter.getCodeForStop(stop);

    if (stop.getDirection() != null)
      description += " - " + stop.getDirection() + " bound";

    _stopDescription.setInnerText(description);
  }

  protected void handleLinksForStopInfoWindow(StopBean bean) {

    RouteBean r = _transitMapManager.getSelectedRoute();
    String href = "stop.action?id=" + bean.getId().toString();

    if (r != null)
      href = href + "&route=" + r.getId();

    String html = "<div><a href=\"" + href
        + "\">Real-time arrival info</a></div>";

    String html2 = "<div><a href=\"schedule.action?id=" + bean.getId()
        + "\">Complete timetable</a></div>";

    _stopLinks.add(new HTML(html + html2));
  }

  protected FlowPanel getStopLinksPanel() {
    return _stopLinks;
  }

  protected void handleRoutesForStopInfoWindow(StopBean stop) {

    List<RouteBean> routes = stop.getRoutes();

    if (routes.isEmpty())
      return;

    List<RouteBean> shortNameRoutes = new ArrayList<RouteBean>();
    List<RouteBean> longNameRoutes = new ArrayList<RouteBean>();

    for (RouteBean route : routes) {
      String name = RoutePresenter.getNameForRoute(route);
      if (name.length() > 3)
        longNameRoutes.add(route);
      else
        shortNameRoutes.add(route);
    }

    if (!shortNameRoutes.isEmpty()) {

      FlowPanel shortNameRoutesPanel = new FlowPanel();
      shortNameRoutesPanel.addStyleName(_css.stopInfoWindowRoutesSubPanel());
      _routesPanel.add(shortNameRoutesPanel);

      for (final RouteBean route : shortNameRoutes) {
        SpanWidget w = new SpanWidget(RoutePresenter.getNameForRoute(route));
        w.addStyleName(_css.stopInfoWindowRouteShortNameEntry());
        w.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent arg0) {
            _stopFinder.queryRoute(route.getId());
          }
        });
        shortNameRoutesPanel.add(w);
      }
    }

    if (!longNameRoutes.isEmpty()) {

      FlowPanel longNameRoutesPanel = new FlowPanel();
      longNameRoutesPanel.addStyleName(_css.stopInfoWindowRoutesSubPanel());
      _routesPanel.add(longNameRoutesPanel);

      for (final RouteBean route : longNameRoutes) {
        String name = RoutePresenter.getNameForRoute(route);
        DivWidget w = new DivWidget(name);
        w.addStyleName(_css.stopInfoWindowRouteLongNameEntry());
        if (name.length() > 10)
          w.addStyleName(_css.stopInfoWindowRouteReallyLongNameEntry());
        w.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent arg0) {
            _stopFinder.queryRoute(route.getId());
          }
        });
        longNameRoutesPanel.add(w);
      }
    }
  }

  interface MyUiBinder extends UiBinder<Widget, StopInfoWindowWidget> {
  }
}
