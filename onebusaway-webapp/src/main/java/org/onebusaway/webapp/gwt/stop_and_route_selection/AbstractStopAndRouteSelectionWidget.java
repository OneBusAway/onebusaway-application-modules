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
package org.onebusaway.webapp.gwt.stop_and_route_selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextImpl;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderCssResource;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderInterface;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderPresenter;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderWidget;
import org.onebusaway.webapp.gwt.where_library.view.StopInfoWindowWidget;
import org.onebusaway.webapp.gwt.where_library.view.constraints.OperationContext;
import org.onebusaway.webapp.gwt.where_library.view.constraints.OperationHandler;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ModalLayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class AbstractStopAndRouteSelectionWidget extends Composite {

  private static WebappServiceAsync _service = WebappServiceAsync.SERVICE;

  private StopRemovedHandler _stopRemovedHandler = new StopRemovedHandler();

  private RouteHandler _routeHandler = new RouteHandler();

  @UiField
  public MyStyle style;
  
  @UiField
  public Anchor _addStopAnchor;

  @UiField
  public FlowPanel _stopsPanel;

  @UiField
  public FlowPanel _routesPanel;

  protected Map<String, StopBean> _stopsById = new HashMap<String, StopBean>();

  protected Map<String, Boolean> _routeSelectionById = new HashMap<String, Boolean>();

  private boolean _routesSelectedByDefault = true;

  private StopBean _stop;

  private ModalLayoutPanel _dialog;

  public void addStop(StopBean stop) {
    _stopsById.put(stop.getId(), stop);
    refresh();
  }

  public void removeStop(StopBean stop) {
    _stopsById.remove(stop.getId());
    refresh();
  }

  @UiHandler("_addStopAnchor")
  public void handleAddStopClick(ClickEvent e) {
    e.preventDefault();

    _stop = null;

    _dialog = new ModalLayoutPanel();

    StopFinderWidgetExtension widget = new StopFinderWidgetExtension();

    final StopFinderPresenter stopFinder = new StopFinderPresenter();
    stopFinder.setDefaultOperationHandler(new DefaultOperationHandler());

    stopFinder.setWidget(widget);
    widget.setStopFinder(stopFinder);

    widget.addStyleName(style.StopFinderDialog());

    _dialog.add(widget);
    _dialog.setWidgetLeftRight(widget, 5, Unit.PCT, 5, Unit.PCT);
    _dialog.setWidgetTopBottom(widget, 10, Unit.PCT, 10, Unit.PCT);

    _dialog.addCloseHandler(new CloseHandler<ModalLayoutPanel>() {
      @Override
      public void onClose(CloseEvent<ModalLayoutPanel> arg0) {
        if (_stop != null)
          addStop(_stop);
      }
    });

    _dialog.show();

    Context context = new ContextImpl();

    if (!_stopsById.isEmpty()) {
      CoordinateBounds b = new CoordinateBounds();
      for (StopBean stop : _stopsById.values())
        b.addPoint(stop.getLat(), stop.getLon());
      b = SphericalGeometryLibrary.bounds(b, 100);
      context = stopFinder.getCoordinateBoundsAsContext(b);
      System.out.println("context=" + context);
    }

    final Context c = context;

    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        stopFinder.onContextChanged(c);
      }
    });
  }

  /****
   *
   ****/

  protected void initialize() {
    _addStopAnchor.setHref("#addStop");    
  }

  protected void setStopsAndRoutes(Collection<String> stopIds,
      Collection<String> routeIds) {
    
    if (routeIds.isEmpty()) {
      _routesSelectedByDefault = true;
    } else {
      _routesSelectedByDefault = false;
      for (String routeId : routeIds)
        _routeSelectionById.put(routeId, Boolean.TRUE);
    }
    
    StopHandler stopHandler = new StopHandler();
    for (String stopId : stopIds) {
      _service.getStop(stopId, stopHandler);
    }
  }

  protected void refresh() {

    Map<String, RouteBean> routesById = new HashMap<String, RouteBean>();
    for (StopBean stop : _stopsById.values()) {
      for (RouteBean route : stop.getRoutes())
        routesById.put(route.getId(), route);
    }

    List<StopBean> stops = new ArrayList<StopBean>(_stopsById.values());

    List<RouteBean> routes = new ArrayList<RouteBean>(routesById.values());
    Collections.sort(routes, new RouteNameComparator());

    _stopsPanel.clear();
    for (StopBean stop : stops)
      _stopsPanel.add(new StopWidget(stop, _stopRemovedHandler));

    _routesPanel.clear();
    for (RouteBean route : routes) {
      Boolean selected = _routeSelectionById.get(route.getId());
      if (selected == null) {
        selected = _routesSelectedByDefault;
        _routeSelectionById.put(route.getId(), selected);
      }
      _routesPanel.add(new RouteWidget(route, selected, _routeHandler));
    }
  }

  public class StopFinderWidgetExtension extends StopFinderWidget {

    public StopFinderWidgetExtension() {
      super();
      setTitleWidget(new StopSelectionTitleWidget(_dialog));
      hideLinksPanel();
    }

    @Override
    protected Widget getStopInfoWindowWidget(StopBean stop,
        StopFinderCssResource css) {
      return new StopInfoWindowWidgetExtension(_stopFinder, _transitMapManager,
          stop, css);
    }
  }

  private class StopInfoWindowWidgetExtension extends StopInfoWindowWidget {

    public StopInfoWindowWidgetExtension(StopFinderInterface stopFinder,
        TransitMapManager transitMapManager, StopBean stop,
        StopFinderCssResource css) {
      super(stopFinder, transitMapManager, stop, css);
    }

    @Override
    protected void handleLinksForStopInfoWindow(final StopBean bean) {
      Anchor anchor = new Anchor("Add this stop to the list");
      anchor.addClickHandler(new ClickHandler() {

        public void onClick(ClickEvent event) {
          _stop = bean;
          _dialog.hide();
        }
      });
      getStopLinksPanel().clear();
      getStopLinksPanel().add(anchor);
    }
  }

  private class DefaultOperationHandler implements OperationHandler {
    @Override
    public void handleOperation(OperationContext context) {
      Panel panel = context.getPanel();
      HTMLPanel html = new HTMLPanel(
          "<p>Search for stops like you normally would.</p><p>Click and select a stop to add it to your custom stop view.</p>");
      panel.add(html);
      context.getTransitMapManager().showStopsInCurrentView();
    }
  }
  
  public interface MyStyle extends CssResource {
    String StopFinderDialog();
  }

  private class StopRemovedHandler implements StopWidget.RemoveClickedHandler {

    @Override
    public void handleRemoveClicked(StopWidget widget, StopBean stop) {
      removeStop(stop);
    }
  }

  private class RouteHandler implements RouteWidget.RouteSelectionHandler {

    @Override
    public void handleSelectionChanged(RouteWidget widget, RouteBean route,
        boolean selected) {
      _routeSelectionById.put(route.getId(), selected);
    }
  }

  private class StopHandler implements AsyncCallback<StopBean> {

    @Override
    public void onSuccess(StopBean arg) {
      addStop(arg);
    }

    @Override
    public void onFailure(Throwable arg0) {

    }

  }

  private static class RouteNameComparator implements Comparator<RouteBean> {
    @Override
    public int compare(RouteBean o1, RouteBean o2) {
      String n1 = RoutePresenter.getNameForRoute(o1);
      String n2 = RoutePresenter.getNameForRoute(o2);
      return n1.compareTo(n2);
    }
  }

}
