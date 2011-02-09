package org.onebusaway.webapp.gwt.common.control;

import java.util.Collections;
import java.util.List;

import com.google.gwt.maps.client.geom.LatLng;

public class PlaceImpl extends AbstractPlaceImpl {

  private String _name;
  private List<String> _description;
  private LatLng _location;
  private int _accuracy;

  @SuppressWarnings("unchecked")
  public PlaceImpl(String name, LatLng location, int accuracy) {
    this(name, Collections.EMPTY_LIST, location, accuracy);
  }

  public PlaceImpl(String name, List<String> description, LatLng location,
      int accuracy) {
    _name = name;
    _description = description;
    _location = location;
    _accuracy = accuracy;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public List<String> getDescription() {
    return _description;
  }

  @Override
  public LatLng getLocation() {
    return _location;
  }

  @Override
  public int getAccuracy() {
    return _accuracy;
  }

}
