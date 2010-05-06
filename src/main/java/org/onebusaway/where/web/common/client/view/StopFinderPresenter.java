package org.onebusaway.where.web.common.client.view;

import org.onebusaway.common.web.common.client.ExceptionSupport;
import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.context.ContextHelper;
import org.onebusaway.common.web.common.client.context.ContextManager;
import org.onebusaway.common.web.common.client.context.DirectContextManager;
import org.onebusaway.common.web.common.client.layout.BoxLayoutManager;
import org.onebusaway.common.web.common.client.layout.EBoxLayoutDirection;
import org.onebusaway.common.web.common.client.model.RouteBean;
import org.onebusaway.common.web.common.client.model.StopBean;
import org.onebusaway.common.web.common.client.resources.CommonResources;
import org.onebusaway.common.web.common.client.resources.StopIconFactory;
import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.common.web.common.client.widgets.SpanPanel;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.where.web.common.client.WhereLibrary;
import org.onebusaway.where.web.common.client.WhereMessages;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.resources.StopFinderResources;
import org.onebusaway.where.web.common.client.rpc.WhereServiceAsync;
import org.onebusaway.where.web.common.client.view.constraints.AddressQueryConstraint;
import org.onebusaway.where.web.common.client.view.constraints.AreaConstraint;
import org.onebusaway.where.web.common.client.view.constraints.LocationConstraint;
import org.onebusaway.where.web.common.client.view.constraints.NoConstraint;
import org.onebusaway.where.web.common.client.view.constraints.RouteConstraint;
import org.onebusaway.where.web.common.client.view.constraints.StopConstraint;
import org.onebusaway.where.web.common.client.view.constraints.StopIdentificationConstraint;
import org.onebusaway.where.web.common.client.view.constraints.StopSelectionConstraint;
import org.onebusaway.where.web.common.client.view.constraints.StopSequenceConstraint;

import com.google.gwt.libideas.client.StyleInjector;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopFinderPresenter implements StopFinderInterface, StopFinderConstants {

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  protected static WhereMessages _msgs = WhereLibrary.MESSAGES;

  protected static WhereServiceAsync _service = WhereServiceAsync.SERVICE;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private boolean _initialized = false;

  private String _stylePrefix = "StopFinder";

  private FlowPanel _panel = new FlowPanel();

  private StopSelectionConstraint _constraint;

  private StopsHandler _stopsHandler = new StopsHandler();

  private MapOverlayManager _manager;

  private FlowPanel _resultsPanel;

  private MapWidget _map;

  private int _unique = 0;

  // private FlowPanel _message;

  private String _selectedStop = null;

  private List<TextBox> _searchBoxes = new ArrayList<TextBox>();

  private Map<EWhereStopFinderSearchType, Integer> _searchTabIndex = new HashMap<EWhereStopFinderSearchType, Integer>();

  private TabPanel _searchTabs;

  private ContextHelper _contextHelper = new ContextHelper();

  private BoxLayoutManager _panelLayout = new BoxLayoutManager();

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public StopFinderPresenter() {
    this(new DirectContextManager());
  }

  public StopFinderPresenter(ContextManager contextManager) {
    setContextManager(contextManager);
  }

  public void setContextManager(ContextManager contextManager) {
    _contextHelper = new ContextHelper(contextManager);
  }

  public void initialize() {
    if (!_initialized) {
      _panel.addStyleName(_stylePrefix);

      initializeTopPanel();
      initializeSplitPanel();

      StyleInjector.injectStylesheet(StopFinderResources.INSTANCE.getCSS().getText());

      _initialized = true;
    }
  }

  public Widget getWidget() {
    initialize();
    return _panel;
  }

  public void onContextChanged(Context context) {

    initialize();

    // Reset the visible state
    _map.clearOverlays();
    _resultsPanel.clear();

    StopSelectionConstraint constraint = createStopConstraint(context);
    setStopSelection(context);

    constraint.setMap(_map);
    constraint.setResultsPanel(_resultsPanel);
    constraint.setStopFinderInterface(this);

    constraint.update(context);
  }

  public AsyncCallback<StopsBean> getStopsHandler() {
    return _stopsHandler;
  }

  public void setSearchText(EWhereStopFinderSearchType type, String value) {

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

  public void queryAddress(String address) {
    _contextHelper.setContext(KEY_MODE, MODE_ADDRESS, KEY_QUERY, address, KEY_UNIQUE, _unique++);
  }

  public void queryLocation(LatLng location, int accuracy) {
    _contextHelper.setContext(KEY_MODE, MODE_LOCATION, "lat", location.getLatitude(), "lon", location.getLongitude(),
        "accuracy", accuracy, KEY_UNIQUE, _unique++);
  }

  public void queryArea(LatLngBounds bounds) {
    LatLng p1 = bounds.getNorthEast();
    LatLng p2 = bounds.getSouthWest();
    _contextHelper.setContext(KEY_MODE, MODE_AREA, "lat1", p1.getLatitude(), "lon1", p1.getLongitude(), "lat2",
        p2.getLatitude(), "lon2", p2.getLongitude(), KEY_UNIQUE, _unique++);
  }

  public void queryRoute(String route) {
    _contextHelper.setContext(KEY_MODE, MODE_ROUTE, KEY_ROUTE, route, KEY_UNIQUE, _unique++);
  }

  public void queryRoute(String route, String blockId) {
    _contextHelper.setContext(KEY_MODE, MODE_ROUTE, KEY_ROUTE, route, KEY_BLOCK_ID, blockId, KEY_UNIQUE, _unique++);
  }

  public void queryStopSequence(String route, int sequenceId) {
    _contextHelper.setContext(KEY_MODE, MODE_STOP_SEQUENCE, KEY_ROUTE, route, KEY_STOP_SEQUENCE_ID, sequenceId,
        KEY_UNIQUE, _unique++);
  }

  public void showStopIdentificationInfo() {
    _contextHelper.setContext(KEY_MODE, MODE_STOP_IDENTIFICAION, KEY_UNIQUE, _unique++);
  }

  public void queryStop(String stopId) {
    Location.replace("stop.action?id=" + stopId);
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  protected String getStyleName(String name) {
    return _stylePrefix + "-" + name;
  }

  private void initializeTopPanel() {

    Grid topPanel = new Grid(1, 2);
    topPanel.addStyleName(getStyleName("TopPanel"));
    _panel.add(topPanel);

    Widget titleWidget = createTitleWidget();

    topPanel.setWidget(0, 0, titleWidget);
    topPanel.getCellFormatter().addStyleName(0, 0, getStyleName("TopPanel-TitlePanel"));

    FlowPanel searchPanel = new FlowPanel();
    searchPanel.addStyleName(getStyleName("SearchPanel"));
    topPanel.setWidget(0, 1, searchPanel);
    topPanel.getCellFormatter().addStyleName(0, 1, getStyleName("TopPanel-SearchPanel"));

    searchPanel.add(new DivWidget(_msgs.standardIndexPageSearchForStops(), getStyleName("SearchForStopsLabel")));

    _searchTabs = new TabPanel();
    _searchTabs.addStyleName(getStyleName("SearchForStopsTabPanel"));

    _searchTabIndex = new HashMap<EWhereStopFinderSearchType, Integer>();
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
    middlePanel.addStyleName(getStyleName("MiddlePanel"));
    _panel.add(middlePanel);

    middlePanel.add(getRedoSearchPanel());
  }

  protected Widget createTitleWidget() {
    return new FlowPanel();
  }

  private void initializeSplitPanel() {

    if (true) {

      DivPanel panel = new DivPanel();
      panel.addStyleName(getStyleName("SplitPanel"));
      _panel.add(panel);

      _map = new MapWidget(_center, _zoom);
      _map.addStyleName(getStyleName("Map"));
      _map.addControl(new LargeMapControl());
      _map.addControl(new MapTypeControl());
      _map.addControl(new ScaleControl());
      panel.add(_map);

      _resultsPanel = new FlowPanel();
      _resultsPanel.addStyleName(getStyleName("ResultsPanel"));
      panel.add(_resultsPanel);

      _panelLayout.addFillRemaining(EBoxLayoutDirection.VERTICAL, _panel, panel, _resultsPanel, _map);
      _panelLayout.addMapWidget(_map);

    } else {
      HorizontalPanel hp = new HorizontalPanel();
      hp.addStyleName(getStyleName("SplitPanel"));
      _panel.add(hp);

      _resultsPanel = new FlowPanel();
      _resultsPanel.addStyleName(getStyleName("ResultsPanel"));
      hp.add(_resultsPanel);

      _map = new MapWidget(_center, _zoom);
      _map.addStyleName(getStyleName("Map"));
      _map.addControl(new LargeMapControl());
      _map.addControl(new MapTypeControl());
      _map.addControl(new ScaleControl());
      hp.add(_map);

      _panelLayout.addFillRemaining(EBoxLayoutDirection.VERTICAL, _panel, hp, _resultsPanel, _map);
      _panelLayout.addMapWidget(_map);
    }

    _manager = new MapOverlayManager(_map);
  }

  protected void handleStopBean(StopBean bean) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(getStyleName("StopPanel"));

    handleTitleForStopInfoWindow(bean, panel);
    handleLinksForStopInfoWindow(bean, panel);
    handleRoutesForStopInfoWindow(bean, panel);

    InfoWindow window = _map.getInfoWindow();
    LatLng point = LatLng.newInstance(bean.getLat(), bean.getLon());
    window.open(point, new InfoWindowContent(panel));
  }

  protected void handleTitleForStopInfoWindow(StopBean bean, FlowPanel panel) {
    String name = bean.getName();
    DivWidget stopAddress = new DivWidget(name);
    stopAddress.addStyleName(getStyleName("StopAddress"));
    panel.add(stopAddress);

    String stopDescription = _msgs.stopByNumberPageStopNumberShebang() + " " + bean.getId() + " - "
        + bean.getDirection() + " " + _msgs.stopByNumberPageBound();

    DivWidget stopNumber = new DivWidget(stopDescription);
    stopNumber.addStyleName(getStyleName("StopNumber"));
    panel.add(stopNumber);
  }

  protected void handleLinksForStopInfoWindow(StopBean bean, FlowPanel panel) {
    String html = "<a href=\"stop.action?id=" + bean.getId() + "\">Real-time arrival info</a>";
    DivWidget link = new DivWidget(html);
    link.addStyleName(getStyleName("StopRealTimeLink"));
    panel.add(link);

    panel.add(new DivWidget(""));

    String html2 = "<a href=\"schedule.action?id=" + bean.getId() + "\">Complete timetable</a>";
    DivWidget link2 = new DivWidget(html2);
    link2.addStyleName(getStyleName("StopRealTimeLink"));
    panel.add(link2);
  }

  protected void handleRoutesForStopInfoWindow(StopBean bean, FlowPanel panel) {
    FlowPanel routesPanel = new FlowPanel();
    routesPanel.addStyleName(getStyleName("StopRoutesPanel"));

    DivWidget desc = new DivWidget(_msgs.standardIndexPageRoutes());
    desc.addStyleName(getStyleName("StopRoutes"));
    routesPanel.add(desc);

    for (final RouteBean route : bean.getRoutes()) {
      SpanWidget w = new SpanWidget(route.getNumber());
      w.addStyleName(getStyleName("StopRouteEntry"));
      w.addClickListener(new ClickListener() {
        public void onClick(Widget arg0) {
          queryRoute(route.getNumber());

        }
      });
      routesPanel.add(w);
    }

    panel.add(routesPanel);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private Widget getSearchByAddressTab() {
    return getSearchTab(EWhereStopFinderSearchType.ADDRESS, _msgs.standardIndexPageSearchByAddressExample());
  }

  private Widget getSearchByRouteTab() {
    return getSearchTab(EWhereStopFinderSearchType.ROUTE, _msgs.standardIndexPageSearchByRouteExample());
  }

  private Widget getSearchByNumberTab() {

    DivPanel p = new DivPanel();
    p.add(new SpanWidget(_msgs.standardIndexPageSearchByNumberExample()));

    Anchor a = new Anchor(_msgs.standardIndexPageSearchByNumberExampleLink());
    p.add(a);
    a.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        showStopIdentificationInfo();
      }
    });

    return getSearchTab(EWhereStopFinderSearchType.NUMBER, p);
  }

  private Widget getSearchTab(final EWhereStopFinderSearchType searchType, String exampleString) {
    return getSearchTab(searchType, new DivWidget(exampleString));
  }

  private Widget getSearchTab(final EWhereStopFinderSearchType searchType, Widget exampleWidget) {

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

    exampleWidget.addStyleName(getStyleName("SearchExample"));
    panel.add(exampleWidget);

    return panel;
  }

  private Panel getRedoSearchPanel() {

    SpanPanel redo = new SpanPanel();
    redo.addStyleName(getStyleName("RedoSearchForStopsInArea"));

    Image img = new Image(CommonResources.INSTANCE.getImageSouth().getUrl());
    redo.add(img);

    SpanWidget redoSearchForStopsInArea = new SpanWidget(_msgs.standardIndexPageSearchForStopsInThisArea());

    redo.add(redoSearchForStopsInArea);

    redoSearchForStopsInArea.addClickListener(new ClickListener() {
      public void onClick(Widget widget) {
        queryArea(_map.getBounds());
      }

    });
    return redo;
  }

  private StopSelectionConstraint createStopConstraint(Context context) {
    StopSelectionConstraint constraint = createInternalStopConstraint(context);
    if (!constraint.equals(_constraint))
      _constraint = constraint;
    return _constraint;
  }

  private StopSelectionConstraint createInternalStopConstraint(Context context) {

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
        throw new IllegalStateException(_msgs.standardIndexPageInvalidLocationSpecified());
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
        throw new IllegalStateException(_msgs.standardIndexPageInvalidLocationSpecified());
      }
    } else if (MODE_ROUTE.equals(mode)) {
      String route = context.getParam(KEY_ROUTE);
      return new RouteConstraint(route);
    } else if (MODE_STOP_IDENTIFICAION.equals(mode)) {
      return new StopIdentificationConstraint();
    } else if (MODE_STOP_SEQUENCE.equals(mode)) {
      String route = context.getParam(KEY_ROUTE);
      return new StopSequenceConstraint(route);
    }

    return new NoConstraint();
  }

  private void setStopSelection(Context context) {

    _selectedStop = null;

    if (context.hasParam(STOP_KEY)) {
      _selectedStop = context.getParam(STOP_KEY);
    }
  }

  private void handleSearchEvent(EWhereStopFinderSearchType searchType, String value) {

    if (value == null || value.length() == 0)
      return;

    switch (searchType) {
      case ADDRESS:
        queryAddress(value);
        break;
      case ROUTE:
        queryRoute(value);
        break;
      case NUMBER:
        queryStop(value);
        break;
    }
  }

  /*****************************************************************************
   * Internal Classes
   ****************************************************************************/

  private class StopsHandler implements AsyncCallback<StopsBean> {

    public void onSuccess(StopsBean bean) {

      _manager.clear();

      LatLngBounds bounds = LatLngBounds.newInstance();

      List<StopBean> stops = bean.getStops();

      if (stops.size() == 75) {
        _resultsPanel.add(new SpanWidget(_msgs.standardIndexPageTooManyResultsPre()));
        SpanWidget link = new SpanWidget(_msgs.standardIndexPageTooManyResultsLink());
        link.addStyleName(getStyleName("TooManyStopsMessageLink"));
        link.addClickListener(new ClickListener() {
          public void onClick(Widget arg0) {
            queryArea(_map.getBounds());
          }
        });
        _resultsPanel.add(link);
        _resultsPanel.add(new SpanWidget(_msgs.standardIndexPageTooManyResultsPost()));
      }

      for (final StopBean stop : stops) {
        LatLng p = LatLng.newInstance(stop.getLat(), stop.getLon());
        bounds.extend(p);

        MarkerClickHandler clickHandler = new MarkerClickHandler() {
          public void onClick(MarkerClickEvent event) {
            StopHandler handler = new StopHandler();
            _service.getStop(stop.getId(), handler);
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
      ExceptionSupport.handleException(ex);
    }

    private Marker getCloseMarker(final StopBean stop, final LatLng p) {
      MarkerOptions opts = MarkerOptions.newInstance();
      boolean isSelected = stop.getId().equals(_selectedStop);
      Icon icon = StopIconFactory.getIconForDirection(stop, isSelected);
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

    public void onSuccess(StopBean bean) {
      handleStopBean(bean);
    }

    public void onFailure(Throwable ex) {
      ExceptionSupport.handleException(ex);
    }
  }
}
