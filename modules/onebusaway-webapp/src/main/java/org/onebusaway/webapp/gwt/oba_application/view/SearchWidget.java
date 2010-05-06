package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.gwt.common.control.FlexibleDateParser;
import org.onebusaway.webapp.gwt.common.layout.BoxLayoutManager;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.common.resources.CommonResources;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.oba_application.control.ConstraintsParameterMapping;
import org.onebusaway.webapp.gwt.oba_application.control.OneBusAwayStandardControl;
import org.onebusaway.webapp.gwt.oba_application.model.QueryModel;

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
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import java.util.Date;

public class SearchWidget extends FlowPanel {

  private static final String USING_LOCATION_TEXTBOX_STYLE = "SearchWidget-TextBox-UsingLocation";

  private static final int DEFAULT_SEARCH_WINDOW = 60;

  private static final int DEFAULT_TRIP_DURATION = 20;

  private static final int DEFAULT_WALKING_DISTANCE = 5280 / 2;

  private static final int DEFAULT_TRANSFERS = 1;

  private static DateTimeFormat _dateFormat = DateTimeFormat.getShortDateFormat();

  private static DateTimeFormat _timeFormat = DateTimeFormat.getShortTimeFormat();

  private QueryModelListener _modelListener = new QueryModelListener();

  private OneBusAwayStandardControl _control;

  private BoxLayoutManager _layoutManager;

  private OneBusAwayConstraintsBean _constraints = new OneBusAwayConstraintsBean();

  /*****************************************************************************
   * 
   ****************************************************************************/

  private TextBox _queryTextBox;

  private TextBox _addressTextBox;

  private DivPanel _optionsPanel;

  private TextBox _dateTextBox;

  private TextBox _timeTextBox;

  private ListBox _maxTripLengthBox;

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

  public SearchWidget() {
    setDefaultConstraints();
    initializeWidget();
    refreshWidgets();
  }

  public void setControl(OneBusAwayStandardControl control) {
    _control = control;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public ModelListener<QueryModel> getQueryModelHandler() {
    return _modelListener;
  }

  public void setLayoutManager(BoxLayoutManager layout) {
    _layoutManager = layout;
  }

  private void setDefaultConstraints() {
    _constraints.setMaxTransfers(DEFAULT_TRANSFERS);
    _constraints.setMaxTripDuration(DEFAULT_TRIP_DURATION);
    _constraints.setMaxWalkingDistance(DEFAULT_WALKING_DISTANCE);
    long t = System.currentTimeMillis();
    _constraints.setMinDepartureTime(t);
    _constraints.setMaxDepartureTime(t + DEFAULT_SEARCH_WINDOW * 60 * 1000);
  }

  private void initializeWidget() {

    addStyleName("SearchWidget");

    FormPanel form = new FormPanel();
    form.setAction("index.html");

    add(form);

    form.addSubmitHandler( new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        event.cancel();
      }
    });

    FlowPanel panel = new FlowPanel();
    form.add(panel);

    DivPanel searchPanel = new DivPanel();
    searchPanel.addStyleName("SearchWidget-SearchPanel");
    panel.add(searchPanel);

    DivPanel searchForPanel = new DivPanel();
    searchForPanel.addStyleName("SearchWidget-SearchForPanel");
    searchPanel.add(searchForPanel);

    DivWidget queryLabel = new DivWidget("Search for:");
    queryLabel.addStyleName("SearchWidget-Label");
    searchForPanel.add(queryLabel);

    DivPanel queryTextBoxPanel = new DivPanel();
    queryTextBoxPanel.addStyleName("SearchWidget-TextBoxPanel");
    searchForPanel.add(queryTextBoxPanel);

    _queryTextBox = new TextBox();
    _queryTextBox.addStyleName("SearchWidget-TextBox");
    _queryTextBox.setName(ConstraintsParameterMapping.PARAM_QUERY);
    _queryTextBox.addKeyPressHandler(new QueryTextBoxHandler());
    queryTextBoxPanel.add(_queryTextBox);

    DivPanel searchForExamplePanel = new DivPanel();
    searchForExamplePanel.addStyleName("SearchWidget-ExamplePanel");
    searchForPanel.add(searchForExamplePanel);

    DivWidget searchForExampleLabel = new DivWidget("(ex. \"restaurants\", \"parks\", \"grocery stores\")");
    searchForExampleLabel.addStyleName("SearchWidget-ExampleLabel");
    searchForExamplePanel.add(searchForExampleLabel);

    DivPanel addressPanel = new DivPanel();
    searchPanel.add(addressPanel);

    DivPanel addressPanel1 = new DivPanel();
    addressPanel.add(addressPanel1);

    DivWidget addressLabel = new DivWidget("Start Address:");
    addressLabel.addStyleName("SearchWidget-Label");
    addressPanel1.add(addressLabel);

    DivPanel addressTextBoxPanel = new DivPanel();
    addressTextBoxPanel.addStyleName("SearchWidget-TextBoxPanel");
    addressPanel1.add(addressTextBoxPanel);

    _addressTextBox = new TextBox();
    _addressTextBox.addStyleName("SearchWidget-TextBox");
    _addressTextBox.setName(ConstraintsParameterMapping.PARAM_LOCATION);
    addressTextBoxPanel.add(_addressTextBox);

    DivPanel addressPanel2 = new DivPanel();
    addressPanel2.addStyleName("SearchWidget-ExamplePanel");
    addressPanel.add(addressPanel2);

    SpanWidget addressExampleLabel1 = new SpanWidget("(ex. \"3rd and pike\" or ");
    addressExampleLabel1.addStyleName("SearchWidget-ExampleLabel");
    addressPanel2.add(addressExampleLabel1);

    Anchor addressExampleLabel2 = new Anchor("use the map");
    addressExampleLabel2.addStyleName("SearchWidget-ExampleLabel");
    addressExampleLabel2.addClickHandler(new UseTheMapHandler());
    addressPanel2.add(addressExampleLabel2);

    SpanWidget addressExampleLabel3 = new SpanWidget(")");
    addressExampleLabel3.addStyleName("SearchWidget-ExampleLabel");
    addressPanel2.add(addressExampleLabel3);

    DivPanel buttonPanel = new DivPanel();
    buttonPanel.addStyleName("SearchWidget-ButtonPanel");
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
    _optionsButton.addStyleName("SearchWidget-ShowOptionsButton");
    _optionsButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
        toggleExpansion();
        _layoutManager.refresh();
      }
    });
    buttonPanel.add(_optionsButton);

    DivPanel clearPanel = new DivPanel();
    clearPanel.addStyleName("ClearPanel");
    panel.add(clearPanel);
    Image hiddenPixel = new Image(CommonResources.INSTANCE.getHiddenPixel().getUrl());
    clearPanel.add(hiddenPixel);

    _optionsPanel = new DivPanel();
    _optionsPanel.addStyleName("SearchWidget-OptionsPanel");
    _optionsPanel.setVisible(false);
    panel.add(_optionsPanel);

    Grid optionsGrid = new Grid(2, 4);
    optionsGrid.addStyleName("SearchWidget-OptionsGrid");
    for (int i = 0; i < 4; i++) {
      optionsGrid.getCellFormatter().addStyleName(0, i, "SearchWidget-OptionsGrid-Column" + i);
      optionsGrid.getCellFormatter().addStyleName(1, i, "SearchWidget-OptionsGrid-Column" + i);
    }
    _optionsPanel.add(optionsGrid);

    SpanWidget timeLabel = new SpanWidget("Start Time:");
    optionsGrid.setWidget(0, 0, timeLabel);

    DivPanel dateAndTimePanel = new DivPanel();
    optionsGrid.setWidget(0, 1, dateAndTimePanel);
    _dateTextBox = new TextBox();
    _dateTextBox.addStyleName("SearchWidget-StartDateTextBox");
    dateAndTimePanel.add(_dateTextBox);
    _timeTextBox = new TextBox();
    _timeTextBox.addStyleName("SearchWidget-StartTimeTextBox");
    dateAndTimePanel.add(_timeTextBox);

    SpanWidget maxLengthLabel = new SpanWidget("Trip Time:");
    optionsGrid.setWidget(1, 0, maxLengthLabel);

    _maxTripLengthBox = new ListBox();
    _maxTripLengthBox.addItem("10 mins", "10");
    _maxTripLengthBox.addItem("15 mins", "15");
    _maxTripLengthBox.addItem("20 mins", "20");
    _maxTripLengthBox.addItem("30 mins", "30");
    _maxTripLengthBox.addItem("45 mins", "45");
    _maxTripLengthBox.addItem("1 hour", "60");
    _maxTripLengthBox.addStyleName("SearchWidget-TripLengthList");
    optionsGrid.setWidget(1, 1, _maxTripLengthBox);

    SpanWidget maxTransfersLabel = new SpanWidget("Transfers:");
    optionsGrid.setWidget(0, 2, maxTransfersLabel);

    _maxTransfersListBox = new ListBox();
    _maxTransfersListBox.addItem("Don't Care", "-1");
    _maxTransfersListBox.addItem("0", "0");
    _maxTransfersListBox.addItem("1", "1");
    _maxTransfersListBox.addItem("2", "2");
    optionsGrid.setWidget(0, 3, _maxTransfersListBox);

    SpanWidget maxWalkLabel = new SpanWidget("Walk at most:");
    optionsGrid.setWidget(1, 2, maxWalkLabel);

    _maxWalkDistance = new ListBox();
    _maxWalkDistance.addItem("1/4 mile", "1320");
    _maxWalkDistance.addItem("1/2 mile", "2640");
    _maxWalkDistance.addItem("3/4 mile", "3960");
    _maxWalkDistance.addItem("1 mile", "5280");
    optionsGrid.setWidget(1, 3, _maxWalkDistance);

    DivPanel optionsPanelRowB = new DivPanel();
    _optionsPanel.add(optionsPanelRowB);
  }

  private void refreshWidgets() {

    Date time = new Date(_constraints.getMinDepartureTime());
    _dateTextBox.setText(_dateFormat.format(time));
    _timeTextBox.setText(_timeFormat.format(time));

    setClosestIndex(_maxTransfersListBox, _constraints.getMaxTransfers());
    setClosestIndex(_maxTripLengthBox, _constraints.getMaxTripDuration());
    setClosestIndex(_maxWalkDistance, (int) _constraints.getMaxWalkingDistance());
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

    Date date = new Date();

    String dateValue = _dateTextBox.getText();
    String timeValue = _timeTextBox.getText();
    if (dateValue.length() > 0 && timeValue.length() > 0) {
      try {
        Date parsedDate = _dateFormat.parse(dateValue);
        FlexibleDateParser parser = new FlexibleDateParser();
        int minutes = parser.getMintuesSinceMidnight(timeValue);
        date = new Date(parsedDate.getTime() + minutes * 60 * 1000);
      } catch (FlexibleDateParser.DateParseException ex) {
        System.err.println("bad time=" + dateValue + " " + timeValue);
      }
    }
    
    System.out.println("date=" + date);

    _constraints.setMinDepartureTime(date.getTime());
    _constraints.setMaxDepartureTime(date.getTime() + DEFAULT_SEARCH_WINDOW * 60 * 1000);

    int maxTransfersIndex = _maxTransfersListBox.getSelectedIndex();
    if (maxTransfersIndex != -1)
      _constraints.setMaxTransfers(Integer.parseInt(_maxTransfersListBox.getValue(maxTransfersIndex)));

    int maxTripDurationIndex = _maxTripLengthBox.getSelectedIndex();
    if (maxTripDurationIndex != -1)
      _constraints.setMaxTripDuration(Integer.parseInt(_maxTripLengthBox.getValue(maxTripDurationIndex)));

    int maxWalkDistanceindex = _maxWalkDistance.getSelectedIndex();
    if (maxWalkDistanceindex != -1)
      _constraints.setMaxWalkingDistance(Integer.parseInt(_maxWalkDistance.getValue(maxWalkDistanceindex)));
  }

  private void toggleExpansion() {
    _optionsExpanded = !_optionsExpanded;
    _optionsPanel.setVisible(_optionsExpanded);
    _optionsButton.setText(_optionsExpanded ? "Hide Options" : "Show More Options");
  }

  private void handleQuery() {

    String queryText = _queryTextBox.getText();
    String addressText = _addressTextBox.getText();

    if (queryText == null || queryText.length() == 0)
      return;

    if (!_usingLocation && (addressText == null || addressText.length() == 0))
      return;

    refreshConstraints();

    if (_usingLocation)
      addressText = "";

    _control.query(queryText, addressText, _location, _constraints);
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

  private class QueryTextBoxHandler implements KeyPressHandler {
    public void onKeyPress(KeyPressEvent event) {
      if (event.getCharCode() == '\n' && ! event.isAnyModifierKeyDown() ) {
        handleQuery();
      }
    }

  }

  private class AddressTextBoxHandler implements KeyPressHandler, FocusHandler, BlurHandler {

    public void onKeyPress(KeyPressEvent event) {
      if (event.getCharCode() == '\n' && ! event.isAnyModifierKeyDown() ) {
        handleQuery();
      } else {
        _usingLocation = false;
        _location = null;
        if (_locationMarker != null)
          _map.removeOverlay(_locationMarker);
      }
    }

    public void onFocus(FocusEvent event) {
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

  private class QueryModelListener implements ModelListener<QueryModel> {

    public void handleUpdate(QueryModel model) {

      _queryTextBox.setText(model.getQuery());

      _constraints = new OneBusAwayConstraintsBean(model.getConstraints());

      String locationValue = model.getLocationQuery();
      if ((locationValue == null || locationValue.length() == 0) && model.getLocation() != null) {
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
