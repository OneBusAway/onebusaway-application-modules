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
package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.StopV2Bean;
import org.onebusaway.api.services.ApiIntervalFactory;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.presentation.impl.conversion.DateTimeConverter;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopFilterByRouteType;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

public class StopsForLocationAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  private static final double DEFAULT_SEARCH_RADIUS_WITHOUT_QUERY = 500;

  private static final double DEFAULT_SEARCH_RADIUS_WITH_QUERY = 10 * 1000;

  @Autowired
  private TransitDataService _service;

  @Autowired
  private ApiIntervalFactory _factory;

  private DateTimeConverter _dateTimeConverter = new DateTimeConverter();

  private double _lat;

  private double _lon;

  private double _radius;

  private double _latSpan;

  private double _lonSpan;

  private MaxCountSupport _maxCount = new MaxCountSupport(100, 250);

  // GTFS route type (3=bus)
  private String _routeType;

  private String _query;

  private long _time = 0;

  public StopsForLocationAction() {
    super(LegacyV1ApiSupport.isDefaultToV1() ? V1 : V2);
  }

  public void setLat(double lat) {
    _lat = lat;
  }

  public void setLon(double lon) {
    _lon = lon;
  }

  public void setRadius(double radius) {
    _radius = radius;
  }

  public void setLatSpan(double latSpan) {
    _latSpan = latSpan;
  }

  public void setLonSpan(double lonSpan) {
    _lonSpan = lonSpan;
  }

  public void setQuery(String query) {
    _query = query;
  }

  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public void setRouteType(String routeType) {
    this._routeType = routeType;
  }

  public void setTime(String timeStr) {
      _time = _dateTimeConverter.parse(timeStr);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    int maxCount = _maxCount.getMaxCount();

    if (maxCount <= 0)
      addFieldError("maxCount", "must be greater than zero");

    if (hasErrors())
      return setValidationErrorsResponse();

    CoordinateBounds bounds = getSearchBounds();
    if (_time == 0) {
      _time = SystemTime.currentTimeMillis();
    }

    SearchQueryBean searchQuery = new SearchQueryBean();
    searchQuery.setServiceInterval(_factory.constructForDate(new Date(_time)));
    searchQuery.setBounds(bounds);
    searchQuery.setMaxCount(maxCount);
    searchQuery.setType(EQueryType.BOUNDS);
    if (_routeType != null && _routeType.length() > 0) {
      // for this filtering to work we need to order the results
      searchQuery.setType(SearchQueryBean.EQueryType.ORDERED_BY_CLOSEST);
      List<Integer> routeFilters = new ArrayList<>();
      for (String typeStr : _routeType.split(",")) {
        routeFilters.add(Integer.parseInt(typeStr));
      }
      searchQuery.getInstanceFilterChain().add(new StopFilterByRouteType(routeFilters));
    }
    if (_query != null) {
      searchQuery.setQuery(_query);
      searchQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
    }

    try {
      StopsBean result = _service.getStops(searchQuery);
      return transformResult(result);
    } catch (OutOfServiceAreaServiceException ex) {
      return transformOutOfRangeResult();
    }
  }

  private DefaultHttpHeaders transformResult(StopsBean result) {
    if (isVersion(V1)) {
      return setOkResponse(result);
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getResponse(result));
    } else {
      return setUnknownVersionResponse();
    }
  }

  private DefaultHttpHeaders transformOutOfRangeResult() {
    if (isVersion(V1)) {
      return setOkResponse(new StopsBean());
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getEmptyList(StopV2Bean.class, true));
    } else {
      return setUnknownVersionResponse();
    }
  }

  private CoordinateBounds getSearchBounds() {

    if (_radius > 0) {
      return SphericalGeometryLibrary.bounds(_lat, _lon, _radius);
    } else if (_latSpan > 0 && _lonSpan > 0) {
      return SphericalGeometryLibrary.boundsFromLatLonOffset(_lat, _lon,
          _latSpan / 2, _lonSpan / 2);
    } else {
      if (_query != null)
        return SphericalGeometryLibrary.bounds(_lat, _lon,
            DEFAULT_SEARCH_RADIUS_WITH_QUERY);
      else
        return SphericalGeometryLibrary.bounds(_lat, _lon,
            DEFAULT_SEARCH_RADIUS_WITHOUT_QUERY);
    }
  }
}
