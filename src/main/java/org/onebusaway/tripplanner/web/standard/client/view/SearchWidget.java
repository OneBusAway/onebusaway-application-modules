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
package org.onebusaway.tripplanner.web.standard.client.view;

import org.onebusaway.common.web.common.client.resources.greenmarker.GreenMarkerNumberResources;
import org.onebusaway.common.web.common.client.widgets.DivPanel;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.tripplanner.web.common.client.model.TripPlannerConstraintsBean;
import org.onebusaway.tripplanner.web.standard.client.control.GeocoderHandlers;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;

public class SearchWidget extends FlowPanel {

  private static DateTimeFormat _dateFormat = DateTimeFormat.getFormat("MM/dd");

  private static DateTimeFormat _timeFormat = DateTimeFormat.getFormat("hh:mm aa");

  private static DateTimeFormat _dateAndTimeFormat = DateTimeFormat.getFormat("MM/dd hh:mm aa");

  private MapWidget _map;

  private FlowPanel _messagePanel;

  private TextBox _dateBox;

  private TextBox _timeBox;

  private GeocoderHandlers _geocoderHandler;

  public void setGeocoderHandler(GeocoderHandlers geocoderHandler) {
    _geocoderHandler = geocoderHandler;
  }

  public void setMapWidget(MapWidget map) {
    _map = map;
  }

  public void initialize() {

    FlowPanel startAddressPanel = new FlowPanel();
    add(startAddressPanel);

    final TextBox startAddressBox = new TextBox();
    startAddressBox.addStyleName("AddressBox");
    startAddressPanel.add(startAddressBox);

    FlowPanel destinationAddressPanel = new FlowPanel();
    add(destinationAddressPanel);

    final TextBox destinationAddressBox = new TextBox();
    destinationAddressBox.addStyleName("AddressBox");
    destinationAddressPanel.add(destinationAddressBox);

    Date now = new Date();

    FlowPanel dateAndTimePanel = new FlowPanel();
    add(dateAndTimePanel);

    _dateBox = new TextBox();
    _dateBox.setText(_dateFormat.format(now));
    dateAndTimePanel.add(_dateBox);

    _timeBox = new TextBox();
    _timeBox.setText(_timeFormat.format(now));
    dateAndTimePanel.add(_timeBox);

    FlowPanel submitPanel = new FlowPanel();
    add(submitPanel);

    Button submitButton = new Button("Get Directions");
    submitButton.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        String start = startAddressBox.getText();
        String destination = destinationAddressBox.getText();
        System.out.println("query=" + start + " to " + destination);
        _messagePanel.setVisible(false);
        _geocoderHandler.query(start, destination);
      }
    });
    submitPanel.add(submitButton);

    _messagePanel = new FlowPanel();
    _messagePanel.setVisible(false);
    add(_messagePanel);
  }

  public TripPlannerConstraintsBean getConstraints() {
    String date = _dateBox.getText();
    String time = _timeBox.getText();
    Date startTime = _dateAndTimeFormat.parse(date + " " + time);

    TripPlannerConstraintsBean constraints = new TripPlannerConstraintsBean();
    constraints.setMinDepartureTime(startTime.getTime());
    return constraints;
  }

  public void setAddressError(boolean isDestination) {
    _messagePanel.clear();

    String txt = isDestination ? "Could not find destination address" : "Could not find start address";
    SpanWidget msg = new SpanWidget(txt);
    _messagePanel.add(msg);

    _messagePanel.setVisible(true);
  }

  public void setMultipleAddresses(boolean isDestination, JsArray<Placemark> locations) {

    _messagePanel.setVisible(true);
    _messagePanel.clear();
    _map.clearOverlays();

    DivWidget didYouMean = new DivWidget("Did you mean:");
    _messagePanel.add(didYouMean);

    DivPanel panel = new DivPanel();
    _messagePanel.add(panel);

    System.out.println("count=" + locations.length());

    for (int i = 0; i < locations.length(); i++) {

      Placemark location = locations.get(i);

      DivPanel addressPanel = new DivPanel();
      panel.add(addressPanel);

      Image image = getMarkerImage(i);
      image.addStyleName("AddressMarker");
      addressPanel.add(image);

      DivPanel mainAddressPanel = new DivPanel();
      addressPanel.add(mainAddressPanel);

      Anchor mainAddressLabel = new Anchor(location.getStreet());
      mainAddressPanel.add(mainAddressLabel);

      DivWidget secondaryAddressLabel = new DivWidget(location.getAddress());
      addressPanel.add(secondaryAddressLabel);

      Marker marker = getMarker(location.getPoint(), i);
      _map.addOverlay(marker);

      AddressClickHandler handler = new AddressClickHandler(isDestination, location);
      mainAddressLabel.addClickListener(handler);
      marker.addMarkerClickHandler(handler);
    }
  }

  private Image getMarkerImage(int index) {
    return getAbstractMarker(index).createImage();
  }

  private Marker getMarker(LatLng location, int index) {
    Image image = getMarkerImage(index);
    MarkerOptions options = MarkerOptions.newInstance();
    Icon icon = Icon.newInstance(image.getUrl());
    icon.setIconSize(Size.newInstance(19, 35));
    icon.setIconAnchor(Point.newInstance(10, 35));
    options.setIcon(icon);
    return new Marker(location, options);
  }

  private AbstractImagePrototype getAbstractMarker(int index) {

    GreenMarkerNumberResources resources = GreenMarkerNumberResources.INSTANCE;

    index = index % 10;

    switch (index) {
      case 0:
        return resources.getMarker1();
      case 1:
        return resources.getMarker2();
      case 2:
        return resources.getMarker3();
      case 3:
        return resources.getMarker4();
      case 4:
        return resources.getMarker5();
      case 5:
        return resources.getMarker6();
      case 6:
        return resources.getMarker7();
      case 7:
        return resources.getMarker8();
      case 8:
        return resources.getMarker9();
      case 9:
        return resources.getMarker0();
      default:
        throw new IllegalStateException();
    }
  }

  private class AddressClickHandler implements ClickListener, MarkerClickHandler {

    private boolean _isDestination;

    private Placemark _location;

    public AddressClickHandler(boolean isDestination, Placemark location) {
      _isDestination = isDestination;
      _location = location;
    }

    public void onClick(Widget arg0) {
      go();
    }

    public void onClick(MarkerClickEvent event) {
      go();
    }

    private void go() {
      if (_isDestination)
        _geocoderHandler.setDestinationLocation(_location);
      else
        _geocoderHandler.setStartLocation(_location);
    }
  }
}
