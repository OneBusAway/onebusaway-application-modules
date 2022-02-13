/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.api;


import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.OneBusAwayFormats;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/*
 * A service to expose certain TDM configuration values to the front-end
 * for handling there.
 */
public class ConfigAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;


  @Autowired
  private ConfigurationService _configurationService;
  
  @Autowired
  TransitDataService _transitDataService;

  public int getStaleTimeout() {
    return _configurationService.getConfigurationValueAsInteger("display.staleTimeout", 120);    
  }

  public String getGoogleAnalyticsSiteId() {
    return _configurationService.getConfigurationValueAsString("display.googleAnalyticsSiteId", null);    
  }

  public String getBingMapsKey() {
    return _configurationService.getConfigurationValueAsString("display.bingMapsKey", null);    
  }
  
  public String getObaApiKey() {
    return _configurationService.getConfigurationValueAsString("display.obaApiKey", "OBAKEY");    
  }
  
  public String getMapBounds() {
    List<AgencyWithCoverageBean> agencyWithCoverageBeans = _transitDataService.getAgenciesWithCoverage();
    
    Double minLat = 999d;
    Double minLon = 999d;
    Double maxLat = -999d;
    Double maxLon = -999d;
    
    for (AgencyWithCoverageBean agencyWithCoverageBean : agencyWithCoverageBeans) {
      if (agencyWithCoverageBean.getLat() != 0 && agencyWithCoverageBean.getLon() != 0) {
        minLat = Math.min(minLat, agencyWithCoverageBean.getLat() - agencyWithCoverageBean.getLatSpan()/2);
        minLon = Math.min(minLon, agencyWithCoverageBean.getLon() - agencyWithCoverageBean.getLonSpan()/2);
        maxLat = Math.max(maxLat, agencyWithCoverageBean.getLat() + agencyWithCoverageBean.getLatSpan()/2);
        maxLon = Math.max(maxLon, agencyWithCoverageBean.getLon() + agencyWithCoverageBean.getLonSpan()/2);
      }
    }
    
    return "{ swLat: " + minLat + ", swLon: " + minLon + ", neLat: " + maxLat + ", neLon: " + maxLon + " }";
  }
  
  public Float getMapCenterLat() {
    return _configurationService.getConfigurationValueAsFloat("display.mapCenterLat", null);
  }
  
  public Float getMapCenterLon() {
    return _configurationService.getConfigurationValueAsFloat("display.mapCenterLon", null);
  }
  
  public Integer getMapZoom() {
    return _configurationService.getConfigurationValueAsInteger("display.mapZoom", null);
  }
  
  public String getSiriBaseURL() {
    return _configurationService.getConfigurationValueAsString("display.siriBaseUrl", "/onebusaway-api-webapp/siri");
  }

  public String getApiBaseURL() {
    return _configurationService.getConfigurationValueAsString("display.apiBaseUrl", "/onebusaway-api-webapp");
  }
  
  public String getUseAgencyId() {
    return _configurationService.getConfigurationValueAsString("display.useAgencyId", "false");
  }
  
  public String getShowAgencyNames() {
    return _configurationService.getConfigurationValueAsString("display.showAgencyNames", "false");
  }
  
  public String getShowVehicleIdInStopPopup() {
    return _configurationService.getConfigurationValueAsString("display.showVehicleIdInStopPopup", "false");
  }

  public String getShowExpectedArrivalTimeInStopPopup() {
    return _configurationService.getConfigurationValueAsString("display.showExpectedArrivalTimeInStopPopup", "false");
  }

  public String getShowBlockIdInVehiclePopup() {
    return _configurationService.getConfigurationValueAsString("display.showBlockIdInVehiclePopup", "false");
  }

  public String getServiceAlertText() {
    return _configurationService.getConfigurationValueAsString("display.serviceAlertText", "Service Alert");
  }

  public String getOccupancySeatsAvailable() {
    return _configurationService.getConfigurationValueAsString("display.occupancy.SEATS_AVAILABLE", OneBusAwayFormats.toPascalCaseWithSpaces(OccupancyStatus.MANY_SEATS_AVAILABLE.name()));
  }

  public String getOccupancyStandingAvailable() {
    return _configurationService.getConfigurationValueAsString("display.occupancy.STANDING_AVAILABLE", OneBusAwayFormats.toPascalCaseWithSpaces(OccupancyStatus.FEW_SEATS_AVAILABLE.name()));
  }

  public String getOccupancyFull() {
    return _configurationService.getConfigurationValueAsString("display.occupancy.FULL", OneBusAwayFormats.toPascalCaseWithSpaces(OccupancyStatus.FULL.name()));
  }

  public String getFeedbackFormText() {
    return _configurationService.getConfigurationValueAsString("display.feedbackForm.text", null);
  }

  public String getFeedbackFormURL() {
    return _configurationService.getConfigurationValueAsString("display.feedbackForm.url", null);
  }
}
