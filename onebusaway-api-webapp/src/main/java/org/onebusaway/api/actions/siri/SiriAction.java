/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.siri;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.onebusaway.api.actions.api.ApiActionSupport;

import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.impl.analytics.GoogleAnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for Siri Actions.
 */
public class SiriAction extends ApiActionSupport
        implements ServletRequestAware, ServletResponseAware {

  protected static final int V3 = 3;
  public static final double MAX_BOUNDS_DISTANCE= 500;
  public static final double MAX_BOUNDS_RADIUS= 250;

  public static final String BOUNDING_BOX = "BoundingBox";
  public static final String CIRCLE = "Circle";
  public static final String MONITORING_REF = "MonitoringRef";
  public static final String LINE_REF = "LineRef";
  public static final String STOP_POINTS_DETAIL_LEVEL = "StopPointsDetailLevel";
  public static final String DIRECTION_REF = "DirectionRef";
  public static final String OPERATOR_REF = "OperatorRef";
  public static final String VEHICLE_REF = "VehicleRef";
  public static final String STOP_MONITORING_DETAIL_LEVEL = "StopMonitoringDetailLevel";
  public static final String VEHICLE_MONITORING_DETAIL_LEVEL = "VehicleMonitoringDetailLevel";
  public static final String UPCOMING_SCHEDULED_SERVICE = "hasUpcomingScheduledService";
  public static final String MAX_ONWARD_CALLS = "MaximumNumberOfCallsOnwards";
  public static final String MAX_STOP_VISITS = "MaximumStopVisits";
  public static final String MIN_STOP_VISITS = "MinimumStopVisitsPerLine";
  public static final String RAW_LOCATION = "ShowRawLocation";
  public static final String TRIP_ID = "TripId";


  @Autowired
  protected TransitDataService _transitDataService;

  @Autowired
  private GoogleAnalyticsServiceImpl _gaService;

  protected HttpServletRequest _servletRequest;
  protected HttpServletResponse _servletResponse;

  // See urlrewrite.xml as to how this is set.  Which means this action doesn't respect an HTTP Accept: header.
  private String _type = null;

  public SiriAction(int defaultVersion) {
    super(defaultVersion);
  }

  public void setType(String type) {
    _type = type;
  }

  public String getType() {
    if (_type == null) {
      if (_servletRequest == null || _servletRequest.getRequestURI() == null) {
        return "xml"; // default on startup and testing
      }
      if (_servletRequest.getRequestURI().contains("json")) {
        return "json";
      }
      // default to xml
      return "xml";
    }
    return _type;
  }

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this._servletRequest = request;
  }

  @Override
  public void setServletResponse(HttpServletResponse servletResponse) {
    this._servletResponse = servletResponse;
  }

  public HttpServletResponse getServletResponse() {
    return _servletResponse;
  }

  protected void processGoogleAnalytics() {
    // no longer supported
  }

}
