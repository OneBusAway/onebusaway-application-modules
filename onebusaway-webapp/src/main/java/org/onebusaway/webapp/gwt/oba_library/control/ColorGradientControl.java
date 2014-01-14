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
package org.onebusaway.webapp.gwt.oba_library.control;

import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayCssResource;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayStandardResources;

import com.google.gwt.dom.client.Style;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.ControlAnchor;
import com.google.gwt.maps.client.control.ControlPosition;
import com.google.gwt.maps.client.control.Control.CustomControl;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class ColorGradientControl extends CustomControl {

  private static OneBusAwayCssResource _css = OneBusAwayStandardResources.INSTANCE.getCss();
  
  private DivPanel _widget = new DivPanel();

  public ColorGradientControl() {
    super(new ControlPosition(ControlAnchor.BOTTOM_RIGHT, 10, 20), true, false);
  }

  @Override
  protected Widget initialize(MapWidget map) {
    _widget.setVisible(false);
    _widget.addStyleName(_css.ColorGradientControl());
    return _widget;
  }

  public void setVisible(boolean visible) {
    _widget.setVisible(visible);
  }

  public void setGradient(List<String> colors, String fromLabel, String toLabel) {
    _widget.clear();

    Grid labelGrid = new Grid(1, 2);
    labelGrid.addStyleName(_css.ColorGradientControlLabelGrid());
    labelGrid.setText(0, 0, fromLabel);
    labelGrid.setText(0, 1, toLabel);
    labelGrid.getCellFormatter().addStyleName(0, 0, _css.ColorGradientControlLabelGridLeft());
    labelGrid.getCellFormatter().addStyleName(0, 1, _css.ColorGradientControlLabelGridRight());
    _widget.add(labelGrid);

    Grid colorGrid = new Grid(1, colors.size());
    colorGrid.addStyleName(_css.ColorGradientControlColorGrid());
    for (int i = 0; i < colors.size(); i++) {
      Element element = colorGrid.getCellFormatter().getElement(0, i);
      Style style = element.getStyle();
      style.setProperty("backgroundColor", colors.get(i));
    }
    _widget.add(colorGrid);
    
    _widget.setVisible(true);
  }

  @Override
  public boolean isSelectable() {
    return false;
  }
}
