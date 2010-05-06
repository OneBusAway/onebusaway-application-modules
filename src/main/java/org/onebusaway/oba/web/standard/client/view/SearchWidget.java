package org.onebusaway.oba.web.standard.client.view;

import org.onebusaway.common.web.common.client.model.ModelListener;
import org.onebusaway.common.web.common.client.resources.CommonResources;
import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.oba.web.standard.client.control.ConstraintsParameterMapping;
import org.onebusaway.oba.web.standard.client.control.OneBusAwayStandardPresenter;
import org.onebusaway.oba.web.standard.client.model.QueryModel;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;

public class SearchWidget extends FlowPanel {

  private static final String USING_LOCATION_TEXTBOX_STYLE = "SearchWidget-TextBox-UsingLocation";

  private static final int DEFAULT_SEARCH_WINDOW = 60;

  private static final int DEFAULT_TRIP_DURATION = 20;

  private static final int DEFAULT_WALKING_DISTANCE = 5280 / 2;

  private static final int DEFAULT_TRANSFERS = 1;

  private static DateTimeFormat _dateFormat = DateTimeFormat.getFormat("MM/dd");

  private static DateTimeFormat _timeFormat = DateTimeFormat.getFormat("hh:mm aa");

  private static DateTimeFormat _dateAndTimeFormat = DateTimeFormat.getFormat("MM/dd hh:mm aa");

  private QueryModelListener _modelListener = new QueryModelListener();

  private OneBusAwayStandardPresenter _presenter;

  private OneBusAwayConstraintsBean _constraints = new OneBusAwayConstraintsBean();

  /*****************************************************************************
   * 
   ****************************************************************************/

  private TextBox _queryTextBox;

  private TextBox _addressTextBox;

  private DivPanel _moreOptionsPanel;

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

  public void setPresenter(OneBusAwayStandardPresenter presenter) {
    _presenter = presenter;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public ModelListener<QueryModel> getQueryModelHandler() {
    return _modelListener;
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
    add(form);

    FlowPanel panel = new FlowPanel();
    form.add(panel);

    DivPanel searchPanel = new DivPanel();
    searchPanel.addStyleName("SearchWidget-SearchPanel");
    panel.add(searchPanel);

    DivPanel searchForPanel = new DivPanel();
    searchPanel.add(searchForPanel);
    searchForPanel.addStyleName("SearchWidget-SearchForPanel");

    DivPanel searchForPanel1 = new DivPanel();
    searchForPanel.add(searchForPanel1);

    DivWidget queryLabel = new DivWidget("Search for:");
    queryLabel.addStyleName("SearchWidget-Label");
    searchForPanel1.add(queryLabel);

    DivPanel queryTextBoxPanel = new DivPanel();
    queryTextBoxPanel.addStyleName("SearchWidget-TextBoxPanel");
    searchForPanel1.add(queryTextBoxPanel);
    _queryTextBox = new TextBox();
    _queryTextBox.addStyleName("SearchWidget-TextBox");
    _queryTextBox.setName(ConstraintsParameterMapping.PARAM_QUERY);
    _queryTextBox.addKeyboardListener(new QueryTextBoxHandler());
    queryTextBoxPanel.add(_queryTextBox);

    DivPanel searchForPanel2 = new DivPanel();
    searchForPanel2.addStyleName("SearchWidget-ExamplePanel");
    searchForPanel.add(searchForPanel2);

    SpanWidget searchForExampleLabel = new SpanWidget("(ex. \"restaurants\", \"parks\", \"grocery stores\")");
    searchForExampleLabel.addStyleName("SearchWidget-ExampleLabel");
    searchForPanel2.add(searchForExampleLabel);

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
    addressExampleLabel2.addClickListener(new UseTheMapHandler());
    addressPanel2.add(addressExampleLabel2);

    SpanWidget addressExampleLabel3 = new SpanWidget(")");
    addressExampleLabel3.addStyleName("SearchWidget-ExampleLabel");
    addressPanel2.add(addressExampleLabel3);

    DivPanel buttonPanel = new DivPanel();
    searchPanel.add(buttonPanel);

    Button button = new Button("Go");
    buttonPanel.add(button);

    button.addClickListener(new ClickListener() {
      public void onClick(Widget widget) {
        handleQuery();
      }
    });

    AddressTextBoxHandler handler = new AddressTextBoxHandler();
    _addressTextBox.addKeyboardListener(handler);
    _addressTextBox.addFocusListener(handler);

    _moreOptionsPanel = new DivPanel();
    buttonPanel.add(_moreOptionsPanel);

    _optionsButton = new Anchor("Show More Options");
    _optionsButton.addStyleName("SearchWidget-ShowOptionsButton");
    _optionsButton.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        toggleExpansion();
      }
    });
    _moreOptionsPanel.add(_optionsButton);

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

    String timeValue = _dateTextBox.getText() + " " + _timeTextBox.getText();
    Date date = _dateAndTimeFormat.parse(timeValue);
    System.out.println(date);
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

    _presenter.query(queryText, addressText, _location, _constraints);
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

  private class QueryTextBoxHandler extends KeyboardListenerAdapter {
    public void onKeyPress(Widget widget, char keyCode, int modifiers) {
      if (keyCode == KeyboardListener.KEY_ENTER && modifiers == 0) {
        handleQuery();
      }
    }
  }
  private class AddressTextBoxHandler extends KeyboardListenerAdapter implements FocusListener {

    public void onKeyPress(Widget widget, char keyCode, int modifiers) {
      if (keyCode == KeyboardListener.KEY_ENTER && modifiers == 0) {
        handleQuery();
      } else {
        _usingLocation = false;
        _location = null;
        if (_locationMarker != null)
          _map.removeOverlay(_locationMarker);
      }
    }

    public void onFocus(Widget widget) {
      System.out.println("onFocus");
      if (_usingLocation) {
        _addressTextBox.removeStyleName(USING_LOCATION_TEXTBOX_STYLE);
        _addressTextBox.setText("");
      }
    }

    public void onLostFocus(Widget widget) {
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

  private class UseTheMapHandler implements ClickListener, MapClickHandler {

    public void onClick(Widget widget) {

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
