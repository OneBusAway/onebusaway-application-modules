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