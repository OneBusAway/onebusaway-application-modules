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
package org.onebusaway.oba.web.standard.client.pages;


import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import org.onebusaway.where.web.common.client.AbstractPageSource;
import org.onebusaway.where.web.common.client.Context;
import org.onebusaway.where.web.common.client.PageException;
import org.onebusaway.where.web.common.client.widgets.DivWidget;

public class IndexPage extends AbstractPageSource {

  public static final int TOP_PANEL_HEIGHT = 90;

  public static final int RESULT_PANEL_WIDTH = 300;

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private Geocoder _geocoder = new Geocoder();

  private MapWidget _map;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public IndexPage() {
    LatLngBounds view = LatLngBounds.newInstance();
    view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
    view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));
    _geocoder.setViewport(view);
  }

  public Widget create(final Context context) throws PageException {

    FlowPanel panel = new FlowPanel();

    HorizontalPanel hp = new HorizontalPanel();
    panel.add(hp);

    FlowPanel leftPanel = new FlowPanel();
    hp.add(leftPanel);
    hp.setCellWidth(leftPanel, RESULT_PANEL_WIDTH + "px");

    leftPanel.add(new DivWidget("Start Address:"));
    final TextBox textBox = new TextBox();
    leftPanel.add(textBox);

    Button button = new Button("Go");
    button.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        String text = textBox.getText();
        if (text == null || text.length() == 0)
          return;
        _geocoder.getLocations(text, new GeocoderResultHandler());
      }
    });
    leftPanel.add(button);

    _map = new MapWidget(_center, _zoom);
    _map.setSize("500px", "500px");
    _map.addStyleName("map");
    _map.addControl(new LargeMapControl());

    hp.add(_map);
    hp.setCellHeight(_map, "100%");

    Window.setTitle("One Bus Away");

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

  private void handleSearch(String text) {

  }

  @Override
  public Widget update(Context context) throws PageException {

    // Reset the visible state
    _map.clearOverlays();

    return null;
  }

  private class GeocoderResultHandler implements LocationCallback {

    public void onSuccess(JsArray<Placemark> placemarks) {

      _map.clearOverlays();
      for (int i = 0; i < placemarks.length(); i++) {

        final Placemark mark = placemarks.get(i);

        final Marker marker = new Marker(mark.getPoint());
        _map.addOverlay(marker);
      }

      if (placemarks.length() == 1) {

      } else if (placemarks.length() > 1) {

      } else {
        System.err.println("  no results");
      }
    }

    public void onFailure(int statusCode) {
      System.err.println("yeah...");
    }

  }

  private class ResizeHandler implements WindowResizeListener {
    public void onWindowResized(int width, int height) {
      _map.setHeight((height - _map.getAbsoluteTop() - 20) + "px");
      _map.setWidth((width - _map.getAbsoluteLeft() - 15) + "px");
    }
  }
}
