/**
 * 
 */
package org.onebusaway.webapp.gwt.notification;


import com.google.gwt.user.client.Window;

public class PopupNotificationMethod extends AbstractNotificationMethod {

  public PopupNotificationMethod() {
    super("popup","Popup an alert");
  }

  @Override
  protected void performNotification(NotificationContext context) {
    Window.alert("Go catch that bus!");
    context.reset();
  }
}