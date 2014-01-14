/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
