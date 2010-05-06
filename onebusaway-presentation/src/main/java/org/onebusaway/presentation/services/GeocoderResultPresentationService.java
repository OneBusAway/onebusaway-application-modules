package org.onebusaway.presentation.services;

import org.onebusaway.geocoder.model.GeocoderResult;

public interface GeocoderResultPresentationService {
  public String getGeocoderResultAsString(GeocoderResult result);
}
