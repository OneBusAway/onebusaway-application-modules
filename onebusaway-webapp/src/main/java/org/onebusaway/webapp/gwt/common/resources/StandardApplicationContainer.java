package org.onebusaway.webapp.gwt.common.resources;

import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class StandardApplicationContainer {
  public static void add(Widget widget) {

    RootLayoutPanel panel = RootLayoutPanel.get();
    panel.add(widget);
    panel.setWidgetTopBottom(widget, 43, Unit.PX, 0, Unit.PX);
    
    StyleInjector.inject(CommonResources.INSTANCE.getApplicationCss().getText());
  }
}
