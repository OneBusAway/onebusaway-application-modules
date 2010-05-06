package org.onebusaway.webapp.gwt.login_widget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class LoginWidgetLibrary implements EntryPoint {

  @Override
  public void onModuleLoad() {
    
    RootPanel panel = RootPanel.get("login_widget");
    panel.add(new LoginWidget("/onebusaway-webapp/services/login",
        "/onebusaway-webapp/login-handler.action"));
  }
}
