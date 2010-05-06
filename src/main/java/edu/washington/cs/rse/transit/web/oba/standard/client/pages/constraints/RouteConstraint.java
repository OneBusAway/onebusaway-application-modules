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
package edu.washington.cs.rse.transit.web.oba.standard.client.pages.constraints;

import edu.washington.cs.rse.transit.web.oba.common.client.Context;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockPathsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchRouteServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.DivWidget;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.SpanWidget;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.ESearchType;
import edu.washington.cs.rse.transit.web.oba.standard.client.pages.StopIconFactory;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class RouteConstraint extends AbstractConstraint {

  private ServicePatternBlockHandler _blockHandler = new ServicePatternBlockHandler();

  private ServicePatternPathHandler _pathHandler = new ServicePatternPathHandler();

  private String _route;

  private String _servicePatternId = null;

  private ServicePatternBlocksBean _blocksCache = null;

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  public RouteConstraint(String route) {
    _route = route;
  }

  public void update(Context context) {

    _wrapper.setSearchText(ESearchType.ROUTE, _route);

    if (context.hasParam(KEY_SERVICE_PATTERN))
      _servicePatternId = context.getParam(KEY_SERVICE_PATTERN);

    if (_blocksCache != null)
      _blockHandler.onSuccess(_blocksCache);
    else
      _service.getServicePatternBlocksByRoute(_route, _blockHandler);

    if (_servicePatternId != null) {
      System.out.println("route=" + _route);
      _service.getServicePatternPath(_route, _servicePatternId, _pathHandler);
      _service.getActiveStopsByServicePattern(_route, _servicePatternId,
          _stopsHandler);
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

  private class ServicePatternBlockHandler implements
      AsyncCallback<ServicePatternBlocksBean> {

    public void onSuccess(ServicePatternBlocksBean result) {

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

      for (final ServicePatternBlockBean block : result.getBlocks()) {

        DivWidget blockWidget = new DivWidget(block.getDescription());
        blockWidget.addStyleName("routeDestination");

        System.out.println(block.getId() + " vs " + _servicePatternId);

        if (block.getId().equals(_servicePatternId))
          blockWidget.addStyleName("selected");

        blockWidget.addClickListener(new ClickListener() {
          public void onClick(Widget arg0) {
            newTarget(INDEX_PAGE, KEY_MODE, MODE_ROUTE, KEY_ROUTE, _route,
                KEY_SERVICE_PATTERN, block.getId());
          }
        });

        routeDestinationPanel.add(blockWidget);
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
  }

  private class ServicePatternPathHandler implements
      AsyncCallback<ServicePatternBlockPathsBean> {

    public void onSuccess(ServicePatternBlockPathsBean paths) {

      LatLngBounds bounds = LatLngBounds.newInstance();

      if (paths.getPaths().isEmpty()) {
        System.out.println("no paths?");
        return;
      }

      for (PathBean bean : paths.getPaths()) {
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
      LatLng first = LatLng.newInstance(paths.getStartLat(), paths.getStartLon());
      _map.addOverlay(new Marker(first, startOpts));

      MarkerOptions endOpts = MarkerOptions.newInstance();
      endOpts.setIcon(StopIconFactory.getRouteEndIcon());
      endOpts.setClickable(false);
      LatLng last = LatLng.newInstance(paths.getEndLat(), paths.getEndLon());
      _map.addOverlay(new Marker(last, endOpts));

      int zoom = _map.getBoundsZoomLevel(bounds);
      _map.setCenter(bounds.getCenter(), zoom);
    }

    public void onFailure(Throwable ex) {
      handleException(ex);
    }
  }
}