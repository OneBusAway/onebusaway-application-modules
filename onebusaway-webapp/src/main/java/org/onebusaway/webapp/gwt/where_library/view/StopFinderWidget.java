package org.onebusaway.webapp.gwt.where_library.view;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.where_library.view.events.StopClickedEvent;
import org.onebusaway.webapp.gwt.where_library.view.events.StopClickedHandler;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;

public class StopFinderWidget extends Composite {

  private static MyUiBinder _uiBinder = GWT.create(MyUiBinder.class);

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  @UiField
  FlowPanel _titlePanel;

  @UiField
  FormPanel _searchFormPanel;

  @UiField
  TextBox _searchTextBox;
  
  @UiField
  FlowPanel _linksPanel;
  
  @UiField
  Anchor _currentLinkAnchor;

  @UiField
  FlowPanel _resultsPanel;

  @UiField
  MapWidgetComposite _mapPanel;
  
  @UiField
  StopFinderCssResource style;

  protected MapWidget _map = new MapWidget(_center, _zoom);

  protected TransitMapManager _transitMapManager = new TransitMapManager(_map);

  protected StopFinderInterface _stopFinder;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public StopFinderWidget() {
    initWidget(_uiBinder.createAndBindUi(this));

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

    _transitMapManager.addStopClickedHandler(new StopClickedHandler() {
      @Override
      public void handleStopClicked(StopClickedEvent event) {
        StopBean stop = event.getStop();
        InfoWindow window = _map.getInfoWindow();
        LatLng point = LatLng.newInstance(stop.getLat(), stop.getLon());
        Widget widget = getStopInfoWindowWidget(stop,style);
        window.open(point, new InfoWindowContent(widget));
      }
    });
  }

  public void setStopFinder(StopFinderInterface stopFinder) {
    _stopFinder = stopFinder;
  }

  public StopFinderInterface getStopFinder() {
    return _stopFinder;
  }

  public MapWidget getMapWidget() {
    return _map;
  }

  public TransitMapManager getTransitMapManager() {
    return _transitMapManager;
  }
  
  public StopFinderCssResource getCss() {
    return style;
  }

  @UiFactory
  MapWidgetComposite makeMapPanel() {
    return new MapWidgetComposite(_map);
  }

  /****
   * Public Methods
   ****/

  public void setTitleWidget(Widget widget) {
    _titlePanel.clear();
    _titlePanel.add(widget);
  }

  public void setSearchText(String value) {
    _searchTextBox.setText(value);
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        _searchTextBox.setFocus(true);
      }
    });
  }

  public void resetContents() {
    _map.clearOverlays();
    _resultsPanel.clear();
  }

  public Panel getResultsPanel() {
    return _resultsPanel;
  }

  /****
   * Local Methods
   ****/

  @UiHandler("_searchFormPanel")
  void doSubmit(SubmitEvent event) {

    event.cancel();

    String value = _searchTextBox.getText();

    if (value == null)
      return;

    value = value.trim();

    if (value.length() == 0)
      return;

    _stopFinder.query(value);
  }
  
  @UiHandler("_currentLinkAnchor")
  void onCurrentLinkAnchorMouseOver(MouseOverEvent event) {
    String url = _stopFinder.getCurrentViewAsUrl();
    _currentLinkAnchor.setHref(url);
  }


  protected Widget getStopInfoWindowWidget(StopBean stop, StopFinderCssResource css) {
    return new StopInfoWindowWidget(_stopFinder, _transitMapManager, stop,css);
  }
  
  protected void hideLinksPanel() {
    _linksPanel.setVisible(false);
  }

  interface MyUiBinder extends UiBinder<Widget, StopFinderWidget> {
  }

}
