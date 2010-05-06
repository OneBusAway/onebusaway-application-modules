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
package org.onebusaway.tripplanner.web.visualizer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.libideas.client.StyleInjector;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripPlannerIntermediateOutputVisualizer implements EntryPoint {
  
  private static DateTimeFormat _format = DateTimeFormat.getFormat("yyyy-MM-dd-HH:mm:ss");

  private FlowPanel _leftPanel;

  private MapWidget _map;

  private List<State> _states = new ArrayList<State>();

  public void onModuleLoad() {

    RootPanel root = RootPanel.get("root");
    HorizontalPanel hp = new HorizontalPanel();
    hp.addStyleName("HorizontalPanel");
    root.add(hp);

    _leftPanel = new FlowPanel();
    _leftPanel.addStyleName("LeftPanel");
    hp.add(_leftPanel);

    final TextArea area = new TextArea();
    area.addStyleName("InputArea");
    _leftPanel.add(area);

    Button button = new Button("Go");
    _leftPanel.add(button);
    button.addClickListener(new ClickListener() {
      public void onClick(Widget arg0) {
        String text = area.getText();
        go(text);
      }
    });

    FlowPanel rightPanel = new FlowPanel();
    rightPanel.addStyleName("RightPanel");
    hp.add(rightPanel);

    _map = new MapWidget(LatLng.newInstance(47.601533, -122.32933), 11);
    _map.addControl(new LargeMapControl());
    rightPanel.add(_map);

    StyleInjector.injectStylesheet(Resources.INSTANCE.getCSS().getText());

    Window.addWindowResizeListener(new WindowResizeHandler());
  }

  private void go(String text) {

    String[] lines = text.split("\n");

    for (String line : lines) {

      if (line.length() == 0)
        continue;

      Map<String, String> keyValuePairs = new HashMap<String, String>();

      for (String token : line.split("\\s+")) {
        String[] kvp = token.split("=");
        if (kvp.length != 2)
          throw new IllegalStateException("invalid line: line=" + line + " token=" + token);
        String key = kvp[0];
        String value = kvp[1];
        keyValuePairs.put(key, value);
      }

      State state = new State();
      state.type = keyValuePairs.get("state");
      state.x = Double.parseDouble(keyValuePairs.get("x"));
      state.y = Double.parseDouble(keyValuePairs.get("y"));
      state.lat = Double.parseDouble(keyValuePairs.get("lat"));
      state.lon = Double.parseDouble(keyValuePairs.get("lon"));
      state.setTime(_format.parse(keyValuePairs.get("time")).getTime());
      state.values = keyValuePairs;

      _states.add(state);
    }

    _leftPanel.clear();

    ListBox box = new ListBox();
    box.setVisibleItemCount(30);

    for (State state : _states) {
      String label = state.getLabel();
      System.out.println(label);
      box.addItem(label);
    }

    _leftPanel.add(box);

    box.addChangeListener(new ChangeHandler());
  }

  private class WindowResizeHandler implements WindowResizeListener {

    public void onWindowResized(int x, int y) {
      int mapX = _map.getAbsoluteLeft();
      int mapY = _map.getAbsoluteTop();
      _map.setWidth((x - mapX) + "px");
      _map.setHeight((y - mapY) + "px");
    }
  }

  private class ChangeHandler implements ChangeListener {

    public void onChange(Widget widget) {
      ListBox box = (ListBox) widget;
      int index = box.getSelectedIndex();
      if (0 <= index && index < _states.size()) {
        State state = _states.get(index);
        _map.clearOverlays();
        LatLng location = LatLng.newInstance(state.lat, state.lon);
        Marker m = new Marker(location);
        _map.addOverlay(m);
        _map.setCenter(location);
      }
    }

  }
}
