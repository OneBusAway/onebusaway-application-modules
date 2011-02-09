package org.onebusaway.webapp.gwt.oba_application.control.state;

import org.onebusaway.webapp.gwt.common.control.Place;

import java.util.List;

public class AddressLookupErrorState extends State {

  private List<Place> _locations;

  public AddressLookupErrorState(List<Place> locations) {
    _locations = locations;
  }

  public List<Place> getLocations() {
    return _locations;
  }
}
