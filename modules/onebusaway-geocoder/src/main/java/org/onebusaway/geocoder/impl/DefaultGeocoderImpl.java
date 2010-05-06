package org.onebusaway.geocoder.impl;

import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;

public class DefaultGeocoderImpl implements GeocoderService {

  @Override
  public GeocoderResults geocode(String location) {
    throw new IllegalStateException(
        "the default geocoder implementation doesn't do much");
  }
}
