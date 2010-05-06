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
