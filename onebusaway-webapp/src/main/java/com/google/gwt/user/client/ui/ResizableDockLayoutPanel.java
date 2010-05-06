package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Style.Unit;

public class ResizableDockLayoutPanel extends DockLayoutPanel {

  public ResizableDockLayoutPanel(Unit unit) {
    super(unit);
  }
  
  public void setWidgetSize(Widget widget, double size) {
    LayoutData data = (LayoutData) widget.getLayoutData();
    data.size = size;
  }
}
