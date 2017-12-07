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
package org.onebusaway.presentation.impl.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.container.ConfigurationParameter;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.presentation.services.configuration.ConfigurationSource;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultWebappConfigurationSource implements ConfigurationSource {

  private TransitDataService _transitDataService;

  private ServiceAreaService _serviceAreaService;

  private String _googleMapsApiKey = "ABQIAAAA1R_R0bUhLYRwbQFpKHVowhR6ggDNEO1rwvdlk5egWeAHsl3o5xT2ki4Fn-LXLHIrJfb8VmKQeIMh5g";
  
  private String _googleAnalyticsAnalyticsKey = "UA-2423527-7";


  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  /**
   * The default Google Maps API key can only be used externally for the
   * onebusaway.org domain. To use OneBusAway from another domain, you must
   * specify your own Google Maps API key.
   * 
   * @param googleMapsApiKey your Google Maps API access key
   */
  @ConfigurationParameter
  public void setGoogleMapsApiKey(String googleMapsApiKey) {
    _googleMapsApiKey = googleMapsApiKey;
  }
  
  @ConfigurationParameter
  public void setGoogleAnalyticsKey(String clientKey) {
    _googleAnalyticsAnalyticsKey = clientKey;
  }

  @Override
  public Map<String, Object> getConfiguration(String contextPath) {

    Map<String, Object> config = new HashMap<String, Object>();
    config.put("apiKey", "web");

    config.put("baseUrl", contextPath);
    config.put("apiUrl", contextPath + "/api");

    List<AgencyWithCoverageBean> agenciesWithCoverage = _transitDataService.getAgenciesWithCoverage();

    CoordinateBounds bounds = new CoordinateBounds();

    for (AgencyWithCoverageBean awc : agenciesWithCoverage) {

      if (awc.getLatSpan() <= 0 || awc.getLonSpan() <= 0)
        continue;

      bounds.addPoint(awc.getLat() + awc.getLatSpan() / 2,
          awc.getLon() + awc.getLonSpan() / 2);
      bounds.addPoint(awc.getLat() - awc.getLatSpan() / 2,
          awc.getLon() - awc.getLonSpan() / 2);
    }

    if (bounds.isEmpty()) {
      config.put("centerLat", 0.0);
      config.put("centerLon", 0.0);
      config.put("spanLat", 180.0);
      config.put("spanLon", 180.0);
    } else {
      config.put("centerLat", (bounds.getMinLat() + bounds.getMaxLat()) / 2);
      config.put("centerLon", (bounds.getMinLon() + bounds.getMaxLon()) / 2);
      config.put("spanLat", bounds.getMaxLat() - bounds.getMinLat());
      config.put("spanLon", bounds.getMaxLon() - bounds.getMinLon());
    }

    config.put("hasDefaultServiceArea",
        _serviceAreaService.hasDefaultServiceArea());
    config.put("googleMapsApiKey", _googleMapsApiKey);
    config.put("tracker", _googleAnalyticsAnalyticsKey);
    return config;
  }

}
