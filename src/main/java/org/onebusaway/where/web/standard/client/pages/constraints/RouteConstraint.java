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
package org.onebusaway.where.web.standard.client.pages.constraints;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import org.onebusaway.where.web.common.client.Context;
import org.onebusaway.where.web.common.client.model.PathBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopsBean;
import org.onebusaway.where.web.common.client.rpc.NoSuchRouteServiceException;
import org.onebusaway.where.web.common.client.widgets.DivWidget;
import org.onebusaway.where.web.common.client.widgets.SpanWidget;
import org.onebusaway.where.web.standard.client.pages.ESearchType;
import org.onebusaway.where.web.standard.client.pages.StopIconFactory;

import java.util.List;

public class RouteConstraint extends AbstractConstraint {

  private StopSequenceBlockHandler _blockHandler = new StopSequenceBlockHandler();

  private String _route;

  private String _blockId = null;

  private List<StopSequenceBlockBean> _blocksCache = null;

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  public RouteConstraint(String route) {
    _route = route;
  }

  public void update(Context context) {

    _wrapper.setSearchText(ESearchType.ROUTE, _route);

    if (context.hasParam(KEY_BLOCK_ID))
      _blockId = context.getParam(KEY_BLOCK_ID);

    if (_blocksCache != null) {
      System.out.println("using cached results");
      _blockHandler.onSuccess(_blocksCache);
    }
    else {
      System.out.println("using fresh results");
      _service.getStopSequenceBlocksByRoute(_route, _blockHandler);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RouteConstraint))
      return false;

    RouteConstraint rc = (RouteConstraint) obj;
    return _route == rc._route;
  }

  @Override
  public int hashCode() {
    return _route.hashCode();
  }

  /***************************************************************************
   * Internal Classes
   **************************************************************************/

  private class StopSequenceBlockHandler implements
      AsyncCallback<List<StopSequenceBlockBean>> {

    public void onSuccess(List<StopSequenceBlockBean> result) {

      _blocksCache = result;
      _resultsPanel.clear();

      FlowPanel routePanel = new FlowPanel();
      routePanel.addStyleName("routePanel");
      _resultsPanel.add(routePanel);

      routePanel.add(new DivWidget("routeNumber",
          _msgs.standardIndexPageRouteNumber(_route)));
      routePanel.add(new DivWidget("routeSelection",
          _msgs.standardIndexPageSelectADestination()));

      FlowPanel routeDestinationPanel = new FlowPanel();
      routeDestinationPanel.addStyleName("routeDestinationsPanel");
      routePanel.add(routeDestinationPanel);

      boolean first = true;

      for (final StopSequenceBlockBean block : result) {

        DivWidget blockWidget = new DivWidget(block.getDescription());
        blockWidget.addStyleName("routeDestination");

        boolean selected = _blockId == null ? first : block.getId().equals(
            _blockId);

        if (selected) {
          blockWidget.addStyleName("selected");
          handlePaths(block);
          StopsBean sb = new StopsBean();
          sb.setStopBeans(block.getStops());
          _stopsHandler.onSuccess(sb);
        }

        blockWidget.addClickListener(new ClickListener() {
          public void onClick(Widget arg0) {
            newTarget(INDEX_PAGE, KEY_MODE, MODE_ROUTE, KEY_ROUTE, _route,
                KEY_BLOCK_ID, block.getId());
          }
        });

        routeDestinationPanel.add(blockWidget);
        first = false;
      }
    }

    public void onFailure(Throwable ex) {
      _resultsPanel.add(new SpanWidget("fail (1)"));
      if (ex instanceof NoSuchRouteServiceException) {
        _resultsPanel.add(new DivWidget("noSuchRouteError",
            _msgs.commonNoSuchRoute()));
      } else {
        handleException(ex);
      }
    }

    private void handlePaths(final StopSequenceBlockBean block) {

      LatLngBounds bounds = LatLngBounds.newInstance();

      List<PathBean> paths = block.getPaths();

      if (paths.isEmpty())
        return;

      for (PathBean bean : paths) {
        double[] lat = bean.getLat();
        double[] lon = bean.getLon();

        if (lat.length == 0)
          return;

        LatLng[] points = new LatLng[lat.length];

        for (int i = 0; i < lat.length; i++) {
          points[i] = LatLng.newInstance(lat[i], lon[i]);
          bounds.extend(points[i]);
        }

        _map.addOverlay(new Polyline(points, "#4F64BA", 3, 1.0));
      }

      MarkerOptions startOpts = MarkerOptions.newInstance();
      startOpts.setIcon(StopIconFactory.getRouteStartIcon());
      startOpts.setClickable(false);
      LatLng first = LatLng.newInstance(block.getStartLat(),
          block.getStartLon());
      _map.addOverlay(new Marker(first, startOpts));

      MarkerOptions endOpts = MarkerOptions.newInstance();
      endOpts.setIcon(StopIconFactory.getRouteEndIcon());
      endOpts.setClickable(false);
      LatLng last = LatLng.newInstance(block.getEndLat(), block.getEndLon());
      _map.addOverlay(new Marker(last, endOpts));

      int zoom = _map.getBoundsZoomLevel(bounds);
      _map.setCenter(bounds.getCenter(), zoom);
      
      System.out.println("start=" + block.getStartLat() + " " + block.getStartLon());
      System.out.println("end=" + block.getEndLat() + " " + block.getEndLon());
    }

  }
}