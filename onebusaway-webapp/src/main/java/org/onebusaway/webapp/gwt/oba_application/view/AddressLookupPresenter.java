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

import java.util.List;

import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;
import org.onebusaway.webapp.gwt.common.widgets.PlacePresenter;
import org.onebusaway.webapp.gwt.oba_application.control.CommonControl;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.AddressLookupErrorState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayCssResource;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayStandardResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddressLookupPresenter {

  private static OneBusAwayCssResource _css = OneBusAwayStandardResources.INSTANCE.getCss();

  private FlowPanel _widget;

  private CommonControl _control;

  public void setControl(CommonControl control) {
    _control = control;
  }

  public Widget getWidget() {
    if (_widget == null)
      initialize();
    return _widget;

  }

  public StateEventListener getStateEventListener() {
    return new StateEventHandler();
  }

  /*****************************************************************************
   *  
   ****************************************************************************/

  private void initialize() {
    _widget = new FlowPanel();
    _widget.addStyleName(_css.AddressLookup());
    _widget.setVisible(false);
  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent event) {
      State state = event.getState();

      if (state instanceof AddressLookupErrorState) {

        AddressLookupErrorState ales = (AddressLookupErrorState) state;
        List<Place> places = ales.getLocations();

        _widget.clear();
        _widget.setVisible(true);

        if (places.size() == 0) {
          _widget.add(new DivWidget(
              "We couldn't find that adress.  Check to make sure it's correct, or perhaps add the city or zip code?",
              _css.AddressLookupNoAddressFound()));
        } else {
          _widget.add(new DivWidget("Did you mean:",
              _css.AddressLookupMultipleAddressesFound()));
          for (final Place placemark : places) {

            DivPanel panel = new DivPanel();
            panel.addStyleName(_css.AddressLookupPlacePanel());
            _widget.add(panel);

            ClickHandler handler = new ClickHandler() {
              public void onClick(ClickEvent arg0) {
                _control.setQueryLocation(placemark.getLocation());
              }
            };

            PlacePresenter p = new PlacePresenter();
            DivPanel placePanel = p.getPlaceAsPanel(placemark, handler);
            placePanel.addStyleName(_css.AddressLookupPlace());
            panel.add(placePanel);
          }
        }
      } else {
        _widget.setVisible(false);
        _widget.clear();
      }
    }
  }
}
