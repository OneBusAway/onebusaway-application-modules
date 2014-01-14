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

import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.resources.map.MapResources;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.StateEventListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchLocationUpdatedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;
import org.onebusaway.webapp.gwt.oba_application.model.LocationQueryModel;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.resources.client.DataResource;

public class SearchOriginMapPresenter implements StateEventListener {

  private LocationQueryModel _queryModel;

  private MapOverlayManager _mapOverlayManager;

  private Marker _marker;

  public void setLocationQueryModel(LocationQueryModel queryModel) {
    _queryModel = queryModel;
  }

  public void setMapOverlayManager(MapOverlayManager manager) {
    _mapOverlayManager = manager;
  }

  public void handleUpdate(StateEvent event) {

    State state = event.getState();

    if (state instanceof SearchLocationUpdatedState) {

      if (_marker != null) {
        _mapOverlayManager.removeOverlay(_marker);
        _marker = null;
      }

      LatLng location = _queryModel.getLocation();

      if (location == null) {
        System.err.println("PROBLEM!");
        return;
      }

      MapResources resources = MapResources.INSTANCE;
      DataResource resource = resources.getImageRouteStart();
      Icon icon = Icon.newInstance();
      icon.setImageURL(resource.getUrl());
      icon.setIconSize(Size.newInstance(20, 34));
      icon.setIconAnchor(Point.newInstance(10, 34));
      MarkerOptions opts = MarkerOptions.newInstance(icon);

      _marker = new Marker(location, opts);
      _mapOverlayManager.addOverlay(_marker);
    }

  }
}
