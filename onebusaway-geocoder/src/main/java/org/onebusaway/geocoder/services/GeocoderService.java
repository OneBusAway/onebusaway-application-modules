package org.onebusaway.geocoder.services;

import org.onebusaway.geocoder.model.GeocoderResults;

public interface GeocoderService {

  public GeocoderResults geocode(String location);

}
