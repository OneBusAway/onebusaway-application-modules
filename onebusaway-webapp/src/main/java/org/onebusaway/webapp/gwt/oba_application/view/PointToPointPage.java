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

import org.onebusaway.webapp.gwt.common.AbstractPageSource;
import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PointToPointPage extends AbstractPageSource {

  private static final String PARAM_TO = "to";
  private static final String PARAM_FROM = "from";
  private Geocoder _geocoder = new Geocoder();

  private LatLng _fromPoint;

  private LatLng _toPoint;

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public PointToPointPage() {

  }

  public Widget create(final Context context) throws PageException {

    FlowPanel panel = new FlowPanel();

    FlowPanel fromPanel = new FlowPanel();
    panel.add(fromPanel);

    fromPanel.add(new SpanWidget("From:"));
    TextBox fromBox = new TextBox();
    fromPanel.add(fromBox);

    FlowPanel toPanel = new FlowPanel();
    panel.add(toPanel);

    toPanel.add(new SpanWidget("To:"));
    TextBox toBox = new TextBox();
    toPanel.add(toBox);

    update(context);

    // return view;
    return panel;
  }

  @Override
  public Widget update(Context context) throws PageException {

    if (context.hasParam(PARAM_FROM) && context.hasParam(PARAM_TO)) {
      String from = context.getParam(PARAM_FROM);
      String to = context.getParam(PARAM_TO);
      _geocoder.getLocations(from, new GeocoderHandler(true));
      _geocoder.getLocations(to, new GeocoderHandler(false));
    }

    return null;
  }

  private void handleResults() {
    if (_fromPoint == null || _toPoint == null)
      return;
  }

  private class GeocoderHandler implements LocationCallback {

    private boolean _from;

    public GeocoderHandler(boolean from) {
      _from = from;
    }

    public void onSuccess(JsArray<Placemark> locations) {
      if (locations.length() == 1) {
        Placemark mark = locations.get(0);
        LatLng point = mark.getPoint();
        if (_from) {
          _fromPoint = point;
        } else {
          _toPoint = point;
        }
        handleResults();
      } else {
        System.err.println("geocoder: " + locations.length());
      }

    }

    public void onFailure(int statusCode) {
      System.err.println("error on geocoding");
    }
  }
}
