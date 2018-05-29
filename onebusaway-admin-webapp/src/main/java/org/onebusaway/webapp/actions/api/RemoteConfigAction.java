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
package org.onebusaway.webapp.actions.api;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
 * A service to expose certain TDM configuration values to the front-end
 * for handling there.
 */
public class RemoteConfigAction extends ActionSupport {

    private static final long serialVersionUID = 1L;
    private static Logger _log = LoggerFactory.getLogger(RemoteConfigAction.class);

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
        return _configurationService.getConfigurationValueAsString("display.siriBaseUrl", getApiBaseURL() + "/siri");
    }

    public String getApiBaseURL() {
        return _configurationService.getConfigurationValueAsString("display.apiBaseUrl", "/onebusaway-api-webapp");
    }
    
    public String getAppBaseUrl() {
        return _configurationService.getConfigurationValueAsString("display.appBaseUrl", "/");
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
    public String getAppHostName() {
        return _configurationService.getConfigurationValueAsString(
                "appHostname", null);
    }
    public String getServiceAlertText() {
        return _configurationService.getConfigurationValueAsString("display.serviceAlertText", "Service Alert");
    }
    
    public boolean isHttps() {
        // force the use of https to remote servers
        String useHttps = _configurationService.getConfigurationValueAsString("display.useHttps", "false");
        boolean isHttps = "true".equalsIgnoreCase(useHttps);
        return isHttps;
    }

    public String getAppHostURL() {
        HttpServletRequest request = ServletActionContext.getRequest();
        if ((request != null && request.isSecure()) || isHttps()) {
            // if we came from https, all subsequent requests need to be https
            return "https://" + getAppHostName();
        }
        return "http://" + getAppHostName();
    }

    public String getCacheBreaker() {
        return String.valueOf(SystemTime.currentTimeMillis());
    }

}
