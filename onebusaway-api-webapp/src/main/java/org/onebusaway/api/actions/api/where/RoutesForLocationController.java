package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.RouteV2Bean;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class RoutesForLocationController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private static final int V2 = 2;

  private static final double DEFAULT_SEARCH_RADIUS_WITHOUT_QUERY = 500;

  private static final double DEFAULT_SEARCH_RADIUS_WITH_QUERY = 10 * 1000;

  @Autowired
  private TransitDataService _service;

  private double _lat;

  private double _lon;

  private double _radius;

  private double _latSpan;

  private double _lonSpan;

  private String _query;

  private MaxCountSupport _maxCount = new MaxCountSupport(10, 50);

  public RoutesForLocationController() {
    super(V1);
  }

  public void setLat(double lat) {
    _lat = lat;
  }

  public void setLon(double lon) {
    _lon = lon;
  }

  public void setLatSpan(double latSpan) {
    _latSpan = latSpan;
  }

  public void setLonSpan(double lonSpan) {
    _lonSpan = lonSpan;
  }

  public void setRadius(double radius) {
    _radius = radius;
  }

  public void setQuery(String query) {
    _query = query;
  }

  public String getQuery() {
    return _query;
  }

  public void setMaxCount(int maxCount) {
    _maxCount.setMaxCount(maxCount);
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    int maxCount = _maxCount.getMaxCount();
    if (maxCount <= 0)
      addFieldError("maxCount", "must be greater than zero");

    if (hasErrors())
      return setValidationErrorsResponse();

    CoordinateBounds bounds = getSearchBounds();

    SearchQueryBean routesQuery = new SearchQueryBean();

    if (_query != null)
      routesQuery.setQuery(_query);

    routesQuery.setBounds(bounds);
    routesQuery.setMaxCount(maxCount);
    routesQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);

    try {
      RoutesBean result = _service.getRoutes(routesQuery);
      return transformResult(result);
    } catch (OutOfServiceAreaServiceException ex) {
      return transformOutOfRangeResult();
    }
  }

  private DefaultHttpHeaders transformResult(RoutesBean result) {
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
      return setOkResponse(new RoutesBean());
    } else if (isVersion(V2)) {
      BeanFactoryV2 factory = getBeanFactoryV2();
      return setOkResponse(factory.getEmptyList(RouteV2Bean.class, true));
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
