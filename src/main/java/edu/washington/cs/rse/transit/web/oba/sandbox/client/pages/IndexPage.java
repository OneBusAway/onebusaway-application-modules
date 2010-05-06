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
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.DivWidget;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.SpanWidget;

import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexPage extends AbstractPageSource {

  private static LatLng _center = LatLng.newInstance(47.601533, -122.32933);

  private static int _zoom = 11;

  /*****************************************************************************
   * Private Members
   ****************************************************************************/

  private MapWidget _map;

  private HashMap<Integer, InfoWindowContent> _stopPanels = new HashMap<Integer, InfoWindowContent>();

  private HashMap<Integer, LatLng> _stopToLocation = new HashMap<Integer, LatLng>();

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

    final TextArea text = new TextArea();
    text.setCharacterWidth(100);
    text.setVisibleLines(10);
    text.setText(Resources.INSTANCE.getData().getText());
    panel.add(text);

    Button button = new Button("Go");
    button.addClickListener(new ClickListener() {

      public void onClick(Widget arg0) {

        _map.clearOverlays();
        _stopToLocation.clear();
        _stopPanels.clear();

        final Map<Integer, Set<Transfer>> outbound = new HashMap<Integer, Set<Transfer>>();
        final Map<Integer, Set<Transfer>> inbound = new HashMap<Integer, Set<Transfer>>();

        LatLngBounds bounds = LatLngBounds.newInstance();

        String content = text.getText();
        for (String line : content.split("\n")) {
          String[] tokens = line.split(",");

          int stopFrom = Integer.parseInt(tokens[0]);
          double latFrom = Double.parseDouble(tokens[1]);
          double lonFrom = Double.parseDouble(tokens[2]);
          LatLng pFrom = LatLng.newInstance(latFrom, lonFrom);
          _stopToLocation.put(stopFrom, pFrom);
          bounds.extend(pFrom);

          int stopTo = Integer.parseInt(tokens[3]);
          double latTo = Double.parseDouble(tokens[4]);
          double lonTo = Double.parseDouble(tokens[5]);
          LatLng pTo = LatLng.newInstance(latTo, lonTo);
          _stopToLocation.put(stopTo, pTo);
          bounds.extend(pTo);

          Polyline pline = new Polyline(new LatLng[] {pFrom, pTo});
          _map.addOverlay(pline);

          Transfer transfer = new Transfer(stopFrom, stopTo, tokens[6]);

          getTransfers(outbound, stopFrom).add(transfer);
          getTransfers(inbound, stopTo).add(transfer);
        }

        int zoom = _map.getBoundsZoomLevel(bounds);
        _map.setCenter(bounds.getCenter(), zoom);

        _stopPanels = new HashMap<Integer, InfoWindowContent>();

        for (final int id : _stopToLocation.keySet()) {

          FlowPanel panel = new FlowPanel();
          panel.add(new DivWidget("Stop # " + id));

          Set<Transfer> out = outbound.get(id);
          if (out != null) {
            FlowPanel bound = new FlowPanel();
            bound.addStyleName("bound");
            bound.add(new DivWidget("Outbound:"));
            for (Transfer transfer : out) {
              FlowPanel transferPanel = new FlowPanel();
              transferPanel.add(new SpanWidget(transfer.getTransfers() + " to "));
              SpanWidget w = new SpanWidget(Integer.toString(transfer.getTo()));
              w.addClickListener(new StopClickHandler(transfer.getTo()));
              w.addStyleName("link");
              transferPanel.add(w);
              bound.add(transferPanel);
            }
            panel.add(bound);
          }

          Set<Transfer> in = inbound.get(id);
          if (in != null) {
            FlowPanel bound = new FlowPanel();
            bound.addStyleName("bound");
            bound.add(new DivWidget("Inbound:"));
            for (Transfer transfer : in) {
              FlowPanel transferPanel = new FlowPanel();
              transferPanel.add(new SpanWidget(transfer.getTransfers()
                  + " from "));
              SpanWidget w = new SpanWidget(
                  Integer.toString(transfer.getFrom()));
              w.addClickListener(new StopClickHandler(transfer.getFrom()));
              w.addStyleName("link");
              transferPanel.add(w);
              bound.add(transferPanel);
            }
            panel.add(bound);
          }

          InfoWindowContent infoWindowContent = new InfoWindowContent(panel);
          _stopPanels.put(id, infoWindowContent);

          final LatLng p = _stopToLocation.get(id);
          Marker m = new Marker(p);
          m.addMarkerClickHandler(new StopClickHandler(id));
          _map.addOverlay(m);
        }
      }

    });
    panel.add(button);

    return panel;
  }

  private Set<Transfer> getTransfers(
      Map<Integer, Set<Transfer>> transfersByStop, int stop) {

    Set<Transfer> transfers = transfersByStop.get(stop);

    if (transfers == null) {
      transfers = new HashSet<Transfer>();
      transfersByStop.put(stop, transfers);
    }

    return transfers;
  }

  private class StopClickHandler implements ClickListener, MarkerClickHandler {

    private int _stop;

    public StopClickHandler(int stop) {
      _stop = stop;
    }

    public void onClick(Widget window) {
      openWindow();
    }

    public void onClick(MarkerClickEvent event) {
      openWindow();
    }

    private void openWindow() {
      LatLng p = _stopToLocation.get(_stop);
      InfoWindowContent content = _stopPanels.get(_stop);
      InfoWindow w = _map.getInfoWindow();
      w.open(p, content);
    }

  }

  private static class Transfer {
    private int _from;
    private int _to;
    private String _transfers;

    public Transfer(int from, int to, String transfers) {
      _from = from;
      _to = to;
      _transfers = transfers;
    }

    public int getFrom() {
      return _from;
    }

    public int getTo() {
      return _to;
    }

    public String getTransfers() {
      return _transfers;
    }
  }
}
