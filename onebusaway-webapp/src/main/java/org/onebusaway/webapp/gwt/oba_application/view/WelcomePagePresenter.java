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
package org.onebusaway.webapp.gwt.oba_application.view;

import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class WelcomePagePresenter {
  private FlowPanel _panel = new FlowPanel();

  public WelcomePagePresenter() {
    _panel.add(new DivWidget(
        "<p>We want to take the hassle out of finding places to eat, see, and go in your neighborhood using mass-transit.</p><p>We've written a tool that makes it easy to search for restaurants, parks, shops, or anything else you might be looking for that are just one bus ride away.</p>"));
  }

  public StateEventListener getStateEventListener() {
    return new StateEventHandler();
  }

  public Widget getWidget() {
    return _panel;
  }

  private class StateEventHandler implements StateEventListener {
    public void handleUpdate(StateEvent model) {
      _panel.setVisible(false);
    }
  }
}
