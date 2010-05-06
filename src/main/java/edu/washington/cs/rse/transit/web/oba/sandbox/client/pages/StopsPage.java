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
package edu.washington.cs.rse.transit.web.oba.sandbox.client.pages;

import edu.washington.cs.rse.transit.web.oba.common.client.AbstractPageSource;
import edu.washington.cs.rse.transit.web.oba.common.client.Context;
import edu.washington.cs.rse.transit.web.oba.common.client.PageException;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithRoutesBean;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.DivWidget;

import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class StopsPage extends AbstractPageSource {

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private MapWidget _map;

  private StopHandler _handler = new StopHandler();

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public Widget create(final Context context) throws PageException {

    FlowPanel panel = new FlowPanel();

    _map = new MapWidget(_center, _zoom);
    _map.setSize("100%", "500px");
    _map.addStyleName("map");
    _map.addControl(new LargeMapControl());
    panel.add(_map);

    final TextBox text = new TextBox();
    panel.add(text);

    Button button = new Button("Go");
    button.addClickListener(new ClickListener() {

      public void onClick(Widget arg0) {
        String stopId = text.getText();
        _service.getStop(stopId, _handler);
      }
    });
    panel.add(button);

    return panel;
  }

  private class StopHandler implements AsyncCallback<StopWithRoutesBean> {

    public void onSuccess(StopWithRoutesBean bean) {
      final StopBean stop = bean.getStop();
      final LatLng p = LatLng.newInstance(stop.getLat(), stop.getLon());
      Marker marker = new Marker(p);
      marker.addMarkerClickHandler(new MarkerClickHandler() {
        public void onClick(MarkerClickEvent event) {
          FlowPanel panel = new FlowPanel();
          panel.add(new DivWidget("Stop # " + stop.getId()));
          InfoWindow w = _map.getInfoWindow();
          w.open(p, new InfoWindowContent(panel));
        }
      });
      _map.addOverlay(marker);

    }

    public void onFailure(Throwable ex) {
      handleException(ex);
    }

  }
}
