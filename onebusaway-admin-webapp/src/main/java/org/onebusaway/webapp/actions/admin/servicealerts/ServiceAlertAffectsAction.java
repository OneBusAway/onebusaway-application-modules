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
package org.onebusaway.webapp.actions.admin.servicealerts;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@InterceptorRefs({
  @InterceptorRef(value = "store", params = {"operationMode", "STORE"}),
  @InterceptorRef("onebusaway-webapp-stack")
})
@Results({
  @Result(type = "redirectAction", name="input", params = {"actionName", "service-alert", "id", "${id}"}),
  @Result(type = "redirectAction", params = {
    "actionName", "service-alert", "id", "${id}", "parse", "true"})})
@AllowedMethods({"update", "delete"})
public class ServiceAlertAffectsAction extends ActionSupport implements
    ModelDriven<SituationAffectsBean> {

  private static final long serialVersionUID = 1L;
  
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsAction.class);

  private static final String AGENCY_AND_ID_REGEX = "^[a-zA-Z0-9]{1,3}_[a-zA-Z0-9]+";

  private TransitDataService _transitDataService;

  private ConsoleServiceAlertsService _alerts;

  private SituationAffectsBean _model = new SituationAffectsBean();

  private String _id;

  private int _index = -1;

  private Set<String> agencies = new HashSet<String>();

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setAlertsService(ConsoleServiceAlertsService service) {
    _alerts = service;
  }

  @Override
  public SituationAffectsBean getModel() {
    return _model;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setIndex(int index) {
    _index = index;
  }

  public int getIndex() {
    return _index;
  }

  public String update() throws IOException, JSONException {

    if (_id == null || _index == -1) {
      return INPUT;
    }

    ServiceAlertBean alert = null;
    try {
      alert = _alerts.getServiceAlertForId(_id);
    } catch (RuntimeException e) {
      _log.error("Error retrieving Service Alerts", e);
      throw e;
    }
   
    if (alert == null) {
      return INPUT;
    }

    List<SituationAffectsBean> allAffects = alert.getAllAffects();
    if (allAffects == null || _index >= allAffects.size()) {
      return INPUT;
    }

    _model.setAgencyId(string(_model.getAgencyId()));
    _model.setRouteId(string(_model.getRouteId()));
    _model.setDirectionId(string(_model.getDirectionId()));
    _model.setTripId(string(_model.getTripId()));
    _model.setStopId(string(_model.getStopId()));
    
    allAffects.set(_index, _model);
    
    try {
      _alerts.updateServiceAlert(_model.getAgencyId(), alert);
    } catch (RuntimeException e) {
      _log.error("Error updating Service Alert Affects clause", e);
      throw e;
    }

    return SUCCESS;
  }

  public String delete() {

    if (_id == null || _index == -1) {
      return INPUT;
    }

    ServiceAlertBean alert = null;
    
    try {
      alert = _alerts.getServiceAlertForId(_id);
    } catch (RuntimeException e) {
      _log.error("Error retrieving Service Alert", e);
      throw e;
    }
    
    if (alert == null) {
      return INPUT;
    }

    List<SituationAffectsBean> allAffects = alert.getAllAffects();
    if (allAffects == null || _index >= allAffects.size()) {
      return INPUT;
    }

    allAffects.remove(_index);
    
    try {
      _alerts.updateServiceAlert(_model.getAgencyId(), alert);
    } catch (RuntimeException e) {
      _log.error("Error removing Service Alert Affects clause", e);
      throw e;
    }

    return SUCCESS;
  }

  public void validate() {
    String id = _model.getAgencyId();
    if (id != null && id.length() > 0) {
      if (!isValidAgency(id)) {
        addFieldError("agencyId_0", "Error on agency id: agency \"" + id + "\" was not found");
      }
    }

    id = _model.getRouteId();
    if (id != null && id.length() > 0) {
      if (!id.matches(AGENCY_AND_ID_REGEX)) {
        addFieldError("routeId_0", "Error on route id format, must be <agency id>_<route id>");
      } else if (!isValidAgency(id.substring(0,id.indexOf('_'))) ) {
        addFieldError("routeId_0", "Error on agency portion of route id: \"" + id.substring(0,id.indexOf('_')) + "\" is not a valid agency id");
      } else {
          try {
            RouteBean routeBean = _transitDataService.getRouteForId(id);
            if (routeBean == null) {
              addFieldError("routeId_0", "Error on route id: route \"" + id + "\" was not found");
            }
          } catch(Exception ex) {
            addFieldError("routeId_0", "Error on route id: route \"" + id + "\" was not found");
          }
      }
    }

    id = _model.getDirectionId();
    if (id != null && id.length() > 0) {
      if (_model.getTripId() == null || _model.getTripId().length() == 0) {
        addFieldError("directionId_0", "Error on direction id: not allowed unless a trip id has also been specified.");
      }
    }

    id = _model.getTripId();
    if (id != null && id.length() > 0) {
      if (!id.matches(AGENCY_AND_ID_REGEX)) {
        addFieldError("tripId_0", "Error on trip id format, must be <agency id>_<trip id>");
      } else if (!isValidAgency(id.substring(0,id.indexOf('_'))) ) {
        addFieldError("tripId_0", "Error on agency portion of trip id: \"" + id.substring(0,id.indexOf('_')) + "\" is not a valid agency id");
      } else {
        try {
          TripBean tripBean = _transitDataService.getTrip(id);
          if (tripBean == null) {
            addFieldError("tripId_0", "Error on trip id: trip \"" + id + "\" was not found");
          }
        } catch(Exception ex) {
          addFieldError("tripId_0", "Error on trip id: trip \"" + id + "\" was not found");
        }
      }
    }

    id = _model.getStopId();
    if (id != null && id.length() > 0) {
      if (!id.matches(AGENCY_AND_ID_REGEX)) {
        addFieldError("stopId_0", "Error on stop id format, must be <agency id>_<stop id>");
      } else if (!isValidAgency(id.substring(0,id.indexOf('_'))) ) {
        addFieldError("stopId_0", "Error on agency portion of stop id: \"" + id.substring(0,id.indexOf('_')) + "\" is not a valid agency id");
      } else {
        try {
          StopBean stopBean = _transitDataService.getStop(id);
          if (stopBean == null) {
            addFieldError("stopId_0", "Error on stop id: stop \"" + id + "\" was not found");
          }
        } catch(NoSuchStopServiceException ex) {
          addFieldError("stopId_0", "Error on stop id: stop \"" + id + "\" was not found");
        }
      }
    }
  }

  /****
   * 
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }

  private boolean isValidAgency(String agencyId) {
    if (agencies.size() == 0) {
      List<AgencyWithCoverageBean> agenciesWithCoverage = _transitDataService.getAgenciesWithCoverage();
      for (AgencyWithCoverageBean agencyBean : agenciesWithCoverage) {
        agencies.add(agencyBean.getAgency().getId());
      }
    }
    if (agencies.contains(agencyId)) {
      return true;
    }
    return false;
  }
}
