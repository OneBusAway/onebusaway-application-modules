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
package org.onebusaway.webapp.gwt.mobile_application.view;

import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationCssResource;
import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.user.client.ui.Widget;

public class LargeButtonMapControl extends CustomControl {

  private static final MobileApplicationCssResource _css = MobileApplicationResources.INSTANCE.getCSS();

  private enum EMapOperation {
    LEFT, RIGHT, UP, DOWN, ZOOM_IN, ZOOM_OUT
  }

  private DivPanel _widget = new DivPanel();

  private MapWidget _map;

  public LargeButtonMapControl() {
    super(new ControlPosition(ControlAnchor.TOP_LEFT, 5, 5), true, true);
  }

  @Override
  protected Widget initialize(MapWidget map) {
    _map = map;
    _widget.setVisible(true);
    _widget.add(getMapControlWidget(EMapOperation.UP));    
    _widget.add(getMapControlWidget(EMapOperation.LEFT));
    _widget.add(getMapControlWidget(EMapOperation.RIGHT));
    _widget.add(getMapControlWidget(EMapOperation.DOWN));
    _widget.add(getMapControlWidget(EMapOperation.ZOOM_IN));
    _widget.add(getMapControlWidget(EMapOperation.ZOOM_OUT));
    return _widget;
  }

  public void setVisible(boolean visible) {
    _widget.setVisible(visible);
  }

  @Override
  public boolean isSelectable() {
    return true;
  }
  
  private Widget getMapControlWidget(EMapOperation operation) {
    DivWidget widget = new DivWidget("",getStyleNameForMapOperation(operation),_css.MapControlAny());
    widget.addClickHandler(new MapOperationClickHandler(operation));
    return widget;
  }

  private String getStyleNameForMapOperation(EMapOperation operation) {
    switch (operation) {
      case UP:
        return _css.MapControlUp();
      case DOWN:
        return _css.MapControlDown();
      case LEFT:
        return _css.MapControlLeft();
      case RIGHT:
        return _css.MapControlRight();
      case ZOOM_IN:
        return _css.MapControlZoomIn();
      case ZOOM_OUT:
        return _css.MapControlZoomOut();
      default:
        throw new IllegalStateException();
    }
  }

  private class MapOperationClickHandler implements ClickHandler {

    private EMapOperation _operation;

    public MapOperationClickHandler(EMapOperation operation) {
      _operation = operation;
    }

    @Override
    public void onClick(ClickEvent arg0) {
      switch (_operation) {
        case UP:
          _map.panDirection(0, 1);
          break;
        case DOWN:
          _map.panDirection(0, -1);
          break;
        case LEFT:
          _map.panDirection(1, 0);
          break;
        case RIGHT:
          _map.panDirection(-1, 0);
          break;
        case ZOOM_IN:
          _map.zoomIn();
          break;
        case ZOOM_OUT:
          _map.zoomOut();
          break;
      }
    }
  }
}
