package org.onebusaway.oba.web.standard.client.control.state;

import com.google.gwt.maps.client.geocode.Placemark;

import java.util.List;

public class AddressLookupErrorState extends State {

  private List<Placemark> _locations;

  public AddressLookupErrorState(List<Placemark> locations) {
    _locations = locations;
  }

  public List<Placemark> getLocations() {
    return _locations;
  }
}
