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
package org.onebusaway.webapp.gwt.where_library.view;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextImpl;
import org.onebusaway.webapp.gwt.common.context.ContextListener;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.context.DirectContextManager;
import org.onebusaway.webapp.gwt.where_library.WhereLibrary;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.view.constraints.DefaultOperationHandler;
import org.onebusaway.webapp.gwt.where_library.view.constraints.LocationOperationHandler;
import org.onebusaway.webapp.gwt.where_library.view.constraints.OperationContext;
import org.onebusaway.webapp.gwt.where_library.view.constraints.OperationHandler;
import org.onebusaway.webapp.gwt.where_library.view.constraints.QueryOperationHandler;
import org.onebusaway.webapp.gwt.where_library.view.constraints.RouteOperationHandler;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

public class StopFinderPresenter implements StopFinderInterface,
    StopFinderConstants, ContextListener {

  private static NumberFormat _format = NumberFormat.getFormat("0.0000");

  private int _unique = 0;

  private ContextManager _contextManager = null;

  private StopFinderWidget _widget;

  private OperationHandler _defaultOperationHandler = new DefaultOperationHandler();

  /*****************************************************************************
   * Public Methods
   ****************************************************************************/

  public StopFinderPresenter() {
    this(new DirectContextManager());
  }

  public StopFinderPresenter(ContextManager contextManager) {
    setContextManager(contextManager);
  }

  public void setContextManager(ContextManager contextManager) {
    if (_contextManager != null)
      _contextManager.removeContextListener(this);

    _contextManager = contextManager;
    contextManager.addContextListener(this);
  }

  public void setWidget(StopFinderWidget widget) {
    _widget = widget;
  }

  public void initialize() {
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        Context context = _contextManager.getContext();
        if (context == null)
          context = new ContextImpl();
        onContextChanged(context);
      }
    });
  }

  public Context getCoordinateBoundsAsContext(CoordinateBounds bounds) {
    if (bounds.isEmpty())
      return new ContextImpl();
    double latCenter = (bounds.getMinLat() + bounds.getMaxLat()) / 2;
    double lonCenter = (bounds.getMinLon() + bounds.getMaxLon()) / 2;
    double latSpan = bounds.getMaxLat() - bounds.getMinLat();
    double lonSpan = bounds.getMaxLon() - bounds.getMinLon();
    Map<String, String> m = new HashMap<String, String>();
    addBoundsToParams(m, latCenter, lonCenter, latSpan, lonSpan);
    return new ContextImpl(m);
  }

  public void setDefaultOperationHandler(OperationHandler handler) {
    _defaultOperationHandler = handler;
  }

  /****
   * {@link ContextListener} Interface
   ****/

  public void onContextChanged(Context context) {
    _widget.resetContents();
    boolean locationSet = setMapCenter(context);
    handleOperation(context, locationSet);
  }

  /****
   * {@link StopFinderInterface}
   ****/

  @Override
  public String getCurrentViewAsUrl() {
    Context context = _contextManager.getContext();
    if (context == null)
      context = new ContextImpl();
    context = buildContext(context.getParams(), true);
    return "#" + _contextManager.getContextAsString(context);
  }

  @Override
  public void queryCurrentView() {
    internalQuery(true, KEY_UNIQUE, _unique++);
  }

  @Override
  public void query(String query) {
    MapWidget map = _widget.getMapWidget();
    LatLng center = map.getCenter();
    String qll = format(center.getLatitude()) + ","
        + format(center.getLongitude());
    internalQuery(false, KEY_MODE, MODE_QUERY, KEY_QUERY, query,
        KEY_QUERY_LATLON, qll, KEY_UNIQUE, _unique++);
  }

  @Override
  public void queryLocation(LatLng location, int accuracy) {
    internalQuery(false, KEY_MODE, MODE_LOCATION, "lat",
        location.getLatitude(), "lon", location.getLongitude(), "accuracy",
        accuracy);
  }

  @Override
  public void queryRoute(String routeId) {
    internalQuery(false, KEY_MODE, MODE_ROUTE, KEY_ROUTE, routeId);
  }

  @Override
  public String getStopQueryLink(String stopId) {
    return "stop.action?id=" + stopId;
  }

  /*****************************************************************************
   * Protected Methods
   ****************************************************************************/

  protected OperationHandler getDefaultOperationHandler() {
    return _defaultOperationHandler;
  }

  /*****************************************************************************
   * Private Methods
   * 
   * @param includeView
   ****************************************************************************/

  private Context buildContext(Map<String, String> params, boolean includeView) {

    Map<String, String> m = new LinkedHashMap<String, String>();
    m.putAll(params);

    if (includeView) {
      MapWidget map = _widget.getMapWidget();

      LatLng center = map.getCenter();

      LatLngBounds bounds = map.getBounds();
      LatLng ne = bounds.getNorthEast();
      LatLng sw = bounds.getSouthWest();
      double latSpan = Math.abs(ne.getLatitude() - sw.getLatitude());
      double lonSpan = Math.abs(ne.getLongitude() - sw.getLongitude());

      addBoundsToParams(m, center.getLatitude(), center.getLongitude(),
          latSpan, lonSpan);
    }

    return new ContextImpl(m);
  }

  private void internalQuery(boolean includeView, Object... params) {

    Map<String, String> m = new LinkedHashMap<String, String>();

    if (params.length % 2 != 0)
      throw new IllegalArgumentException(
          "Number of params must be even (key-value pairs)");

    for (int i = 0; i < params.length; i += 2)
      m.put(params[i].toString(), params[i + 1].toString());

    Context context = buildContext(m, includeView);

    _contextManager.setContext(context);
  }

  private void addBoundsToParams(Map<String, String> m, double latCenter,
      double lonCenter, double latSpan, double lonSpan) {
    m.put(KEY_LATLON, format(latCenter) + "," + format(lonCenter));
    m.put(KEY_SPAN, format(latSpan) + "," + format(lonSpan));
  }

  private String format(double dv) {
    return _format.format(dv);
  }

  private boolean setMapCenter(Context context) {


    try {

      String latlon = context.getParam(KEY_LATLON);
      LatLng center = getStringAsLatLng(latlon);

      if( center == null)
        return false;
      int zoomLevel = getZoomLevelForContextAndCenter(context, center);
      MapWidget map = _widget.getMapWidget();
      map.setCenter(center, zoomLevel);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  private LatLng getStringAsLatLng(String latlon) {
    
    if (latlon == null)
      return null;

    String[] tokens = latlon.split(",");
    if (tokens.length != 2)
      return null;

    try {
      double lat = Double.parseDouble(tokens[0]);
      double lon = Double.parseDouble(tokens[1]);
      return LatLng.newInstance(lat, lon);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private int getZoomLevelForContextAndCenter(Context context, LatLng center) {

    int zoom = 16;

    String param = context.getParam(KEY_SPAN);
    if (param == null)
      return zoom;

    String[] tokens = param.split(",");
    if (tokens.length != 2)
      return zoom;

    try {
      double latRadius = Double.parseDouble(tokens[0]) / 2;
      double lonRadius = Double.parseDouble(tokens[1]) / 2;

      LatLngBounds bounds = LatLngBounds.newInstance();
      bounds.extend(LatLng.newInstance(center.getLatitude() + latRadius,
          center.getLongitude() + lonRadius));
      bounds.extend(LatLng.newInstance(center.getLatitude() - latRadius,
          center.getLongitude() - lonRadius));

      MapWidget map = _widget.getMapWidget();
      return map.getBoundsZoomLevel(bounds);
    } catch (NumberFormatException ex) {
      return zoom;
    }
  }

  private void handleOperation(Context context, boolean locationSet) {
    OperationHandler handler = getOperartionHandler(context, locationSet);
    OperationContext opContext = new OperationContext(_widget, locationSet);
    handler.handleOperation(opContext);
  }

  private OperationHandler getOperartionHandler(Context context,
      boolean locationSet) {

    String mode = context.getParam(KEY_MODE);

    if (MODE_QUERY.equals(mode)) {

      String query = context.getParam(KEY_QUERY);
      if (query == null || query.length() == 0)
        return getDefaultOperationHandler();
      String qll = context.getParam(KEY_QUERY_LATLON);
      LatLng point = getStringAsLatLng(qll);
      return new QueryOperationHandler(query,point);

    } else if (MODE_LOCATION.equals(mode)) {

      try {
        double lat = Double.parseDouble(context.getParam("lat"));
        double lon = Double.parseDouble(context.getParam("lon"));
        int accuracy = Integer.parseInt(context.getParam("accuracy"));

        return new LocationOperationHandler(LatLng.newInstance(lat, lon),
            accuracy);
      } catch (NumberFormatException ex) {
        WhereMessages msgs = WhereLibrary.MESSAGES;
        throw new IllegalStateException(
            msgs.standardIndexPageInvalidLocationSpecified());
      }

    } else if (MODE_ROUTE.equals(mode)) {
      String route = context.getParam(KEY_ROUTE);
      return new RouteOperationHandler(route);
    }

    return getDefaultOperationHandler();
  }
}
