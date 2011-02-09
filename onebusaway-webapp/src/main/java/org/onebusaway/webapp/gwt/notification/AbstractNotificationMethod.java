/**
 * 
 */
package org.onebusaway.webapp.gwt.notification;

import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;

abstract class AbstractNotificationMethod implements NotificationMethod {

  protected String _id;

  protected String _methodDescription;

  private boolean _enabled = true;

  private CheckBox _checkBox;

  public AbstractNotificationMethod(String id, String methodDescription) {
    _id = id;
    _methodDescription = methodDescription;
  }

  public String getId() {
    return _id;
  }

  public boolean isEnabled() {
    return _enabled;
  }

  public boolean getSelectionRow(DivPanel methodRow) {

    _checkBox = new CheckBox();
    _checkBox.setValue(_enabled);
    _checkBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent arg0) {
        _enabled = _checkBox.getValue();
      }
    });
    methodRow.add(_checkBox);
    addMethodDescriptionToRow(methodRow);

    return true;
  }

  public void loadFromState(NotificationMethodState methodState) {
    _enabled = methodState.isEnabled();
    if(_checkBox != null)
      _checkBox.setValue(_enabled);
  }

  public void handleNotification(NotificationContext context) {
    if (_enabled)
      performNotification(context);
  }

  public void handleNotificationReset() {

  }

  protected void addMethodDescriptionToRow(DivPanel methodRow) {
    methodRow.add(new SpanWidget(_methodDescription));
  }

  protected abstract void performNotification(NotificationContext context);
}