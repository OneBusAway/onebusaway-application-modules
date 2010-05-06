package org.onebusaway.oba.web.standard.client.view;

import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.common.web.common.client.resources.CommonResources;
import org.onebusaway.oba.web.standard.client.control.StateEvent;
import org.onebusaway.oba.web.standard.client.control.StateEventListener;
import org.onebusaway.oba.web.standard.client.control.state.SearchLocationUpdatedState;
import org.onebusaway.oba.web.standard.client.control.state.State;
import org.onebusaway.oba.web.standard.client.model.QueryModel;

import com.google.gwt.libideas.resources.client.DataResource;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;

public class SearchOriginMapPresenter implements StateEventListener {

  private QueryModel _queryModel;

  private MapOverlayManager _mapOverlayManager;

  private Marker _marker;

  public void setQueryModel(QueryModel queryModel) {
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

      CommonResources resources = CommonResources.INSTANCE;
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
