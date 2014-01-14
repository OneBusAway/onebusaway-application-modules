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

import java.util.List;

import org.onebusaway.geocoder.model.GeocoderResult;
import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.services.GeocoderService;
import org.onebusaway.presentation.services.GeocoderResultPresentationService;
import org.onebusaway.presentation.services.SetUserDefaultSearchFromGeocoderService;
import org.onebusaway.presentation.services.DefaultSearchLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetUserDefaultSearchFromGeocoderServiceImpl implements
    SetUserDefaultSearchFromGeocoderService {

  private GeocoderService _geocoderService;

  private DefaultSearchLocationService _searchLocationService;

  private GeocoderResultPresentationService _geocoderResultPresentationService = new GeocoderResultPresentationServiceImpl();

  @Autowired
  public void setGeocoderService(GeocoderService geocoderService) {
    _geocoderService = geocoderService;
  }

  @Autowired
  public void setSearchLocationService(
      DefaultSearchLocationService searchLocationService) {
    _searchLocationService = searchLocationService;
  }

  public void setGeocoderResultPresentationService(
      GeocoderResultPresentationService service) {
    _geocoderResultPresentationService = service;
  }

  public GeocoderResults setUserDefaultSearchFromGeocoderService(
      String location) {

    GeocoderResults results = _geocoderService.geocode(location);
    List<GeocoderResult> records = results.getResults();

    if (records.size() == 1) {

      GeocoderResult result = records.get(0);

      // Store default search location for future sessions
      String name = _geocoderResultPresentationService.getGeocoderResultAsString(result);
      if( name == null || name.length() == 0)
        name = location;
      _searchLocationService.setDefaultLocationForCurrentUser(name, result.getLatitude(),
          result.getLongitude());
    }

    return results;
  }

}
