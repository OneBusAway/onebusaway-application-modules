package org.onebusaway.webapp.gwt.where_library.view;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.ExceptionSupport;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextHelper;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.context.DirectContextManager;
import org.onebusaway.webapp.gwt.common.layout.Box;
import org.onebusaway.webapp.gwt.common.layout.BoxLayoutManager;
import org.onebusaway.webapp.gwt.common.resources.map.StopIconFactory;
import org.onebusaway.webapp.gwt.common.resources.map.StopIconFactory.ESize;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.where_library.WhereLibrary;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.resources.StopFinderCssResource;
import org.onebusaway.webapp.gwt.where_library.resources.StopFinderResources;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;
import org.onebusaway.webapp.gwt.where_library.services.StopsForRegionService;
import org.onebusaway.webapp.gwt.where_library.view.constraints.AddressQueryConstraint;
import org.onebusaway.webapp.gwt.where_library.view.constraints.LocationConstraint;
import org.onebusaway.webapp.gwt.where_library.view.constraints.NoConstraint;
import org.onebusaway.webapp.gwt.where_library.view.constraints.RouteConstraint;
import org.onebusaway.webapp.gwt.where_library.view.constraints.RoutesConstraint;
import org.onebusaway.webapp.gwt.where_library.view.constraints.StopIdentificationConstraint;
import org.onebusaway.webapp.gwt.where_library.view.constraints.StopSelectionConstraint;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.event.MapMoveEndHandler;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class StopFinderPresenter implements StopFinderInterface,
    StopFinderConstants {

  private static StopFinderCssResource _css = StopFinderResources.INSTANCE.getCSS();

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  protected static WhereMessages _msgs = WhereLibrary.MESSAGES;

  protected static WebappServiceAsync _service = WebappServiceAsync.SERVICE;

  private StopsForRegionService _stopsForRegionService;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private boolean _initialized = false;

  private FlowPanel _panel = new FlowPanel();

  private StopSelectionConstraint _constraint;

  private StopsHandler _stopsHandler = new StopsHandler();

  private MapOverlayManager _manager;

  private FlowPanel _resultsPanel;

  private MapWidget _map;

  private int _unique = 0;

  private String _selectedStop = null;

  private TextBox _searchBox;

  private ContextHelper _contextHelper = new ContextHelper();

  private BoxLayoutManager _panelLayout = new BoxLayoutManager();

  private Grid _topPanel;

  private Map<String, StopAndOverlays> _visibleStopsById = new HashMap<String, StopAndOverlays>();

  private boolean _showStopsInCurrentView = true;

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

  public Widget initialize(final Context context) {

    if (!_initialized) {
      _panel.addStyleName(_css.stopFinder());

      initializeTopPanel();
      initializeSplitPanel();

      StyleInjector.injectStylesheet(StopFinderResources.INSTANCE.getCSS().getText());

      _initialized = true;
    }

    DeferredCommand.addCommand(new Command() {
      public void execute() {
        onContextChanged(context);
      }
    });

    return _panel;
  }

  public void onContextChanged(Context context) {

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

  public void setSearchText(String value) {
    _searchBox.setText(value);
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        _searchBox.setFocus(true);
      }
    });
  }

  public void queryAddress(String address) {
    _contextHelper.setContext(KEY_MODE, MODE_ADDRESS, KEY_QUERY, address,
        KEY_UNIQUE, _unique++);
  }

  public void queryLocation(LatLng location, int accuracy) {
    _contextHelper.setContext(KEY_MODE, MODE_LOCATION, "lat",
        location.getLatitude(), "lon", location.getLongitude(), "accuracy",
        accuracy, KEY_UNIQUE, _unique++);
  }

  public void queryRoutes(String route) {
    _contextHelper.setContext(KEY_MODE, MODE_ROUTES, KEY_ROUTE, route,
        KEY_UNIQUE, _unique++);
  }

  public void queryRoute(String routeId) {
    _contextHelper.setContext(KEY_MODE, MODE_ROUTE, KEY_ROUTE, routeId,
        KEY_UNIQUE, _unique++);
  }

  public void showStopIdentificationInfo() {
    _contextHelper.setContext(KEY_MODE, MODE_STOP_IDENTIFICAION, KEY_UNIQUE,
        _unique++);
  }

  public void queryStop(String stopId) {
    Location.replace("stop.action?id=" + stopId);
  }

  public void setCenter(LatLng center, int zoomLevel) {
    _map.setCenter(center, zoomLevel);
    refreshStopsInCurrentView();
  }

  public void showStops(List<StopBean> stops) {
    for (StopBean stop : stops)
      handleStopBean(stop);
  }

  public void setShowStopsInCurrentView(boolean showStopsInCurrentView) {

    if (_showStopsInCurrentView == showStopsInCurrentView)
      return;

    _showStopsInCurrentView = showStopsInCurrentView;

    refreshStopsInCurrentView();
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  private void initializeTopPanel() {

    _topPanel = new Grid(1, 2);
    _topPanel.addStyleName(_css.stopFinderTopPanel());
    _panel.add(_topPanel);

    Set<String> styles = new HashSet<String>();
    styles.add(_css.stopFinderTopPanelTitlePanel());

    Widget titleWidget = createTitleWidget(styles);

    _topPanel.setWidget(0, 0, titleWidget);
    for (String style : styles)
      _topPanel.getCellFormatter().addStyleName(0, 0, style);

    FlowPanel searchPanel = new FlowPanel();
    searchPanel.addStyleName(_css.stopFinderSearchPanel());
    _topPanel.setWidget(0, 1, searchPanel);
    _topPanel.getCellFormatter().addStyleName(0, 1,
        _css.stopFinderTopPanelSearchPanel());

    searchPanel.add(new DivWidget(_msgs.standardIndexPageSearchForStops(),
        _css.stopFinderSearchForStopsLabel()));

    FormPanel form = new FormPanel();
    form.addStyleName(_css.stopFinderSearchForStopsForm());
    searchPanel.add(form);

    FlowPanel panel = new FlowPanel();
    form.setWidget(panel);

    FormSubmitHandler handler = new FormSubmitHandler();

    _searchBox = new TextBox();
    _searchBox.setVisibleLength(55);
    _searchBox.setName("q");
    _searchBox.addKeyPressHandler(handler);
    panel.add(_searchBox);

    Button submitButton = new Button(_msgs.standardIndexPageSearch());
    submitButton.addClickHandler(handler);
    panel.add(submitButton);

    // We handle form submission and cancel it out
    form.addSubmitHandler(handler);

    DivWidget exampleWidget = new DivWidget(
        "By address (ex. \"3rd and pike\") or route number (ex. \"44\" or \"71\").",
        _css.stopFinderSearchExample());
    searchPanel.add(exampleWidget);
  }

  protected Widget createTitleWidget(Set<String> styles) {
    return new FlowPanel();
  }

  private void initializeSplitPanel() {

    final DivPanel splitPanel = new DivPanel();
    splitPanel.addStyleName(_css.stopFinderSplitPanel());
    _panel.add(splitPanel);

    final DivPanel mainMapPanel = new DivPanel();
    mainMapPanel.addStyleName(_css.stopFinderMainMapPanel());
    splitPanel.add(mainMapPanel);

    final DivPanel mapWrapper = new DivPanel();
    mapWrapper.addStyleName(_css.stopFinderMapWrapper());
    mainMapPanel.add(mapWrapper);

    FlowPanel resultsWrapper = new FlowPanel();
    resultsWrapper.addStyleName(_css.stopFinderResultsWrapper());
    splitPanel.add(resultsWrapper);

    _resultsPanel = new FlowPanel();
    _resultsPanel.addStyleName(_css.stopFinderResultsPanel());
    resultsWrapper.add(_resultsPanel);

    Box lowerBound = getLowerBoundForWidget();
    Box target = Box.minus(lowerBound, Box.top(mainMapPanel));
    _panelLayout.addSetHeightConstraint(target, mapWrapper, resultsWrapper);
    _panelLayout.refresh();

    // We delay initialization of the map
    DeferredCommand.addCommand(new Command() {
      public void execute() {

        _map = new MapWidget(_center, _zoom);
        _map.addStyleName(_css.stopFinderMap());
        _map.addControl(new LargeMapControl());
        _map.addControl(new MapTypeControl());
        _map.addControl(new ScaleControl());
        _map.setScrollWheelZoomEnabled(true);

        ZoomOrMoveHandler handler = new ZoomOrMoveHandler();
        _map.addMapZoomEndHandler(handler);
        _map.addMapMoveEndHandler(handler);

        mapWrapper.add(_map);

        _manager = new MapOverlayManager(_map);

        _panelLayout.addMapWidget(_map);
        _panelLayout.refresh();
      }
    });
  }

  protected Box getLowerBoundForWidget() {
    try {
      Dictionary dictionary = Dictionary.getDictionary("StopFinderConfig");
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

    return Box.bottom(_panel.getElement());
  }

  protected void handleStopBean(StopBean bean) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(_css.stopFinderStopPanel());

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
    stopAddress.addStyleName(_css.stopFinderStopAddress());
    panel.add(stopAddress);

    String stopDescription = _msgs.stopByNumberPageStopNumberShebang() + " "
        + bean.getCode();

    if (bean.getDirection() != null)
      stopDescription += " - " + bean.getDirection() + " "
          + _msgs.stopByNumberPageBound();

    DivWidget stopNumber = new DivWidget(stopDescription);
    stopNumber.addStyleName(_css.stopFinderStopNumber());
    panel.add(stopNumber);
  }

  protected void handleLinksForStopInfoWindow(StopBean bean, FlowPanel panel) {
    String html = "<a href=\"stop.action?id=" + bean.getId()
        + "\">Real-time arrival info</a>";
    DivWidget link = new DivWidget(html);
    link.addStyleName(_css.stopFinderStopRealTimeLink());
    panel.add(link);

    panel.add(new DivWidget(""));

    String html2 = "<a href=\"schedule.action?id=" + bean.getId()
        + "\">Complete timetable</a>";
    DivWidget link2 = new DivWidget(html2);
    link2.addStyleName(_css.stopFinderStopRealTimeLink());
    panel.add(link2);
  }

  protected void handleRoutesForStopInfoWindow(StopBean bean, FlowPanel panel) {
    FlowPanel routesPanel = new FlowPanel();
    routesPanel.addStyleName(_css.stopFinderStopRoutesPanel());

    DivWidget desc = new DivWidget(_msgs.standardIndexPageRoutes());
    desc.addStyleName(_css.stopFinderStopRoutes());
    routesPanel.add(desc);

    for (final RouteBean route : bean.getRoutes()) {
      SpanWidget w = new SpanWidget(route.getShortName());
      w.addStyleName(_css.stopFinderStopRouteEntry());
      w.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent arg0) {
          queryRoutes(route.getShortName());
        }
      });
      routesPanel.add(w);
    }

    panel.add(routesPanel);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

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
    if (MODE_LOCATION.equals(mode)) {

      try {
        double lat = Double.parseDouble(context.getParam("lat"));
        double lon = Double.parseDouble(context.getParam("lon"));
        int accuracy = Integer.parseInt(context.getParam("accuracy"));
        return new LocationConstraint(LatLng.newInstance(lat, lon), accuracy);
      } catch (NumberFormatException ex) {
        throw new IllegalStateException(
            _msgs.standardIndexPageInvalidLocationSpecified());
      }

    } else if (MODE_ROUTES.equals(mode)) {
      String route = context.getParam(KEY_ROUTE);
      return new RoutesConstraint(route);
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

  private void handleSearchEvent(String value) {

    if (value == null)
      return;

    value = value.trim();

    if (value.length() == 0)
      return;

    if (value.matches("^\\d+$"))
      queryRoutes(value);
    else
      queryAddress(value);
  }

  private void refreshStopsInCurrentView() {

    if (_showStopsInCurrentView) {

      LatLngBounds r = _map.getBounds();
      LatLng ne = r.getNorthEast();
      LatLng sw = r.getSouthWest();
      CoordinateBounds bounds = new CoordinateBounds(ne.getLatitude(),
          ne.getLongitude(), sw.getLatitude(), sw.getLongitude());
      for (Iterator<Map.Entry<String, StopAndOverlays>> it = _visibleStopsById.entrySet().iterator(); it.hasNext();) {
        Entry<String, StopAndOverlays> entry = it.next();
        StopAndOverlays stopAndOverlays = entry.getValue();
        StopBean stop = stopAndOverlays.getStop();
        if (!bounds.contains(stop.getLat(), stop.getLon())) {
          it.remove();
          for (Overlay overlay : stopAndOverlays.getOverlays())
            _manager.removeOverlay(overlay);
        }
      }
      _stopsForRegionService.getStopsForRegion(bounds, _stopsHandler);

    } else {

      for (StopAndOverlays stopAndOverlays : _visibleStopsById.values()) {
        for (Overlay overlay : stopAndOverlays.getOverlays())
          _manager.removeOverlay(overlay);
      }
      _visibleStopsById.clear();
    }
  }

  /*****************************************************************************
   * Internal Classes
   ****************************************************************************/

  private class FormSubmitHandler implements ClickHandler, KeyPressHandler,
      SubmitHandler {

    public void onKeyPress(KeyPressEvent event) {
      char keyCode = event.getCharCode();
      if (keyCode == '\n' && !event.isAnyModifierKeyDown())
        handleSearchEvent(_searchBox.getText());
    }

    public void onClick(ClickEvent arg0) {
      handleSearchEvent(_searchBox.getText());
    }

    public void onSubmit(SubmitEvent event) {
      event.cancel();
    }
  }

  private class StopsHandler implements AsyncCallback<List<StopBean>> {

    public void onSuccess(List<StopBean> stops) {

      for (final StopBean stop : stops) {

        if (_visibleStopsById.containsKey(stop.getId()))
          continue;

        StopAndOverlays stopAndOverlays = new StopAndOverlays(stop);
        _visibleStopsById.put(stop.getId(), stopAndOverlays);

        LatLng p = LatLng.newInstance(stop.getLat(), stop.getLon());

        MarkerClickHandler clickHandler = new MarkerClickHandler() {
          public void onClick(MarkerClickEvent event) {
            StopHandler handler = new StopHandler();
            _service.getStop(stop.getId(), handler);
          }
        };

        Marker largerMarker = getStopMarker(stop, p, ESize.LARGE);
        Marker mediumMarker = getStopMarker(stop, p, ESize.MEDIUM);
        Marker smallMarker = getStopMarker(stop, p, ESize.SMALL);
        Marker tinyMarker = getStopMarker(stop, p, ESize.TINY);

        largerMarker.addMarkerClickHandler(clickHandler);
        mediumMarker.addMarkerClickHandler(clickHandler);
        smallMarker.addMarkerClickHandler(clickHandler);
        tinyMarker.addMarkerClickHandler(clickHandler);

        if (true) {
          addOverlayAtZoom(tinyMarker, 12, 14);
          addOverlayAtZoom(smallMarker, 14, 16);
          addOverlayAtZoom(mediumMarker, 16, 17);
          addOverlayAtZoom(largerMarker, 17, 20);

          stopAndOverlays.addOverlays(tinyMarker, smallMarker, mediumMarker,
              largerMarker);

        } else {
          addOverlayAtZoom(largerMarker, 8, 20);
          stopAndOverlays.addOverlays(largerMarker);
        }

      }
    }

    public void onFailure(Throwable ex) {
      _resultsPanel.add(new DivWidget("failure"));
      ExceptionSupport.handleException(ex);
    }

    private Marker getStopMarker(final StopBean stop, final LatLng p, ESize size) {
      MarkerOptions opts = MarkerOptions.newInstance();
      boolean isSelected = stop.getId().equals(_selectedStop);

      Icon icon = StopIconFactory.getStopIcon(stop, size, isSelected);
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

  private class ZoomOrMoveHandler implements MapZoomEndHandler,
      MapMoveEndHandler {

    public void onZoomEnd(MapZoomEndEvent event) {
      refreshStopsInCurrentView();
    }

    public void onMoveEnd(MapMoveEndEvent event) {
      refreshStopsInCurrentView();
    }
  }

  private class StopAndOverlays {

    private StopBean _stop;

    private List<Overlay> _overlays = new ArrayList<Overlay>();

    public StopAndOverlays(StopBean stop) {
      _stop = stop;
    }

    public StopBean getStop() {
      return _stop;
    }

    public List<Overlay> getOverlays() {
      return _overlays;
    }

    public void addOverlays(Overlay... overlays) {
      for (Overlay overlay : overlays)
        _overlays.add(overlay);
    }

  }

}
