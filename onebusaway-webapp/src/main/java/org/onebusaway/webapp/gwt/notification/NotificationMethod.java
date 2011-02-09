/**
 * 
 */
package org.onebusaway.webapp.gwt.notification;

import org.onebusaway.webapp.gwt.common.widgets.DivPanel;

public interface NotificationMethod {
  
  public String getId();
  
  public boolean isEnabled();
  
  public boolean getSelectionRow(DivPanel methodPanel);
  
  public void loadFromState(NotificationMethodState methodState);

  public void handleNotification(NotificationContext context);
  
  public void handleNotificationReset();
}