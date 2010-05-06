package org.onebusaway.webapp.gwt.oba_library.control;

import org.onebusaway.webapp.gwt.common.widgets.DivPanel;

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

  private DivPanel _widget = new DivPanel();

  public ColorGradientControl() {
    super(new ControlPosition(ControlAnchor.BOTTOM_RIGHT, 10, 20), true, false);
  }

  @Override
  protected Widget initialize(MapWidget map) {
    _widget.setVisible(false);
    _widget.addStyleName("ColorGradientControl");
    return _widget;
  }

  public void setVisible(boolean visible) {
    _widget.setVisible(visible);
  }

  public void setGradient(List<String> colors, String fromLabel, String toLabel) {
    _widget.clear();

    Grid labelGrid = new Grid(1, 2);
    labelGrid.addStyleName("ColorGradientControl-LabelGrid");
    labelGrid.setText(0, 0, fromLabel);
    labelGrid.setText(0, 1, toLabel);
    labelGrid.getCellFormatter().addStyleName(0, 0, "ColorGradientControl-LabelGrid-Left");
    labelGrid.getCellFormatter().addStyleName(0, 1, "ColorGradientControl-LabelGrid-Right");
    _widget.add(labelGrid);

    Grid colorGrid = new Grid(1, colors.size());
    colorGrid.addStyleName("ColorGradientControl-ColorGrid");
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
