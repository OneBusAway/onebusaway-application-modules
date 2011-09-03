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

import org.onebusaway.transit_data.model.StopBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class StopWidget extends Composite {

  interface MyUiBinder extends UiBinder<Widget, StopWidget> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  SpanElement label;

  @UiField
  Anchor removeStopAnchor;

  private StopBean _stop;

  private RemoveClickedHandler _handler;

  public StopWidget(StopBean stop, RemoveClickedHandler handler) {
    _stop = stop;
    _handler = handler;

    initWidget(uiBinder.createAndBindUi(this));
    String title = stop.getName();
    if( stop.getCode() != null)
      title += " - Stop # " + stop.getCode();
    if( stop.getDirection() != null)
      title += " - " + stop.getDirection() + " bound";
    label.setInnerText(title);
    removeStopAnchor.setHref("#removeStop");
  }

  @UiHandler("removeStopAnchor")
  void handleRemoveStopClick(ClickEvent e) {
    e.preventDefault();
    if (_handler != null)
      _handler.handleRemoveClicked(this, _stop);
  }

  public interface RemoveClickedHandler {
    public void handleRemoveClicked(StopWidget widget, StopBean stop);
  }
}
