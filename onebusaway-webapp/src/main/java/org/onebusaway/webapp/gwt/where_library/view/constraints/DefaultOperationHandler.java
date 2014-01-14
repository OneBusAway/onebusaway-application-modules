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
package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.gwt.where_library.OBAConfig;
import org.onebusaway.webapp.gwt.where_library.UserContext;
import org.onebusaway.webapp.gwt.where_library.view.stops.TransitMapManager;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;

public class DefaultOperationHandler implements OperationHandler {

  @Override
  public void handleOperation(OperationContext context) {

    TransitMapManager manager = context.getTransitMapManager();
    manager.showStopsInCurrentView();

    Panel panel = context.getPanel();
    DefaultOperationWidget widget = new DefaultOperationWidget();
    panel.add(widget);

    final UserContext userContext = UserContext.getContext();
    userContext.getCurrentUser(new UserHandler(context, widget));
  }

  private class UserHandler implements AsyncCallback<UserBean> {

    private OperationContext _context;

    private DefaultOperationWidget _widget;

    public UserHandler(OperationContext context, DefaultOperationWidget widget) {
      _context = context;
      _widget = widget;
    }

    @Override
    public void onSuccess(UserBean user) {
      onSuccess(user, true);
    }

    public void onSuccess(UserBean user, boolean askForDefaultSearchLocation) {

      OBAConfig config = OBAConfig.getConfig();

      boolean noDefaultServiceAera = !config.hasDefaultServiceArea();
      boolean noUserLocation = user == null || !user.hasDefaultLocation()
          || user.getDefaultLocationName() == null;
      boolean ask = user != null && user.isRememberPreferencesEnabled()
          && askForDefaultSearchLocation;

      if (noDefaultServiceAera && noUserLocation && ask) {

        final SetInitialSearchLocationPopupPanel popup = new SetInitialSearchLocationPopupPanel();
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {

          @Override
          public void onClose(CloseEvent<PopupPanel> arg0) {
            UserBean updatedUser = popup.getUser();
            if (updatedUser != null)
              onSuccess(updatedUser, false);
          }
        });

        popup.show();

      } else if (!noUserLocation) {

        if (!_context.isLocationSet()) {
          LatLng center = LatLng.newInstance(user.getDefaultLocationLat(),
              user.getDefaultLocationLon());
          MapWidget map = _context.getMap();
          map.setCenter(center, 16);
        }

        _widget.addDefaultSearchLocationLink(user);
      }
    }

    @Override
    public void onFailure(Throwable err) {

    }
  }

}
