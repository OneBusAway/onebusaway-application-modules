package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.gwt.common.layout.BoxLayoutManager;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.common.resources.CommonResources;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.oba_application.control.ConstraintsParameterMapping;
import org.onebusaway.webapp.gwt.oba_application.control.TransitScoreControl;
import org.onebusaway.webapp.gwt.oba_application.model.LocationQueryModel;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayCssResource;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayStandardResources;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Date;

public class TransitScoreSearchPresenter extends FlowPanel {

  private static OneBusAwayCssResource _css = OneBusAwayStandardResources.INSTANCE.getCss();
  
  private static final String USING_LOCATION_TEXTBOX_STYLE = _css.SearchWidgetTextBoxUsingLocation();

  private static final int DEFAULT_SEARCH_WINDOW = 60;

  private static final int DEFAULT_WALKING_DISTANCE = 5280 / 2;

  private static final int DEFAULT_TRANSFERS = 1;

  private static DateTimeFormat _dateFormat = DateTimeFormat.getFormat("MM/dd");

  private static DateTimeFormat _timeFormat = DateTimeFormat.getFormat("hh:mm aa");

  private static DateTimeFormat _dateAndTimeFormat = DateTimeFormat.getFormat("MM/dd hh:mm aa");

  private QueryModelListener _modelListener = new QueryModelListener();

  private TransitScoreControl _control;

  private BoxLayoutManager _layoutManager;

  private OneBusAwayConstraintsBean _constraints = new OneBusAwayConstraintsBean();

  /*****************************************************************************
   * 
   ****************************************************************************/

  private TextBox _addressTextBox;

  private DivPanel _optionsPanel;

  private TextBox _dateTextBox;

  private TextBox _timeTextBox;

  private ListBox _maxTransfersListBox;

  private ListBox _maxWalkDistance;

  /****
   * Location Selection
   ****/

  private MapWidget _map;

  private boolean _usingLocation = false;

  private LatLng _location = null;

  private Marker _locationMarker = null;

  /****
   * Options Panel
   ****/

  private boolean _optionsExpanded = false;

  private Anchor _optionsButton;

  public TransitScoreSearchPresenter() {
    setDefaultConstraints();
    initializeWidget();
    refreshWidgets();
  }

  public void setControl(TransitScoreControl control) {
    _control = control;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public ModelListener<LocationQueryModel> getQueryModelHandler() {
    return _modelListener;
  }

  public void setLayoutManager(BoxLayoutManager layout) {
    _layoutManager = layout;
  }

  private void setDefaultConstraints() {
    _constraints.setMaxTransfers(DEFAULT_TRANSFERS);
    _constraints.setMaxWalkingDistance(DEFAULT_WALKING_DISTANCE);
    long t = System.currentTimeMillis();
    _constraints.setMinDepartureTime(t);
    _constraints.setMaxDepartureTime(t + DEFAULT_SEARCH_WINDOW * 60 * 1000);
  }

  private void initializeWidget() {

    addStyleName(_css.SearchWidget());

    FormPanel form = new FormPanel();
    add(form);

    FlowPanel panel = new FlowPanel();
    form.add(panel);

    DivPanel searchPanel = new DivPanel();
    searchPanel.addStyleName(_css.SearchWidgetSearchPanel());
    panel.add(searchPanel);

    DivPanel addressPanel = new DivPanel();
    searchPanel.add(addressPanel);

    DivPanel addressPanel1 = new DivPanel();
    addressPanel.add(addressPanel1);

    DivWidget addressLabel = new DivWidget("Start Address:");
    addressLabel.addStyleName(_css.SearchWidgetLabel());
    addressPanel1.add(addressLabel);

    DivPanel addressTextBoxPanel = new DivPanel();
    addressTextBoxPanel.addStyleName(_css.SearchWidgetTextBoxPanel());
    addressPanel1.add(addressTextBoxPanel);

    _addressTextBox = new TextBox();
    _addressTextBox.addStyleName(_css.SearchWidgetTextBox());
    _addressTextBox.setName(ConstraintsParameterMapping.PARAM_LOCATION);
    addressTextBoxPanel.add(_addressTextBox);

    DivPanel addressPanel2 = new DivPanel();
    addressPanel2.addStyleName(_css.SearchWidgetExamplePanel());
    addressPanel.add(addressPanel2);

    SpanWidget addressExampleLabel1 = new SpanWidget(
        "(ex. \"3rd and pike\" or ");
    addressExampleLabel1.addStyleName(_css.SearchWidgetExampleLabel());
    addressPanel2.add(addressExampleLabel1);

    Anchor addressExampleLabel2 = new Anchor("use the map");
    addressExampleLabel2.addStyleName(_css.SearchWidgetExampleLabel());
    addressExampleLabel2.addClickHandler(new UseTheMapHandler());
    addressPanel2.add(addressExampleLabel2);

    SpanWidget addressExampleLabel3 = new SpanWidget(")");
    addressExampleLabel3.addStyleName(_css.SearchWidgetExampleLabel());
    addressPanel2.add(addressExampleLabel3);

    DivPanel buttonPanel = new DivPanel();
    buttonPanel.addStyleName(_css.SearchWidgetButtonPanel());
    searchPanel.add(buttonPanel);

    Button button = new Button("Go");
    buttonPanel.add(button);

    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent widget) {
        handleQuery();
      }
    });

    AddressTextBoxHandler handler = new AddressTextBoxHandler();
    _addressTextBox.addKeyPressHandler(handler);
    _addressTextBox.addFocusHandler(handler);
    _addressTextBox.addBlurHandler(handler);

    _optionsButton = new Anchor("Show More Options");
    _optionsButton.addStyleName(_css.SearchWidgetShowOptionsButton());
    _optionsButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
        toggleExpansion();
        _layoutManager.refresh();
      }
    });
    buttonPanel.add(_optionsButton);

    DivPanel clearPanel = new DivPanel();
    clearPanel.addStyleName(_css.ClearPanel());
    panel.add(clearPanel);
    Image hiddenPixel = new Image(
        CommonResources.INSTANCE.getHiddenPixel().getUrl());
    clearPanel.add(hiddenPixel);

    _optionsPanel = new DivPanel();
    _optionsPanel.addStyleName(_css.SearchWidgetOptionsPanel());
    _optionsPanel.setVisible(false);
    panel.add(_optionsPanel);

    Grid optionsGrid = new Grid(3, 2);
    optionsGrid.addStyleName(_css.SearchWidgetOptionsGrid());
    for (int col = 0; col < 2; col++) {
      for (int row = 0; row < 3; row++)
        optionsGrid.getCellFormatter().addStyleName(row, col,
            "SearchWidget-OptionsGrid-Column" + col);
    }
    _optionsPanel.add(optionsGrid);

    SpanWidget timeLabel = new SpanWidget("Start Time:");
    optionsGrid.setWidget(0, 0, timeLabel);

    DivPanel dateAndTimePanel = new DivPanel();
    optionsGrid.setWidget(0, 1, dateAndTimePanel);
    _dateTextBox = new TextBox();
    _dateTextBox.addStyleName(_css.SearchWidgetStartDateTextBox());
    dateAndTimePanel.add(_dateTextBox);
    _timeTextBox = new TextBox();
    _timeTextBox.addStyleName(_css.SearchWidgetStartTimeTextBox());
    dateAndTimePanel.add(_timeTextBox);

    SpanWidget maxTransfersLabel = new SpanWidget("Transfers:");
    optionsGrid.setWidget(1, 0, maxTransfersLabel);

    _maxTransfersListBox = new ListBox();
    _maxTransfersListBox.addItem("Don't Care", "-1");
    _maxTransfersListBox.addItem("0", "0");
    _maxTransfersListBox.addItem("1", "1");
    _maxTransfersListBox.addItem("2", "2");
    optionsGrid.setWidget(1, 1, _maxTransfersListBox);

    SpanWidget maxWalkLabel = new SpanWidget("Walk at most:");
    optionsGrid.setWidget(2, 0, maxWalkLabel);

    _maxWalkDistance = new ListBox();
    _maxWalkDistance.addItem("1/4 mile", "1320");
    _maxWalkDistance.addItem("1/2 mile", "2640");
    _maxWalkDistance.addItem("3/4 mile", "3960");
    _maxWalkDistance.addItem("1 mile", "5280");
    optionsGrid.setWidget(2, 1, _maxWalkDistance);

    DivPanel optionsPanelRowB = new DivPanel();
    _optionsPanel.add(optionsPanelRowB);
  }

  private void refreshWidgets() {

    Date time = new Date(_constraints.getMinDepartureTime());
    _dateTextBox.setText(_dateFormat.format(time));
    _timeTextBox.setText(_timeFormat.format(time));

    setClosestIndex(_maxTransfersListBox, _constraints.getMaxTransfers());
    setClosestIndex(_maxWalkDistance,
        (int) _constraints.getMaxWalkingDistance());
  }

  private void setClosestIndex(ListBox listBox, int value) {
    int minIndex = -1;
    double minDiff = 0;
    for (int i = 0; i < listBox.getItemCount(); i++) {
      Integer itemValue = Integer.parseInt(listBox.getValue(i));
      double diff = Math.abs(value - itemValue);
      if (minIndex == -1 || diff < minDiff) {
        minIndex = i;
        minDiff = diff;
      }
    }

    if (minIndex != -1)
      listBox.setSelectedIndex(minIndex);
  }

  private void refreshConstraints() {

    String timeValue = _dateTextBox.getText() + " " + _timeTextBox.getText();
    Date date = _dateAndTimeFormat.parse(timeValue);
    System.out.println(date);
    _constraints.setMinDepartureTime(date.getTime());
    _constraints.setMaxDepartureTime(date.getTime() + DEFAULT_SEARCH_WINDOW
        * 60 * 1000);

    int maxTransfersIndex = _maxTransfersListBox.getSelectedIndex();
    if (maxTransfersIndex != -1)
      _constraints.setMaxTransfers(Integer.parseInt(_maxTransfersListBox.getValue(maxTransfersIndex)));

    int maxWalkDistanceindex = _maxWalkDistance.getSelectedIndex();
    if (maxWalkDistanceindex != -1)
      _constraints.setMaxWalkingDistance(Integer.parseInt(_maxWalkDistance.getValue(maxWalkDistanceindex)));
  }

  private void toggleExpansion() {
    _optionsExpanded = !_optionsExpanded;
    _optionsPanel.setVisible(_optionsExpanded);
    _optionsButton.setText(_optionsExpanded ? "Hide Options"
        : "Show More Options");
  }

  private void handleQuery() {

    String addressText = _addressTextBox.getText();

    if (!_usingLocation && (addressText == null || addressText.length() == 0))
      return;

    refreshConstraints();

    if (_usingLocation)
      addressText = "";

    _control.query(addressText, _location, _constraints);
  }

  private void setLocation(LatLng location) {
    _addressTextBox.setText("See the marker on the map...");
    _addressTextBox.addStyleName(USING_LOCATION_TEXTBOX_STYLE);

    _location = location;

    if (_locationMarker != null) {
      _map.removeOverlay(_locationMarker);
      _locationMarker = null;
    }

    _locationMarker = new Marker(_location);
    _map.addOverlay(_locationMarker);

    _map.setCenter(_location);
    _usingLocation = true;
  }

  /*****************************************************************************
   * Inner Classes
   ****************************************************************************/

  private class AddressTextBoxHandler implements KeyPressHandler, FocusHandler,
      BlurHandler {

    public void onKeyPress(KeyPressEvent event) {
      if (event.getCharCode() == '\n' && !event.isAnyModifierKeyDown()) {
        handleQuery();
      } else {
        _usingLocation = false;
        _location = null;
        if (_locationMarker != null)
          _map.removeOverlay(_locationMarker);
      }
    }

    public void onFocus(FocusEvent arg0) {
      System.out.println("onFocus");
      if (_usingLocation) {
        _addressTextBox.removeStyleName(USING_LOCATION_TEXTBOX_STYLE);
        _addressTextBox.setText("");
      }
    }

    public void onBlur(BlurEvent arg0) {
      if (_usingLocation) {
        _addressTextBox.setText("See the marker on the map...");
        _addressTextBox.addStyleName(USING_LOCATION_TEXTBOX_STYLE);
      }
    }
  }

  private class QueryModelListener implements ModelListener<LocationQueryModel> {

    public void handleUpdate(LocationQueryModel model) {

      _constraints = new OneBusAwayConstraintsBean(model.getConstraints());

      String locationValue = model.getLocationQuery();
      if ((locationValue == null || locationValue.length() == 0)
          && model.getLocation() != null) {
        setLocation(model.getLocation());
      } else {
        _addressTextBox.setText(locationValue);
      }

      refreshWidgets();
    }
  }

  private class UseTheMapHandler implements ClickHandler, MapClickHandler {

    public void onClick(ClickEvent widget) {

      _addressTextBox.setText("Click on the map to pick a location...");
      _addressTextBox.addStyleName(USING_LOCATION_TEXTBOX_STYLE);

      _map.addMapClickHandler(this);
    }

    public void onClick(MapClickEvent event) {
      _map.removeMapClickHandler(this);
      setLocation(event.getLatLng());
    }
  }

}
