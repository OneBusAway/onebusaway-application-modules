package org.onebusaway.federations.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.federations.CoordinateBoundsTestBean;
import org.onebusaway.federations.SimpleFederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

public class SimpleFederatedServiceImpl implements SimpleFederatedService {

  private Map<String, List<CoordinateBounds>> _agencyIdsWithCoverageArea;

  private String _value;

  public SimpleFederatedServiceImpl(
      Map<String, List<CoordinateBounds>> agencyIdsWithCoverageArea,
      String value) {
    _agencyIdsWithCoverageArea = agencyIdsWithCoverageArea;
    _value = value;
  }

  @Override
  public Map<String, List<CoordinateBounds>> getAgencyIdsWithCoverageArea() {
    return _agencyIdsWithCoverageArea;
  }

  @Override
  public String getValueForBounds(double lat1, double lon1, double lat2,
      double lon2) {
    return _value;
  }

  @Override
  public String getValueForCoordinateBounds(CoordinateBounds bounds) {
    return _value;
  }

  @Override
  public String getValueForCoordinateBoundsTestBean(
      CoordinateBoundsTestBean bounds) {
    return _value;
  }

  @Override
  public String getValueForId(String entityId) {
    return _value;
  }

  @Override
  public String getValueForIds(Set<String> entityId) {
    return _value;
  }

  @Override
  public String getValueForLocation(double lat, double lon) {
    return _value;
  }

  @Override
  public String getValueForValueAndId(String value, String entityId) {
    return _value;
  }

  @Override
  public String getValueForValueAndIds(String value, Set<String> entityId) {
    return _value;
  }

  @Override
  public List<String> getValuesAsList() {
    return Arrays.asList(_value);
  }

  @Override
  public Map<String, String> getValuesAsMap() {
    HashMap<String, String> m = new HashMap<String, String>();
    m.put(_value, _value);
    return m;
  }
}