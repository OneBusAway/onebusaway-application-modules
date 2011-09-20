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
package org.onebusaway.webapp.gwt.common.widgets;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

public class MapWidgetMaxSizeResizeHandler implements ResizeHandler {

  private Widget _parent;

  private Widget _mapPanel;

  private MapWidget _map;

  public static Widget registerHandler(Widget parent, MapWidget map) {

    DivPanel mapPanel = new DivPanel();
    mapPanel.setSize("100%", "100%");
    mapPanel.add(map);

    map.setSize("100%", "100%");

    final MapWidgetMaxSizeResizeHandler handler = new MapWidgetMaxSizeResizeHandler(
        parent, mapPanel, map);
    DeferredCommand.addCommand(new Command() {

      public void execute() {
        ResizeEvent event = new ResizeEventImpl(Window.getClientWidth(),
            Window.getClientHeight());
        handler.onResize(event);
        Window.addResizeHandler(handler);
      }
    });

    return mapPanel;
  }

  private MapWidgetMaxSizeResizeHandler(Widget parent, Widget mapPanel,
      MapWidget map) {
    _parent = parent;
    _mapPanel = mapPanel;
    _map = map;
  }

  public void onResize(ResizeEvent arg0) {

    System.out.println("GO! w=" + _parent.getOffsetWidth() + " h="
        + _parent.getOffsetHeight());

    _mapPanel.setWidth(_parent.getOffsetWidth() + "px");
    _mapPanel.setHeight(_parent.getOffsetHeight() + "px");

    _map.checkResizeAndCenter();
  }

  private static class ResizeEventImpl extends ResizeEvent {

    protected ResizeEventImpl(int width, int height) {
      super(width, height);
    }
  }
}
