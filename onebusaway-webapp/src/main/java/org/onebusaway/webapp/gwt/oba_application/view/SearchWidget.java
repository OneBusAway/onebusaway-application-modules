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
package org.onebusaway.webapp.gwt.oba_application.view;

import java.util.Date;

import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.webapp.gwt.common.control.FlexibleDateParser;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.common.resources.CommonResources;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.oba_application.control.ConstraintsParameterMapping;
import org.onebusaway.webapp.gwt.oba_application.control.OneBusAwayStandardControl;
import org.onebusaway.webapp.gwt.oba_application.model.QueryModel;
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
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ResizableDockLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SearchWidget extends FlowPanel {

  private static OneBusAwayCssResource _css = OneBusAwayStandardResources.INSTANCE.getCss();

  private static final String USING_LOCATION_TEXTBOX_STYLE = _css.SearchWidgetTextBoxUsingLocation();

  private static final int DEFAULT_SEARCH_WINDOW = 60;

  private static final int DEFAULT_TRIP_DURATION = 20;

  private static final int DEFAULT_WALKING_DISTANCE = 5280 / 2;

  private static final int DEFAULT_TRANSFERS = 1;

  private static DateTimeFormat _dateFormat = DateTimeFormat.getShortDateFormat();

  private static DateTimeFormat _timeFormat = DateTimeFormat.getShortTimeFormat();

  private QueryModelListener _modelListener = new QueryModelListener();

  private OneBusAwayStandardControl _control;

  private long _time = System.currentTimeMillis();

  private TransitShedConstraintsBean _constraints = new TransitShedConstraintsBean();

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

  private ResizableDockLayoutPanel _dockLayoutPanelParent;

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

  public void setDockLayoutPanelParent(ResizableDockLayoutPanel dockLayoutPanel) {
    _dockLayoutPanelParent = dockLayoutPanel;
  }

  private void setDefaultConstraints() {

    _time = System.currentTimeMillis();

    _constraints.setMaxInitialWaitTime(DEFAULT_SEARCH_WINDOW * 60);

    ConstraintsBean c = _constraints.getConstraints();
    c.setMaxTransfers(DEFAULT_TRANSFERS);
    c.setMaxTripDuration(DEFAULT_TRIP_DURATION * 60);
    c.setMaxWalkingDistance(DEFAULT_WALKING_DISTANCE);
  }

  private void initializeWidget() {

    addStyleName(_css.SearchWidget());

    FormPanel form = new FormPanel();
    form.setAction("index.html");

    add(form);

    form.addSubmitHandler(new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        event.cancel();
      }
    });

    FlowPanel panel = new FlowPanel();
    form.add(panel);

    DivPanel searchPanel = new DivPanel();
    searchPanel.addStyleName(_css.SearchWidgetSearchPanel());
    panel.add(searchPanel);

    DivPanel searchForPanel = new DivPanel();
    searchForPanel.addStyleName(_css.SearchWidgetSearchForPanel());
    searchPanel.add(searchForPanel);

    DivWidget queryLabel = new DivWidget("Search for:");
    queryLabel.addStyleName(_css.SearchWidgetLabel());
    searchForPanel.add(queryLabel);

    DivPanel queryTextBoxPanel = new DivPanel();
    queryTextBoxPanel.addStyleName(_css.SearchWidgetTextBoxPanel());
    searchForPanel.add(queryTextBoxPanel);

    _queryTextBox = new TextBox();
    _queryTextBox.addStyleName(_css.SearchWidgetTextBox());
    _queryTextBox.setName(ConstraintsParameterMapping.PARAM_QUERY);
    _queryTextBox.addKeyPressHandler(new QueryTextBoxHandler());
    queryTextBoxPanel.add(_queryTextBox);

    DivPanel searchForExamplePanel = new DivPanel();
    searchForExamplePanel.addStyleName(_css.SearchWidgetExamplePanel());
    searchForPanel.add(searchForExamplePanel);

    DivWidget searchForExampleLabel = new DivWidget(
        "(ex. \"restaurants\", \"parks\", \"grocery stores\")");
    searchForExampleLabel.addStyleName(_css.SearchWidgetExampleLabel());
    searchForExamplePanel.add(searchForExampleLabel);

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
        // TODO : Refresh layout
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

    Grid optionsGrid = new Grid(2, 4);
    optionsGrid.addStyleName(_css.SearchWidgetOptionsGrid());
    for (int i = 0; i < 4; i++) {
      optionsGrid.getCellFormatter().addStyleName(0, i,
          "SearchWidget-OptionsGrid-Column" + i);
      optionsGrid.getCellFormatter().addStyleName(1, i,
          "SearchWidget-OptionsGrid-Column" + i);
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

    SpanWidget maxLengthLabel = new SpanWidget("Trip Time:");
    optionsGrid.setWidget(1, 0, maxLengthLabel);

    _maxTripLengthBox = new ListBox();
    _maxTripLengthBox.addItem("10 mins", "10");
    _maxTripLengthBox.addItem("15 mins", "15");
    _maxTripLengthBox.addItem("20 mins", "20");
    _maxTripLengthBox.addItem("30 mins", "30");
    _maxTripLengthBox.addItem("45 mins", "45");
    _maxTripLengthBox.addItem("1 hour", "60");
    _maxTripLengthBox.addStyleName(_css.SearchWidgetTripLengthList());
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

    Date time = new Date(_time);
    _dateTextBox.setText(_dateFormat.format(time));
    _timeTextBox.setText(_timeFormat.format(time));

    ConstraintsBean c = _constraints.getConstraints();
    setClosestIndex(_maxTransfersListBox, c.getMaxTransfers());
    setClosestIndex(_maxTripLengthBox, c.getMaxTripDuration() / 60);
    setClosestIndex(_maxWalkDistance, (int) c.getMaxWalkingDistance());
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

    _time = date.getTime();

    ConstraintsBean c = _constraints.getConstraints();

    int maxTransfersIndex = _maxTransfersListBox.getSelectedIndex();
    if (maxTransfersIndex != -1)
      c.setMaxTransfers(Integer.parseInt(_maxTransfersListBox.getValue(maxTransfersIndex)));

    int maxTripDurationIndex = _maxTripLengthBox.getSelectedIndex();
    if (maxTripDurationIndex != -1)
      c.setMaxTripDuration(Integer.parseInt(_maxTripLengthBox.getValue(maxTripDurationIndex)) * 60);

    int maxWalkDistanceindex = _maxWalkDistance.getSelectedIndex();
    if (maxWalkDistanceindex != -1)
      c.setMaxWalkingDistance(Integer.parseInt(_maxWalkDistance.getValue(maxWalkDistanceindex)));
  }

  private void toggleExpansion() {
    _optionsExpanded = !_optionsExpanded;
    _optionsPanel.setVisible(_optionsExpanded);
    _optionsButton.setText(_optionsExpanded ? "Hide Options"
        : "Show More Options");
    double size = _optionsExpanded ? 8 : 4;
    _dockLayoutPanelParent.setWidgetSize(this, size);
    _dockLayoutPanelParent.forceLayout();
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

    _control.query(queryText, addressText, _location, _time, _constraints);
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
      if (event.getCharCode() == '\n' && !event.isAnyModifierKeyDown()) {
        handleQuery();
      }
    }

  }

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

      _constraints = new TransitShedConstraintsBean(model.getConstraints());

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
