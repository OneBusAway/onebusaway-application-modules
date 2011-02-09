package org.onebusaway.presentation.services;


import org.onebusaway.geocoder.model.GeocoderResults;

public interface SetUserDefaultSearchFromGeocoderService {
  public GeocoderResults setUserDefaultSearchFromGeocoderService(
      String location);
}
