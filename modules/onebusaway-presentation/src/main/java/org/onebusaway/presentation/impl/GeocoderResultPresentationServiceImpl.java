package org.onebusaway.presentation.impl;

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.presentation.services.GeocoderResultPresentationService;

public class GeocoderResultPresentationServiceImpl implements
    GeocoderResultPresentationService {

  public String getGeocoderResultAsString(GeocoderResult result) {

    StringBuilder b = new StringBuilder();

    appendIfSet(b, result.getAddress());
    appendIfSet(b, result.getCity());
    appendIfSet(b, result.getAdministrativeArea());
    appendIfSet(b, result.getPostalCode());

    return b.toString();
  }

  private static void appendIfSet(StringBuilder b, String value) {
    if (isSet(value)) {
      if (b.length() > 0)
        b.append(", ");
      b.append(value);
    }
  }

  private static final boolean isSet(String value) {
    return value != null && value.length() > 0;
  }
}
