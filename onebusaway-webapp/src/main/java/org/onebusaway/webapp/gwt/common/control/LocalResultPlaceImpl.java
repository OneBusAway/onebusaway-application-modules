package org.onebusaway.webapp.gwt.common.control;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.search.client.LocalResult;

import java.util.ArrayList;
import java.util.List;

public class LocalResultPlaceImpl extends AbstractPlaceImpl {

  private LocalResult _result;

  public LocalResultPlaceImpl(LocalResult result) {
    _result = result;
  }

  public String getName() {
    return _result.getTitleNoFormatting();
  }

  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    if (_result.getStreetAddress() != null && _result.getStreetAddress().length() > 0 && ! _result.getStreetAddress().equals(_result.getTitleNoFormatting()))
      desc.add(_result.getStreetAddress());
    boolean city = _result.getCity() != null && _result.getCity().length() > 0;
    boolean region = _result.getRegion() != null & _result.getRegion().length() > 0;
    if (city && region)
      desc.add(_result.getCity() + ", " + _result.getRegion());
    else if (city)
      desc.add(_result.getCity());
    else if (region)
      desc.add(_result.getRegion());
    return desc;
  }

  public LatLng getLocation() {
    return LatLng.newInstance(_result.getLat(), _result.getLng());
  }

  public int getAccuracy() {
    return 9;
  }
  
  @Override
  public String toString() {
    return "LocalPlace: " + getName() + " " + getDescription();
  }
}
