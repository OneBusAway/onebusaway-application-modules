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
package org.onebusaway.webapp.gwt.stop_and_route_selection;

import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.transit_data.model.RouteBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RouteWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, RouteWidget> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  CheckBox selection;

  @UiField
  SpanElement label;

  private RouteBean _route;

  private RouteSelectionHandler _handler;

  public RouteWidget(RouteBean route, boolean selected,
      RouteSelectionHandler handler) {
    _route = route;
    _handler = handler;
    initWidget(uiBinder.createAndBindUi(this));
    selection.setValue(selected);
    label.setInnerText(RoutePresenter.getNameForRoute(route) + " - "
        + RoutePresenter.getDescriptionForRoute(route));
  }

  public boolean isSelected() {
    return selection.getValue();
  }

  @UiHandler("selection")
  void handleClick(ClickEvent e) {
    if (_handler != null)
      _handler.handleSelectionChanged(this, _route, selection.getValue());
  }

  public interface RouteSelectionHandler {
    public void handleSelectionChanged(RouteWidget widget, RouteBean route,
        boolean selected);
  }
}
