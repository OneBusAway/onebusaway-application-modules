/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.service.NotificationService;
import org.onebusaway.admin.service.impl.TwitterServiceImpl;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.Reasons;
import org.onebusaway.presentation.bundles.service_alerts.Severity;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;

@ParentPackage("onebusaway-admin-webapp-default")
@Results({
  @Result(type = "redirectAction", name = "refreshResult", params = {
      "actionName", "service-alerts", "parse", "true"})
      })

public class ServiceAlertEditAction extends ActionSupport implements
    ModelDriven<ServiceAlertBean> {
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertEditAction.class);
  
  private ServiceAlertBean _model;
  
  private List<AgencyWithCoverageBean> _agencies;
  
  private String _alertId;
  
  private String _agencyId;
  
  private boolean _newServiceAlert = false;
  
  private TransitDataService _transitDataService;

  @Override
  public ServiceAlertBean getModel() {
    if (_model == null) {
      _model = new ServiceAlertBean();
      _model.setReason("Initial setup of empty ServiceAlert");
    }
    return _model;
  }

  public void setModel(ServiceAlertBean model) {
    this._model = model;
  }

  public List<AgencyWithCoverageBean> getAgencies() {
    return _agencies;
  }

  public String getAlertId() {
    return _alertId;
  }

  public void setAlertId(String alertId) {
    this._alertId = alertId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public void setAgencyId(String agencyId) {
    this._agencyId = agencyId;
  }

  public boolean isNewServiceAlert() {
    return _newServiceAlert;
  }

  public void setNewServiceAlert(boolean newServiceAlert) {
    this._newServiceAlert = newServiceAlert;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setSummary(String summary) {
    List<NaturalLanguageStringBean> summaries = _model.getSummaries();
    if (summaries == null) {
      summaries = new ArrayList<NaturalLanguageStringBean>();
      _model.setSummaries(summaries);
    }
    if (summaries.isEmpty()) {
      summaries.add(new NaturalLanguageStringBean());
    }
    NaturalLanguageStringBean nls = summaries.get(0);
    nls.setValue(summary);
    nls.setLang(Locale.getDefault().getLanguage());
  }

  public String getSummary() {
    List<NaturalLanguageStringBean> summaries = _model.getSummaries();
    if (summaries == null || summaries.isEmpty()) {
      return null;
    }
    NaturalLanguageStringBean nls = summaries.get(0);
    return nls.getValue();
  }

  public void setDescription(String description) {
    List<NaturalLanguageStringBean> descriptions = _model.getDescriptions();
    if (descriptions == null) {
      descriptions = new ArrayList<NaturalLanguageStringBean>();
      _model.setDescriptions(descriptions);
    }
    if (descriptions.isEmpty()) {
      descriptions.add(new NaturalLanguageStringBean());
    }
    NaturalLanguageStringBean nls = descriptions.get(0);
    nls.setValue(description);
    nls.setLang(Locale.getDefault().getLanguage());
  }

  public String getDescription() {
    List<NaturalLanguageStringBean> descriptions = _model.getDescriptions();
    if (descriptions == null || descriptions.isEmpty()) {
      return null;
    }
    NaturalLanguageStringBean nls = descriptions.get(0);
    return nls.getValue();
  }
  
  public String getStartDate() {
	  List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
	  if(publicationWindows == null || publicationWindows.isEmpty() || publicationWindows.get(0).getFrom() == 0){
		  return null;
	  }
	  Date date = new Date(publicationWindows.get(0).getFrom());
	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	  return sdf.format(date);
  }

  public void setStartDate(Date startDate) {
	  List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
	  if (publicationWindows == null) {
		  publicationWindows = new ArrayList<TimeRangeBean>();
	      _model.setPublicationWindows(publicationWindows);
	  }
	  
	  if (publicationWindows.isEmpty()) {
		  publicationWindows.add(new TimeRangeBean());
	  }
	  
	  TimeRangeBean timeRangeBean = publicationWindows.get(0);
	  
	  if(startDate != null){
		  timeRangeBean.setFrom(startDate.getTime());
	  }
	  else{
		  timeRangeBean.setFrom(0); 
	  }
	 
  }
  
  public String getEndDate() {
	  List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
	  if(publicationWindows == null || publicationWindows.isEmpty() || publicationWindows.get(0).getTo() == 0){
		  return null;
	  }
	  Date date = new Date(publicationWindows.get(0).getTo());
	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	  return sdf.format(date);
  }

  public void setEndDate(Date endDate) {
	  List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
	  if (publicationWindows == null) {
		  publicationWindows = new ArrayList<TimeRangeBean>();
	      _model.setPublicationWindows(publicationWindows);
	  }
	  
	  if (publicationWindows.isEmpty()) {
		  publicationWindows.add(new TimeRangeBean());
	  }
	  
	  TimeRangeBean timeRangeBean = publicationWindows.get(0);
	  
	  if(endDate != null){
		  timeRangeBean.setTo(endDate.getTime());
	  }
	  else{
		  timeRangeBean.setTo(0); 
	  }
	 
  }


  @Override
  public String execute() {
    try {
      if (_alertId != null && !_alertId.trim().isEmpty())
        _model = _transitDataService.getServiceAlertForId(_alertId);
    } catch (RuntimeException e) {
      _log.error("Unable to retrieve Service Alert", e);
      throw e;
    }
    
    if (_agencyId == null && _alertId != null) {
      int index = _alertId.indexOf('_');
      if (index != -1)
        _agencyId = _alertId.substring(0, index);
    }

    try {
      _agencies = _transitDataService.getAgenciesWithCoverage();
    } catch (Throwable t) {
      _log.error("unable to retrieve agencies with coverage", t);
      _log.error("issue connecting to TDS -- check your configuration in data-sources.xml");
      throw new RuntimeException("Check your onebusaway-nyc-transit-data-federation-webapp configuration", t);
    }
    return "SUCCESS";
  }
  
  public Map<String, String> getReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, Reasons.class);
  }

  public Map<String, String> getSeverityValues() {
    return ResourceBundleSupport.getLocaleMap(this, Severity.class);
  }

  public String deleteAlert() {
    try {
      _transitDataService.removeServiceAlert(_alertId);
    } catch (RuntimeException e) {
      _log.error("Error deleting service alert", e);
      throw e;
    }
    return "refreshResult";
  }

  private NotificationService _notificationService;
  @Autowired(required = false)
  public void setNotificationService(NotificationService notificationService) {
    _notificationService = notificationService;
  }

  public String tweetAlert() {
    _log.info("Tweet! " + _alertId);

    if (_notificationService != null) {
      String response = null;
      try {
        _log.info("calling tweet....");
        response = _notificationService.tweet(TwitterServiceImpl.toTweet(_transitDataService.getServiceAlertForId(_alertId)));
        _log.info("tweet succeeded with response=" + response);
      } catch (IOException ioe) {
        _log.error("tweet failed!", ioe);
        return "error";
      }
      _log.info("tweet response=" + response);
    } else {
      _log.info("no notification service provided");
    }
    return "refreshResult";
  }

  public String everbridgeAlert() {
    _log.info("everbridge! " + _alertId);
    return "refreshResult";
  }
  
}
