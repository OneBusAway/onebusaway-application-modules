package org.onebusaway.api.actions.api.where;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.services.TransitDataService;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class StopsForLocationController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final String VERSION = "1.0";

  private static final double DEFAULT_SEARCH_RADIUS_WITHOUT_QUERY = 500;

  private static final double DEFAULT_SEARCH_RADIUS_WITH_QUERY = 10 * 1000;

  private static final int DEFAULT_MAX_COUNT = 100;

  private static final int ABSOLUTE_MAX_COUNT = 250;

  @Autowired
  private TransitDataService _service;

  private double _lat;

  private double _lon;

  private double _radius;

  private double _latSpan;

  private double _lonSpan;

  private int _maxCount = DEFAULT_MAX_COUNT;

  private String _query;

  public StopsForLocationController() {
    super(VERSION);
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
    _maxCount = maxCount;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (_maxCount <= 0)
      addFieldError("maxCount", "must be greater than zero");

    _maxCount = Math.min(_maxCount, ABSOLUTE_MAX_COUNT);

    if (hasErrors())
      return setValidationErrorsResponse();

    CoordinateBounds bounds = getSearchBounds();
    double minLat = bounds.getMinLat();
    double minLon = bounds.getMinLon();
    double maxLat = bounds.getMaxLat();
    double maxLon = bounds.getMaxLon();

    StopsBean result = null;

    if (_query != null) {
      result = _service.getStopsByBoundsAndQuery(minLat, minLon, maxLat,
          maxLon, _query, _maxCount);
    } else {
      result = _service.getStopsByBounds(minLat, minLon, maxLat, maxLon,
          _maxCount);
    }

    return setOkResponse(result);
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
