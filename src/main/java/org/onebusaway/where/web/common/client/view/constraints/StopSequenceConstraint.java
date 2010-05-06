/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * 
 */
package org.onebusaway.where.web.common.client.view.constraints;

import org.onebusaway.common.web.common.client.ExceptionSupport;
import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.model.PathBean;
import org.onebusaway.common.web.common.client.resources.StopIconFactory;
import org.onebusaway.common.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.common.client.model.StopSequenceBean;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchRouteServiceException;
import org.onebusaway.where.web.common.client.view.EWhereStopFinderSearchType;
import org.onebusaway.where.web.common.client.view.StopFinderConstants;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public class StopSequenceConstraint extends AbstractConstraint {

  private StopSequenceHandler _blockHandler = new StopSequenceHandler();

  private String _route;

  private String _stopSequenceId;

  private List<StopSequenceBean> _cache = null;

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  public StopSequenceConstraint(String route) {
    _route = route;
  }

  public void update(Context context) {

    _stopFinder.setSearchText(EWhereStopFinderSearchType.ROUTE, _route);

    if (context.hasParam(StopFinderConstants.KEY_STOP_SEQUENCE_ID))
      _stopSequenceId = context.getParam(StopFinderConstants.KEY_STOP_SEQUENCE_ID);

    if (_cache != null) {
      _blockHandler.onSuccess(_cache);
    } else {
      _service.getStopSequencesByRoute(_route, _blockHandler);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopSequenceConstraint))
      return false;

    StopSequenceConstraint rc = (StopSequenceConstraint) obj;
    return _route.equals(rc._route);
  }

  @Override
  public int hashCode() {
    return _route.hashCode();
  }

  /***************************************************************************
   * Internal Classes
   **************************************************************************/

  private class StopSequenceHandler implements AsyncCallback<List<StopSequenceBean>> {

    public void onSuccess(List<StopSequenceBean> result) {

      _cache = result;

      FlowPanel routePanel = new FlowPanel();
      routePanel.addStyleName("StopFinder-RoutePanel");
      _resultsPanel.add(routePanel);

      routePanel.add(new DivWidget(_msgs.standardIndexPageRouteNumber(_route), "StopFinder-RouteNumber"));
      routePanel.add(new DivWidget(_msgs.standardIndexPageSelectADestination(), "StopFinder-RouteSelection"));

      FlowPanel routeDestinationPanel = new FlowPanel();
      routeDestinationPanel.addStyleName("StopFinder-RouteDestinationsPanel");
      routePanel.add(routeDestinationPanel);

      boolean first = true;

      for (final StopSequenceBean sequence : result) {

        DivWidget blockWidget = new DivWidget("id=" + sequence.getId() + " directionId=" + sequence.getDirectionId()
            + " tripCount=" + sequence.getTripCount());
        blockWidget.addStyleName("StopFinder-RouteDestination");

        boolean selected = _stopSequenceId == null ? first : _stopSequenceId.equals(Integer.toString(sequence.getId()));

        if (selected) {
          blockWidget.addStyleName("StopFinder-Selected");
          handlePaths(sequence.getPath());
          StopsBean sb = new StopsBean();
          sb.setStopBeans(sequence.getStops());
          _stopsHandler.onSuccess(sb);
        }

        blockWidget.addClickListener(new ClickListener() {
          public void onClick(Widget arg0) {
            _stopFinder.queryStopSequence(_route, sequence.getId());
          }
        });

        routeDestinationPanel.add(blockWidget);
        first = false;
      }
    }

    public void onFailure(Throwable ex) {
      if (ex instanceof NoSuchRouteServiceException) {
        _resultsPanel.add(new DivWidget(_msgs.commonNoSuchRoute(), "StopFinder-NoSuchRouteError"));
      } else {
        ExceptionSupport.handleException(ex);
      }
    }

    private void handlePaths(PathBean path) {

      LatLngBounds bounds = LatLngBounds.newInstance();

      double[] lat = path.getLat();
      double[] lon = path.getLon();

      if (lat.length == 0)
        return;

      LatLng[] points = new LatLng[lat.length];

      for (int i = 0; i < lat.length; i++) {
        points[i] = LatLng.newInstance(lat[i], lon[i]);
        bounds.extend(points[i]);
      }

      _map.addOverlay(new Polyline(points, "#4F64BA", 3, 1.0));

      MarkerOptions startOpts = MarkerOptions.newInstance();
      startOpts.setIcon(StopIconFactory.getRouteStartIcon());
      startOpts.setClickable(false);
      LatLng first = points[0];
      _map.addOverlay(new Marker(first, startOpts));

      MarkerOptions endOpts = MarkerOptions.newInstance();
      endOpts.setIcon(StopIconFactory.getRouteEndIcon());
      endOpts.setClickable(false);
      LatLng last = points[points.length - 1];
      _map.addOverlay(new Marker(last, endOpts));

      int zoom = _map.getBoundsZoomLevel(bounds);
      _map.setCenter(bounds.getCenter(), zoom);
    }
  }

}