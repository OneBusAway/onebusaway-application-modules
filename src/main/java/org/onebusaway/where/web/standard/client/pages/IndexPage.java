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
package org.onebusaway.where.web.standard.client.pages;

import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import org.onebusaway.where.web.common.client.AbstractPageSource;
import org.onebusaway.where.web.common.client.Context;
import org.onebusaway.where.web.common.client.PageException;
import org.onebusaway.where.web.common.client.model.RouteBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.common.client.widgets.SpanPanel;
import org.onebusaway.where.web.common.client.widgets.SpanWidget;
import org.onebusaway.where.web.standard.client.pages.constraints.AddressQueryConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.AreaConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.LocationConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.NoConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.RouteConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.StopConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.StopIdentificationConstraint;
import org.onebusaway.where.web.standard.client.pages.constraints.StopSelectionConstraint;
import org.onebusaway.where.web.standard.client.resources.OneBusAwayStandardResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexPage extends AbstractPageSource implements
    IndexPageConstants, SearchWrapper {

  public static final int TOP_PANEL_HEIGHT = 90;

  public static final int RESULT_PANEL_WIDTH = 300;

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private StopSelectionConstraint _constraint;

  private StopsHandler _stopsHandler = new StopsHandler();

  private MyMarkerManager _manager;

  private FlowPanel _resultsPanel;

  private MapWidget _map;

  private int _unique = 0;

  private FlowPanel _message;

  private String _selectedStop = null;

  private List<TextBox> _searchBoxes = new ArrayList<TextBox>();

  private Map<ESearchType, Integer> _searchTabIndex = new HashMap<ESearchType, Integer>();

  private TabPanel _searchTabs;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public Widget create(final Context context) throws PageException {

    StopSelectionConstraint constraint = createStopConstraint(context);
    setStopSelection(context);

    FlowPanel panel = new FlowPanel();

    HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.setHeight(TOP_PANEL_HEIGHT + "px");
    topPanel.addStyleName("topPanel");
    panel.add(topPanel);

    HorizontalPanel titlePanel = new HorizontalPanel();
    titlePanel.addStyleName("titlePanel");
    titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    titlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    topPanel.add(titlePanel);
    topPanel.setCellVerticalAlignment(titlePanel,
        HasVerticalAlignment.ALIGN_MIDDLE);
    topPanel.setCellHorizontalAlignment(titlePanel,
        HasHorizontalAlignment.ALIGN_CENTER);
    topPanel.setCellWidth(titlePanel, RESULT_PANEL_WIDTH + "px");

    Image g = new Image(
        OneBusAwayStandardResources.INSTANCE.getImageBus().getUrl());
    g.addStyleName("busLogo");
    titlePanel.add(g);

    titlePanel.add(new DivWidget("title",
        _msgs.standardIndexPageWhereIsYourBus()));

    FlowPanel searchPanel = new FlowPanel();
    searchPanel.addStyleName("searchPanel");
    topPanel.add(searchPanel);

    searchPanel.add(new DivWidget("searchForStopsLabel",
        _msgs.standardIndexPageSearchForStops()));

    _searchTabs = new TabPanel();
    _searchTabs.addStyleName("searchForStopsTabPanel");

    _searchTabIndex = new HashMap<ESearchType, Integer>();
    _searchBoxes.clear();

    _searchTabs.add(getSearchByAddressTab(), "By Address");
    _searchTabs.add(getSearchByRouteTab(), "By Route");
    _searchTabs.add(getSearchByNumberTab(), "By Number");

    _searchTabs.selectTab(0);
    _searchTabs.addTabListener(new TabListener() {

      public boolean onBeforeTabSelected(SourcesTabEvents arg0, int arg1) {
        return true;
      }

      public void onTabSelected(SourcesTabEvents event, int index) {
        if (0 <= index && index < _searchBoxes.size()) {
          final TextBox box = _searchBoxes.get(index);
          DeferredCommand.addCommand(new Command() {
            public void execute() {
              box.setFocus(true);
            }
          });
        }
      }
    });

    searchPanel.add(_searchTabs);

    FlowPanel middlePanel = new FlowPanel();
    middlePanel.addStyleName("middlePanel");
    panel.add(middlePanel);

    middlePanel.add(getRedoSearchPanel());

    HorizontalPanel hp = new HorizontalPanel();
    panel.add(hp);

    FlowPanel leftPanel = new FlowPanel();
    hp.add(leftPanel);
    hp.setCellWidth(leftPanel, RESULT_PANEL_WIDTH + "px");

    _resultsPanel = new FlowPanel();
    _resultsPanel.addStyleName("resultsPanel");
    leftPanel.add(_resultsPanel);

    _message = new FlowPanel();
    _message.addStyleName("tooManyStopsMessage");
    leftPanel.add(_message);

    _map = new MapWidget(_center, _zoom);
    _map.setSize("500px", "500px");
    _map.addStyleName("map");
    _map.addControl(new LargeMapControl());

    hp.add(_map);
    hp.setCellHeight(_map, "100%");

    _manager = new MyMarkerManager(_map);

    updateConstraint(context, constraint);

    Window.setTitle("One Bus Away - Search For Stops");

    DeferredCommand.addCommand(new Command() {
      public void execute() {
        _map.setHeight((Window.getClientHeight() - _map.getAbsoluteTop() - 20)
            + "px");
        _map.setWidth((Window.getClientWidth() - _map.getAbsoluteLeft() - 15)
            + "px");
        Window.addWindowResizeListener(new ResizeHandler());
      }
    });

    // return view;
    return panel;
  }

  @Override
  public Widget update(Context context) throws PageException {

    // Reset the visible state
    _map.clearOverlays();

    StopSelectionConstraint constraint = createStopConstraint(context);
    setStopSelection(context);

    updateConstraint(context, constraint);

    return null;
  }

  private void updateConstraint(Context context,
      StopSelectionConstraint constraint) {
    constraint.setMap(_map);
    constraint.setResultsPanel(_resultsPanel);
    constraint.setStopsHandler(_stopsHandler);
    constraint.setSearchWrapper(this);

    _resultsPanel.clear();
    _message.clear();
    constraint.update(context);
  }

  public void setSearchText(ESearchType type, String value) {

    Integer indexValue = _searchTabIndex.get(type);

    if (indexValue != null) {
      int index = indexValue.intValue();
      _searchTabs.selectTab(index);
      if (0 <= index && index < _searchBoxes.size()) {
        final TextBox box = _searchBoxes.get(index);
        box.setText(value);
        DeferredCommand.addCommand(new Command() {
          public void execute() {
            box.setFocus(true);
          }
        });
      }
    }

  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private Widget getSearchByAddressTab() {
    return getSearchTab(ESearchType.ADDRESS,
        _msgs.standardIndexPageSearchByAddressExample());
  }

  private Widget getSearchByRouteTab() {
    return getSearchTab(ESearchType.ROUTE,
        _msgs.standardIndexPageSearchByRouteExample());
  }

  private Widget getSearchByNumberTab() {
    return getSearchTab(ESearchType.NUMBER,
        _msgs.standardIndexPageSearchByNumberExample());
  }

  private Widget getSearchTab(final ESearchType searchType, String exampleString) {

    FlowPanel panel = new FlowPanel();
    final TextBox box = new TextBox();
    box.setVisibleLength(55);
    box.addKeyboardListener(new KeyboardListenerAdapter() {
      public void onKeyPress(Widget widget, char keyCode, int modifiers) {
        if (keyCode == KeyboardListener.KEY_ENTER && modifiers == 0)
          handleSearchEvent(searchType, box.getText());
      }
    });

    _searchBoxes.add(box);
    int index = _searchTabs.getWidgetCount();
    _searchTabIndex.put(searchType, new Integer(index));

    Button b = new Button(_msgs.standardIndexPageSearch());
    b.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        handleSearchEvent(searchType, box.getText());
      }
    });

    panel.add(box);
    panel.add(b);
    DivWidget example = new DivWidget(exampleString);
    example.addStyleName("searchExample");
    panel.add(example);

    return panel;
  }

  private Panel getRedoSearchPanel() {

    SpanPanel redo = new SpanPanel();
    redo.addStyleName("redoSearchForStopsInArea");

    Image img = new Image(
        OneBusAwayStandardResources.INSTANCE.getImageSouth().getUrl());
    redo.add(img);

    SpanWidget redoSearchForStopsInArea = new SpanWidget(
        _msgs.standardIndexPageSearchForStopsInThisArea());

    redo.add(redoSearchForStopsInArea);

    redoSearchForStopsInArea.addClickListener(new ClickListener() {
      public void onClick(Widget widget) {
        searchForStopsInVisibleArea();
      }

    });
    return redo;
  }

  private StopSelectionConstraint createStopConstraint(Context context)
      throws PageException {
    StopSelectionConstraint constraint = createInternalStopConstraint(context);
    if (constraint.equals(_constraint)) {
      System.out.println("reusing previous constraint");
      constraint = _constraint;
    }
    _constraint = constraint;
    return constraint;
  }

  private StopSelectionConstraint createInternalStopConstraint(Context context)
      throws PageException {
    String mode = context.getParam(KEY_MODE);

    if (MODE_ADDRESS.equals(mode)) {
      String query = context.getParam(KEY_QUERY);
      if (query == null || query.length() == 0)
        return new NoConstraint();
      return new AddressQueryConstraint(query);
    }
    if (MODE_LOCATION.equals(mode) || MODE_STOP.equals(mode)) {

      try {
        double lat = Double.parseDouble(context.getParam("lat"));
        double lon = Double.parseDouble(context.getParam("lon"));
        int accuracy = Integer.parseInt(context.getParam("accuracy"));
        if (MODE_LOCATION.equals(mode))
          return new LocationConstraint(LatLng.newInstance(lat, lon), accuracy);
        else
          return new StopConstraint(LatLng.newInstance(lat, lon), accuracy);

      } catch (NumberFormatException ex) {
        throw new PageException(
            _msgs.standardIndexPageInvalidLocationSpecified());
      }

    } else if (MODE_AREA.equals(mode)) {

      try {
        double lat1 = Double.parseDouble(context.getParam("lat1"));
        double lon1 = Double.parseDouble(context.getParam("lon1"));
        double lat2 = Double.parseDouble(context.getParam("lat2"));
        double lon2 = Double.parseDouble(context.getParam("lon2"));

        LatLngBounds bounds = LatLngBounds.newInstance();
        bounds.extend(LatLng.newInstance(lat1, lon1));
        bounds.extend(LatLng.newInstance(lat2, lon2));

        return new AreaConstraint(bounds);

      } catch (NumberFormatException ex) {
        throw new PageException(
            _msgs.standardIndexPageInvalidLocationSpecified());
      }
    } else if (MODE_ROUTE.equals(mode)) {
      String route = context.getParam(KEY_ROUTE);
      return new RouteConstraint(route);
    } else if (MODE_STOP_IDENTIFICAION.equals(mode)) {
      return new StopIdentificationConstraint();
    }

    return new NoConstraint();
  }

  private void setStopSelection(Context context) {

    _selectedStop = null;

    if (context.hasParam(STOP_KEY)) {
      _selectedStop = context.getParam(STOP_KEY);
    }
  }

  private void handleSearchEvent(ESearchType searchType, String value) {

    if (value == null || value.length() == 0)
      return;

    switch (searchType) {
      case ADDRESS:
        newTarget("index", KEY_MODE, MODE_ADDRESS, KEY_QUERY, value,
            KEY_UNIQUE, _unique++);
        break;
      case ROUTE:
        newTarget("index", KEY_MODE, MODE_ROUTE, KEY_ROUTE, value, KEY_UNIQUE,
            _unique++);
        break;
      case NUMBER:
        // newTarget("stop", "id", value);
        Location.assign("stop.action?id=" + value);
        break;
    }
  }

  private void searchForStopsInVisibleArea() {
    LatLngBounds bounds = _map.getBounds();
    LatLng p1 = bounds.getNorthEast();
    LatLng p2 = bounds.getSouthWest();
    newTarget("index", KEY_MODE, MODE_AREA, "lat1", p1.getLatitude(), "lon1",
        p1.getLongitude(), "lat2", p2.getLatitude(), "lon2", p2.getLongitude(),
        KEY_UNIQUE, _unique++);
  }

  /*****************************************************************************
   * Internal Classes
   ****************************************************************************/

  private class StopsHandler implements AsyncCallback<StopsBean> {

    public void onSuccess(StopsBean bean) {

      _manager.clear();

      LatLngBounds bounds = LatLngBounds.newInstance();

      List<StopBean> stops = bean.getStops();

      _message.clear();
      if (stops.size() == 75) {
        _message.add(new SpanWidget(_msgs.standardIndexPageTooManyResultsPre()));
        SpanWidget link = new SpanWidget(
            _msgs.standardIndexPageTooManyResultsLink());
        link.addStyleName("tooManyStopsMessageLink");
        link.addClickListener(new ClickListener() {
          public void onClick(Widget arg0) {
            searchForStopsInVisibleArea();
          }
        });
        _message.add(link);
        _message.add(new SpanWidget(_msgs.standardIndexPageTooManyResultsPost()));
      }

      final Map<String, Integer> counts = new HashMap<String, Integer>();

      for (StopBean stop : stops) {
        String id = stop.getId();
        Integer c = counts.get(id);
        if (c == null) {
          c = 0;
        }
        c++;
        counts.put(id, c);
      }

      for (final StopBean stop : stops) {
        final LatLng p = LatLng.newInstance(stop.getLat(), stop.getLon());
        bounds.extend(p);

        MarkerClickHandler clickHandler = new MarkerClickHandler() {
          public void onClick(MarkerClickEvent event) {
            StopHandler handler = new StopHandler(p);
            _service.getStop(stop.getId(), handler);
            Integer c = counts.get(stop.getId());
            if (c != null)
              System.out.println("count=" + c);
          }
        };

        Marker closeMarker = getCloseMarker(stop, p);
        Marker middleMarker = getMiddleMarker(stop, p);
        Marker farMarker = getFarMarker(stop, p);

        closeMarker.addMarkerClickHandler(clickHandler);
        middleMarker.addMarkerClickHandler(clickHandler);
        farMarker.addMarkerClickHandler(clickHandler);

        addOverlayAtZoom(farMarker, 10, 13);
        addOverlayAtZoom(middleMarker, 13, 15);
        addOverlayAtZoom(closeMarker, 15, 20);
      }

      int zoomLevel = _map.getBoundsZoomLevel(bounds);
      _map.setCenter(bounds.getCenter(), zoomLevel);
    }

    public void onFailure(Throwable ex) {
      _resultsPanel.add(new DivWidget("failure"));
      handleException(ex);
    }

    private Marker getCloseMarker(final StopBean stop, final LatLng p) {
      MarkerOptions opts = MarkerOptions.newInstance();
      boolean isSelected = stop.getId().equals(_selectedStop);
      Icon icon = StopIconFactory.getIconForDirection(stop.getDirection(),
          isSelected);
      opts.setIcon(icon);
      return new Marker(p, opts);
    }

    private Marker getMiddleMarker(final StopBean stop, final LatLng p) {
      MarkerOptions opts = MarkerOptions.newInstance();
      Icon icon = StopIconFactory.getMiddleStopIcon();
      opts.setIcon(icon);
      return new Marker(p, opts);
    }

    private Marker getFarMarker(final StopBean stop, final LatLng p) {
      MarkerOptions opts = MarkerOptions.newInstance();
      Icon icon = StopIconFactory.getFarStopIcon();
      opts.setIcon(icon);
      return new Marker(p, opts);
    }

    private void addOverlayAtZoom(Overlay overlay, int from, int to) {
      _manager.addOverlay(overlay, from, to);
    }

  }

  private class StopHandler implements AsyncCallback<StopBean> {

    private LatLng _point;

    public StopHandler(LatLng point) {
      _point = point;
    }

    public void onSuccess(StopBean bean) {

      FlowPanel panel = new FlowPanel();
      panel.addStyleName("stopPanel");

      String name = bean.getName();
      DivWidget stopAddress = new DivWidget(name);
      stopAddress.addStyleName("stopAddress");
      panel.add(stopAddress);

      String stopDescription = _msgs.stopByNumberPageStopNumberShebang() + " "
          + bean.getId() + " - " + bean.getDirection() + " "
          + _msgs.stopByNumberPageBound();

      DivWidget stopNumber = new DivWidget(stopDescription);
      stopNumber.addStyleName("stopNumber");
      panel.add(stopNumber);

      // String target = getTarget("stop", "id", stop.getId());
      String html = "<a href=\"stop.action?id=" + bean.getId() + "\">"
          + _msgs.standardIndexPageGetRealtimeArrivalInfo() + "</a>";
      DivWidget link = new DivWidget(html);
      link.addStyleName("stopRealTimeLink");
      panel.add(link);

      FlowPanel routesPanel = new FlowPanel();
      routesPanel.addStyleName("stopRoutesPanel");

      DivWidget desc = new DivWidget(_msgs.standardIndexPageRoutes());
      desc.addStyleName("stopRoutes");
      routesPanel.add(desc);

      for (final RouteBean route : bean.getRoutes()) {
        SpanWidget w = new SpanWidget(route.getNumber());
        w.addStyleName("stopRouteEntry");
        w.addClickListener(new ClickListener() {
          public void onClick(Widget arg0) {
            newTarget(INDEX_PAGE, KEY_MODE, MODE_ROUTE, KEY_ROUTE,
                route.getNumber());
          }
        });
        routesPanel.add(w);
      }

      panel.add(routesPanel);

      InfoWindow window = _map.getInfoWindow();
      window.open(_point, new InfoWindowContent(panel));
    }

    public void onFailure(Throwable ex) {
      handleException(ex);
    }
  }

  private class ResizeHandler implements WindowResizeListener {
    public void onWindowResized(int width, int height) {
      _map.setHeight((height - _map.getAbsoluteTop() - 20) + "px");
      _map.setWidth((width - _map.getAbsoluteLeft() - 15) + "px");
    }
  }
}
