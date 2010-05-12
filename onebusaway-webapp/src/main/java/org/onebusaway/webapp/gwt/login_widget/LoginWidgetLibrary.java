package org.onebusaway.webapp.gwt.login_widget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootPanel;

public class LoginWidgetLibrary implements EntryPoint {

  @Override
  public void onModuleLoad() {
    
    String path = Location.getPath();
    String prefix = path.contains("/onebusaway-webapp") ? "/onebusaway-webapp" : "";
    
    RootPanel panel = RootPanel.get("login_widget");
    panel.add(new LoginWidget(prefix + "/services/login",
        prefix + "/login-handler.action"));
  }
}
