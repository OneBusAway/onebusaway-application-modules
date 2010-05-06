package org.onebusaway.webapp.gwt.common.control;

import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;

import java.util.ArrayList;
import java.util.List;

class PlacemarkPlaceImpl implements Place {

  private Placemark _placemark;

  public PlacemarkPlaceImpl(Placemark placemark) {
    _placemark = placemark;
  }

  public String getName() {
    return _placemark.getStreet();
  }

  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add(_placemark.getCity() + ", " + _placemark.getState() + " " + _placemark.getPostalCode());
    return desc;
  }

  public LatLng getLocation() {
    return _placemark.getPoint();
  }

  public int getAccuracy() {
    return _placemark.getAccuracy();
  }
}
