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

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.util.SystemTime;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertRecordBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import org.springframework.remoting.RemoteConnectFailureException;

@Results({@Result(type = "redirectAction", name = "redirect", params = {
    "actionName", "service-alerts!agency", "agencyId", "${agencyId}", "parse",
    "true"})})
@AllowedMethods({"agency", "deleteAlert", "removeAllForAgency"})
public class ServiceAlertsAction extends OneBusAwayNYCAdminActionSupport {

  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsAction.class);
  private TransitDataService _transitDataService;
  private ConsoleServiceAlertsService _alerts;
  private String _agencyId;
  private String _alertId;
  private List<AgencyWithCoverageBean> _agencies;
  private List<ServiceAlertBean> _situations;
  private List<ServiceAlertRecordBean>[] _situationsByAgency;
  String summary = "";
  String description = "";
  String reason = "";
  String severity = "";
  String owningAgency = "";
  private boolean submit;
  private boolean clear;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setAlertsService(ConsoleServiceAlertsService service) {
    _alerts = service;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }
  
  public String get_alertId() {
    return _alertId;
  }

  public void set_alertId(String _alertId) {
    this._alertId = _alertId;
  }

  public List<AgencyWithCoverageBean> getAgencies() {
    return _agencies;
  }

  public List<ServiceAlertBean> getSituations() {
    return _situations;
  }

  public List<ServiceAlertRecordBean>[] getSituationsByAgency() {
    return _situationsByAgency;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public void setOwningAgency(String owningAgency) {
    this.owningAgency = owningAgency;
  }

  public void setSubmit(String submit) {
    this.submit = true;
  }
  
  public void setClear(String clear) {
    this.clear = true;
  }

  @SkipValidation
  @Override
  public String execute() {
    _log.info("ServiceAlerts.execute()");
    /*
    if (submit) {
        _log.info("Service Alert submitted");
        //doSubmit();
        //return "submitResult";
     }
     if (clear) {
       _log.info("Service Alert cleared");
        //doClear();
        //return "clearResult";
     }
     */
  
  	// Check that we have permission:
  	super.execute();
	
    try {
      _agencies = _transitDataService.getAgenciesWithCoverage();
      _situationsByAgency = new List[_agencies.size()];
      //for (AgencyWithCoverageBean agency : _agencies) {
      for (int i=0; i<_agencies.size(); ++i) {
        AgencyWithCoverageBean agency = _agencies.get(i);
        String agencyId = agency.getAgency().getId();
        ListBean<ServiceAlertRecordBean> result = _alerts.getAllServiceAlertRecordsForAgencyId(agencyId);

        //don't include alerts that are global for non-admin
        List<ServiceAlertRecordBean> serviceAlerts = new ArrayList<>();
        if (!isAdminUser()) {
          for (int j = 0; j < result.getList().size(); j++)
            if (result.getList().get(j).getServiceAlertBean() != null &&
                    result.getList().get(j).getServiceAlertBean().getAllAffects() != null) {
              for (int k = 0; k < result.getList().get(j).getServiceAlertBean().getAllAffects().size(); k++) {
                if (!"__ALL_OPERATORS__".equals(result.getList().get(j).getServiceAlertBean().getAllAffects().get(k).getAgencyId()))
                  serviceAlerts.add(result.getList().get(j));
              }
            }
            else serviceAlerts.add(result.getList().get(j));//just add it since there aren't any affects
        }
        else serviceAlerts = result.getList();

        _situationsByAgency[i] = serviceAlerts;
      }
      for (int i=0; i<_agencies.size(); ++i) {
        _log.info("Agency " + _agencies.get(i).getAgency().getId());
        List<ServiceAlertRecordBean> serviceAlerts = _situationsByAgency[i];
        for (ServiceAlertRecordBean serviceAlert : serviceAlerts) {
          _log.info("   Alert: " + serviceAlert.getServiceAlertBean().getSummaries().get(0));
        }
      }
    } catch (Throwable t) {
      _log.error("unable to retrieve agencies with coverage", t);
      _log.error("issue connecting to TDS -- check your configuration in data-sources.xml");
      throw new RemoteConnectFailureException("Check your onebusaway-nyc-transit-data-federation-webapp configuration", t);
    }
    return SUCCESS;
  }
  
  public boolean isActive(List<TimeRangeBean> windows){
	  if(windows != null && !windows.isEmpty()){
		  long now = SystemTime.currentTimeMillis();
		  TimeRangeBean timeRangeBean = windows.get(0);
		  if((timeRangeBean.getTo() > 0 &&  timeRangeBean.getTo() <= now) ||
				  (timeRangeBean.getFrom() > 0 &&  timeRangeBean.getFrom() >= now)){
			  return false;
		  }
	  }
	  return true;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "agencyId", message = "missing required agencyId field")})
  public String agency() {
    ListBean<ServiceAlertBean> result = _alerts.getAllServiceAlertsForAgencyId(_agencyId);
    _situations = result.getList();
    return SUCCESS;
  }

  public String deleteAlert() {
    String id = _alertId;
    try {
      _alerts.removeServiceAlert(AgencyAndId.convertFromString(_alertId));
    } catch (RuntimeException e) {
      _log.error("Error deleting service alert", e);
      throw e;
    }
    return "SUCCESS";
  }

  
  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "agencyId", message = "missing required agencyId field")})
  public String removeAllForAgency() {
    try {
      _alerts.removeAllServiceAlertsForAgencyId(_agencyId);
    } catch (RuntimeException e) {
      _log.error("Unable to remove all service alerts for agency", e);
      throw e;
    }
    return "redirect";
  }
  
}
