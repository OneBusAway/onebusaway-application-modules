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
package org.onebusaway.enterprise.webapp.actions.m;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.presentation.model.SearchResultCollection;
import org.onebusaway.presentation.services.realtime.RealtimeService;
import org.onebusaway.presentation.services.routes.RouteListService;
import org.onebusaway.presentation.services.search.SearchResultFactory;
import org.onebusaway.presentation.services.search.SearchService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.onebusaway.enterprise.webapp.actions.m.model.GeocodeResult;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteAtStop;
import org.onebusaway.enterprise.webapp.actions.m.model.RouteResult;
import org.onebusaway.enterprise.webapp.actions.m.model.StopResult;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.springframework.beans.factory.annotation.Autowired;

public class IndexAction extends OneBusAwayEnterpriseActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private ConfigurationService _configurationService;

  @Autowired
  private TransitDataService _transitDataService;

  @Autowired
  private RealtimeService _realtimeService;

  @Autowired
  private SearchService _searchService;

  @Autowired
  private ConfigurationService _configService;

  @Autowired
  private RouteListService _routeListService;

  private SearchResultCollection _results = new SearchResultCollection();

  private boolean _resultsOriginatedFromGeocode = false;

  private String _q = null;

  private CoordinatePoint _location = null;

  private String _type = null;

  private String _agencyFilter = null;

  public void setAgencyFilter(String filter) {
    _agencyFilter = filter;
  }

  public List<RouteBean> getRoutes() {
    if (_agencyFilter != null)
      return _routeListService.getFilteredRoutes(_agencyFilter);
    return _routeListService.getRoutes();

  }


  public void setQ(String q) {
    if (q != null) {
      this._q = q.trim();
    }
  }

  public void setL(String location) {
    String[] locationParts = location.split(",");

    if (locationParts.length == 2) {
      this._location = new CoordinatePoint(
          Double.parseDouble(locationParts[0]),
          Double.parseDouble(locationParts[1]));
    }
  }

  public void setT(String type) {
    this._type = type;
  }

  public String execute() throws Exception {
    if (_q == null)
      return SUCCESS;

    SearchResultFactory factory = new SearchResultFactoryImpl(
        _transitDataService, _realtimeService, _configurationService);

    // empty query with location means search for stops near current location
    if (_location != null && _q.isEmpty()) {
      if (_type.equals("stops")) {
        _results = _searchService.findStopsNearPoint(_location.getLat(),
            _location.getLon(), factory, _results.getRouteFilter());
      } else {
        _results = _searchService.findRoutesStoppingNearPoint(
            _location.getLat(), _location.getLon(), factory);
      }

    } else {
      if (_q.isEmpty()) {
        return SUCCESS;
      }

      boolean serviceDateFilterOn = Boolean.parseBoolean(_configService.getConfigurationValueAsString("display.serviceDateFiltering", "false"));
      if (serviceDateFilterOn) {
        _results = _searchService.getSearchResultsForServiceDate(_q, factory, new ServiceDate(new Date(SystemTime.currentTimeMillis())));
      }
      else {
        _results = _searchService.getSearchResults(_q, factory);
      }

      // do a bit of a hack with location matches--since we have no map to show
      // locations on,
      // find things that are actionable near/within/etc. the result
      if (_results.getMatches().size() == 1
          && _results.getResultType().equals("GeocodeResult")) {

        this._resultsOriginatedFromGeocode = true;
        GeocodeResult result = (GeocodeResult) _results.getMatches().get(0);

        // if we got a region back, list routes that pass through it
        if (result.getIsRegion()) {
          _results = _searchService.findRoutesStoppingWithinRegion(
              result.getBounds(), factory);

          // if we got a location (point) back, find stops nearby
        } else {
          _results = _searchService.findStopsNearPoint(result.getLatitude(),
              result.getLongitude(), factory, _results.getRouteFilter());
        }
      } else {
        if (_results.getMatches().isEmpty() && _results.getSuggestions().size() > 1 && !_q.contains(",")) {
          // we have multiple suggestions but we don't support disambiguation
          // force a geocode search on the former argument
          // TODO SUPPORT DISAMBIGUATION!!!!
          _q = _q + ',';
          execute();
        }
      }
    }

    return SUCCESS;
  }

  /**
   * METHODS FOR VIEWS
   */
  public List<ServiceAlertBean> getGlobalServiceAlerts() {
    List<ServiceAlertBean> results = _realtimeService.getServiceAlertsGlobal();
    return (results != null && results.size() > 0) ? results : null;
  }

  public String getGoogleMapsApiKey() {
    return _configurationService.getConfigurationValueAsString("display.googleMapsApiKey", "");
  }

  public String getGoogleAnalyticsSiteId() {
	  return _configurationService.getConfigurationValueAsString(
              "display.googleAnalyticsSiteId", null);
  }
  
  public String getGoogleAnalyticsValue() {
      // event tracking
      String label = getQ();
      if (label == null) {
        label = "";
      }
      label += " [M: " + _results.getMatches().size() + " S: "
          + _results.getSuggestions().size() + "]";
      label = label.trim();
      return label;
  }
  
  public String getGoogleAnalyticsLabel() {
      String action = "Unknown";
      if (_results != null && !_results.isEmpty()) {
        if (_results.getResultType().equals("RouteInRegionResult")) {
          action = "Region Search";

        } else if (_results.getResultType().equals("RouteResult")) {
          if (_location != null) {
            action = "GPS Route Search";
          } else {
            action = "Route Search";
          }
        } else if (_results.getResultType().equals("GeocodeResult")) {
          action = "Location Disambiguation";

        } else if (_results.getResultType().equals("StopResult")) {
          if (_location != null) {
            action = "GPS Stop Search";
          } else {
            action = "Stop or Intersection Search";
          }
        }
      } else {
        if (getQueryIsEmpty() && _location == null) {
          action = "Home";
        } else {
          action = "No Search Results";
        }
      }
      return action;
  }

  public String getQ() {
    if (_q == null || _q.isEmpty()) {
      return null;
    } else {
      return StringEscapeUtils.escapeHtml(_q.replace("&amp;", "&"));
    }
  }

  public String getL() {
    if (_location != null)
      return _location.getLat() + "," + _location.getLon();
    else
      return null;
  }

  public String getT() {
    return this._type;
  }

  public String getRouteColors() {
    Set<String> routeColors = new HashSet<String>();
    for (SearchResult _result : _results.getMatches()) {
      RouteResult result = (RouteResult) _result;
      routeColors.add(result.getColor());
    }

    return StringUtils.join(routeColors, ",");
  }

  public String getCacheBreaker() {
    return String.valueOf(SystemTime.currentTimeMillis());
  }

  public boolean getQueryIsEmpty() {
    return (_q == null || _q.isEmpty());
  }

  public String getLastUpdateTime() {
    return DateFormat.getTimeInstance().format(new Date(SystemTime.currentTimeMillis()));
  }

  public String getResultType() {
    return _results.getResultType();
  }

  public Set<String> getUniqueServiceAlertsForResults() {
    Set<String> uniqueServiceAlerts = new HashSet<String>();

    for (SearchResult _result : _results.getMatches()) {
      if (_results.getResultType().equals("RouteResult")) {
        RouteResult result = (RouteResult) _result;
        uniqueServiceAlerts.addAll(result.getServiceAlerts());

      } else if (_results.getResultType().equals("StopResult")) {
        StopResult result = (StopResult) _result;
        // add stop level service alerts
        uniqueServiceAlerts.addAll(result.getStopServiceAlerts());
        // then add route level service alerts -- being careful to
        // display alerts even if buses aren't present
        for (RouteAtStop route : result.getAllRoutesPossible()) {
          uniqueServiceAlerts.addAll(route.getServiceAlerts());
        }
      }
    }

    return uniqueServiceAlerts;
  }

  public SearchResultCollection getResults() {
    return _results;
  }

  public boolean getResultsOriginatedFromGeocode() {
    return _resultsOriginatedFromGeocode;
  }

  public String getTitle() {
    if (_location != null && getQueryIsEmpty()) {
      return "";
    }
    if (_results.getMatches().size() == 1) {
      SearchResult result = _results.getMatches().get(0);
      if (_results.getResultType().equals("StopResult")) {
        StopResult stopResult = (StopResult)result;
        return ": Stop " + stopResult.getCode() + " " + stopResult.getName();
      } else if (_results.getResultType().equals("RouteResult")) {
        RouteResult routeResult = (RouteResult)result;
        return ": Route " + routeResult.getShortName();
      }
    }
    if (!getQueryIsEmpty()) {
      return ": " + _q;
    }
    return "";
  }

  public String getRouteFilterShortName() {
    Object[] routeBeans = _results.getRouteFilter().toArray();
    if (routeBeans.length > 0) {
      RouteBean routeBean = (RouteBean)routeBeans[0];
      return routeBean.getShortName();
    } else {
      return null;
    }
  }
  
  public String getUseAgencyId() {
    return _configurationService.getConfigurationValueAsString("display.useAgencyId", "false");
  }
  
  public String getShowAgencyNames() {
    return _configurationService.getConfigurationValueAsString("display.showAgencyNames", "false");
  }

  public String getFeedbackFormText() {
    return _configurationService.getConfigurationValueAsString("display.feedbackForm.text", null);
  }

  public String getFeedbackFormURL() {
    return _configurationService.getConfigurationValueAsString("display.feedbackForm.url", null);
  }
}
