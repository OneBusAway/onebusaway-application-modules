package org.onebusaway.api.actions.api.where;

import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class TripsForLocationController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final String VERSION = "1.0";

  @Autowired
  private TransitDataService _service;

  private double _lat;

  private double _lon;

  private double _latSpan;

  private double _lonSpan;

  private long _time = 0;

  public TripsForLocationController() {
    super(VERSION);
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

  public void setTime(long time) {
    _time = time;
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    CoordinateBounds bounds = getSearchBounds();

    long time = System.currentTimeMillis();
    if (_time != 0)
      time = _time;

    ListBean<TripStatusBean> results = _service.getTripsForBounds(bounds,
        time);

    return setOkResponse(results);
  }

  private CoordinateBounds getSearchBounds() {
    return SphericalGeometryLibrary.boundsFromLatLonOffset(_lat, _lon,
        _latSpan / 2, _lonSpan / 2);
  }
}
