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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.Reasons;
import org.onebusaway.presentation.bundles.service_alerts.Severity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;
import com.thoughtworks.xstream.XStream;

@ParentPackage("onebusaway-admin-webapp-default")
@InterceptorRefs({
  @InterceptorRef("onebusaway-webapp-stack"),
  @InterceptorRef(value = "store", params = {"operationMode", "RETRIEVE"})
})
@Results({
    @Result(type = "redirectAction", name = "submitSuccess", params = {
        "actionName", "service-alerts", "id", "${id}", "parse", "true"}),
    @Result(type = "redirectAction", name = "deleteSuccess", params = {
        "actionName", "service-alerts!agency", "agencyId", "${agencyId}",
        "parse", "true"}),
    @Result(type = "redirectAction", name = "cancelResult", params = {
        "actionName", "service-alerts", "id", "${id}", "parse", "true"}),      
	@Result(type = "redirectAction", name = "addToFavoritesSuccess", params = {
        "actionName", "service-alerts", "id", "${id}", "parse", "true"})
		})
@AllowedMethods({"submit", "cancel", "addToFavorites", "addAffects", "delete"})
public class ServiceAlertAction extends ActionSupport implements
    ModelDriven<ServiceAlertBean>, Preparable {

  private static final long serialVersionUID = 1L;

  private static final String EDITOR_SOURCE = "console";
  
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertsAction.class);

  private ConsoleServiceAlertsService _alerts;

  private ServiceAlertBean _model;

  private String _agencyId;
  
  private String _alertId;

  private boolean _favorite;
  
  private boolean _newServiceAlert;
  
  private boolean _fromFavorite;
  
  private String _raw;

//  String summary = "";
//  String description = "";
//  String reason = "";
//  String severity = "";
//  String owningAgency = "";
  private boolean submit;
  private boolean cancel;
  private boolean addToFavorites;

  private String _endTime;
  private String _startTime;
  private Date _endDate;
  private Date _startDate;
  private String link;

  @Autowired
  public void setAlertsService(ConsoleServiceAlertsService service) {
    _alerts = service;
  }

  @Override
  public ServiceAlertBean getModel() {
    return _model;
  }

  public void setModel(ServiceAlertBean model) {
    this._model = model;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public String getAlertId() {
    return _alertId;
  }

  public void setAlertId(String alertId) {
    this._alertId = alertId;
  }
  
  public void setFavorite(boolean favorite) {
	  _favorite = favorite;
  }

  public boolean isFavorite() {
    return _favorite;
  }
  
  public void setNewServiceAlert(boolean newServiceAlert) {
	  _newServiceAlert = newServiceAlert;
  }

  public boolean isNewServiceAlert() {
    return _newServiceAlert;
  }
  
  public void setFromFavorite(boolean fromFavorite) {
	  _fromFavorite = fromFavorite;
  }

  public boolean isFromFavorite() {
    return _fromFavorite;
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

  public String getRaw() {
    return _raw;
  }

  public void setSubmit(String submit) {
    this.submit = true;
  }
  
  public void setCancel(String cancel) {
    this.cancel = true;
  }
  
  public boolean isAddToFavorites() {
	return addToFavorites;
  }

  public void setAddToFavorites(boolean addToFavorites) {
	this.addToFavorites = addToFavorites;
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

  public void setStartDate(Date startDate) throws java.text.ParseException {
    _startDate = startDate;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    Date time = null;
    if (_startTime != null && !_startTime.isEmpty()) {
      time = simpleDateFormat.parse(_startTime);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      setCombinedStartDate(((hour * 60)  + min) * 60 * 1000);
    }
    else setCombinedStartDate(0);
	 
  }

  public String getStartTime() {
    List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
    if(publicationWindows == null || publicationWindows.isEmpty() || publicationWindows.get(0).getFrom() == 0){
      return null;
    }
    Date date = new Date(publicationWindows.get(0).getFrom());
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    return sdf.format(date);
  }

  public void setStartTime(String startTime) throws java.text.ParseException {
  _startTime = startTime;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    Date time = null;
    if (_startTime != null && !_startTime.isEmpty()) {
      time = simpleDateFormat.parse(_startTime);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      setCombinedStartDate(((hour * 60)  + min) * 60 * 1000);
    }
    else setCombinedStartDate(0);
  }

  private void setCombinedStartDate(long startTime) {
    List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
    if (publicationWindows == null) {
      publicationWindows = new ArrayList<TimeRangeBean>();
      _model.setPublicationWindows(publicationWindows);
    }

    if (publicationWindows.isEmpty()) {
      publicationWindows.add(new TimeRangeBean());
    }

    TimeRangeBean timeRangeBean = publicationWindows.get(0);

    if (startTime == 0 && _startDate != null) {//just have date
      timeRangeBean.setFrom(_startDate.getTime());
    }
    else if(_startDate != null){//have both date and time
      timeRangeBean.setFrom(_startDate.getTime() + startTime);
    }
    else{
      timeRangeBean.setFrom(0);
    }

    //adjust times if they aren't in order (if there is an end date)
    if (timeRangeBean.getTo() > 0 && timeRangeBean.getTo() < timeRangeBean.getFrom()){
      timeRangeBean.setFrom(timeRangeBean.getTo());
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

  public void setEndDate(Date endDate) throws java.text.ParseException {
    _endDate = endDate;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    Date time = null;
    if (_endTime != null && !_endTime.isEmpty()) {
      time = simpleDateFormat.parse(_endTime);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      setCombinedEndDate(((hour * 60)  + min) * 60 * 1000);
    }
    else setCombinedEndDate(0);
	 
  }

  public String getEndTime() {
    List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
    if(publicationWindows == null || publicationWindows.isEmpty() || publicationWindows.get(0).getTo() == 0){
      return null;
    }
    Date date = new Date(publicationWindows.get(0).getTo());
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    return sdf.format(date);
  }

  public void setEndTime(String endTime) throws java.text.ParseException {
    _endTime = endTime;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    Date time = null;
    if (_endTime != null && !_endTime.isEmpty()) {
      time = simpleDateFormat.parse(_endTime);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time);
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      int min = cal.get(Calendar.MINUTE);
      setCombinedEndDate(((hour * 60)  + min) * 60 * 1000);
    }
    else setCombinedEndDate(0);
  }

  public void setCombinedEndDate(long endTime) {
    List<TimeRangeBean> publicationWindows = _model.getPublicationWindows();
    if (publicationWindows == null) {
      publicationWindows = new ArrayList<TimeRangeBean>();
      _model.setPublicationWindows(publicationWindows);
    }

    if (publicationWindows.isEmpty()) {
      publicationWindows.add(new TimeRangeBean());
    }

    TimeRangeBean timeRangeBean = publicationWindows.get(0);

    if (_endDate != null && endTime == 0) {
      timeRangeBean.setTo(_endDate.getTime());
    }
    else if(_endDate != null){
      timeRangeBean.setTo(_endDate.getTime() + endTime);
    }
    else{
      timeRangeBean.setTo(0);
    }

    //adjust times if they aren't in order (if there is an end date)
      if (timeRangeBean.getTo() > 0 && timeRangeBean.getTo() < timeRangeBean.getFrom()){
          timeRangeBean.setFrom(timeRangeBean.getTo());
      }
  }

  public void setLink(String link) {
    this.link = link;
  }

  private void setUrl(String link) {
    if (_model != null) {
      if (StringUtils.isBlank(link)) {
        _model.setUrls(null);
        return;
      }
      List<NaturalLanguageStringBean> urls = _model.getUrls();
      if (urls == null) {
        urls = new ArrayList<>();
        _model.setUrls(urls);
      }
      if (urls.isEmpty()) {
        urls.add(new NaturalLanguageStringBean());
      }
      urls.get(0).setValue(link);
    }
  }

  @Override
  public void prepare() throws Exception {
    try {
      if (_alertId != null && !_alertId.trim().isEmpty()) {
       	_model = _alerts.getServiceAlertForId(_alertId);
       	_model.setAllAffects(null);
      } else {
        _model = new ServiceAlertBean();
      }
    } catch (RuntimeException e) {
      _log.error("Unable to retrieve Service Alerts for agency Id", e);
      throw e;
    }
  }
  
  @Override
  public String execute() throws IOException, JSONException, ParseException {

    _log.info("ServiceAlerts.execute()");
    if (submit) {
        _log.info("Service Alert submitted");
        submit();
        return "submitSuccess";
     }
     if (cancel) {
       _log.info("Service Alert cleared");
        //doClear();
        return "cancelResult";
     }
     if (addToFavorites) {
         _log.info("Added to Favorites");
         addToFavorites();
         return "addToFavorites";
      }

  
    try {
      if (_model.getId() != null && !_model.getId().trim().isEmpty())
        _model = _alerts.getServiceAlertForId(_model.getId());
    } catch (RuntimeException e) {
      _log.error("Unable to retrieve Service Alerts for agency Id", e);
      throw e;
    }

    if (_agencyId == null && _model.getId() != null) {
    	_agencyId = ServiceAlertsUtil.getAgencyFromAlertId(_model.getId());
    }

    _raw = getRawSituationAsString();

    return SUCCESS;
  }

  public String submit() throws IOException, JSONException, ParseException {

    setUrl(this.link);
    _model.setReason(string(_model.getReason()));
    
    if(isNewServiceAlert() || isFromFavorite()){
    	_model.setId(null);
	}
    
    try { 
      if (_model.getId() == null || _model.getId().trim().isEmpty() ) {
    	 _model.setSource(EDITOR_SOURCE);
        _model.combineAffectsIds();
    	 _model = _alerts.createServiceAlert(_agencyId, _model);
      }
      else {
        // if we've edited a service alert from some other agency, we now own it
        _model.setSource(EDITOR_SOURCE);
        _model.combineAffectsIds();
        _alerts.updateServiceAlert(_agencyId, _model, isFavorite());
      }
    } catch (RuntimeException e) {
      _log.error("Error creating or updating Service Alert", e);
      throw e;
    }

    return "submitSuccess";
  }
  
  @Action("cancel")
  public String cancel() {
    _log.info("Service Alert cleared");
    //doClear();
    return "cancelResult"; 
  }
  
  public String addToFavorites() throws IOException, JSONException, ParseException {
    try{
    	// Set End Date in past to make inactive
    	Date endDate = new Date(20000000L);
	    setEndDate(endDate);
	    _alerts.copyServiceAlert(_agencyId, _model);
    } catch (RuntimeException e) {
        _log.error("Error creating Service Alert Favorite", e);
        throw e;
    }

    return "addToFavoritesSuccess";
  }

  public String addAffects() {
    if (_model.getId() == null) {
      return INPUT;
    }
    try {
      _model = _alerts.getServiceAlertForId(_model.getId());
  
      List<SituationAffectsBean> allAffects = _model.getAllAffects();
      if (allAffects == null) {
        allAffects = new ArrayList<SituationAffectsBean>();
        _model.setAllAffects(allAffects);
      }
      allAffects.add(new SituationAffectsBean());
      _alerts.updateServiceAlert(_agencyId, _model);
    } catch (RuntimeException e) {
      _log.error("Error updating Service Alert Affects clause", e);
      throw e;
    }
      
    return "submitSuccess";
  }

  public String delete() {

    try {
      if (_model.getId() != null) {
        _alerts.removeServiceAlert(new AgencyAndId(_agencyId, _model.getId()));
      }
    } catch (RuntimeException e) {
      _log.error("Error removing Service Alert", e);
      throw e;
    }

    return "deleteSuccess";
  }

  /****
   * 
   ****/

  public Map<String, String> getReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, Reasons.class);
  }

  public Map<String, String> getSeverityValues() {
    return ResourceBundleSupport.getLocaleMap(this, Severity.class);
  }

  /****
   * 
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }

  private String getRawSituationAsString() {

    XStream xstream = createXStream();
    return xstream.toXML(_model);
  }

  private ServiceAlertBean getStringAsRawSituation(String value)
      throws IOException, JSONException {

    if (value == null || value.trim().isEmpty())
      return new ServiceAlertBean();

    XStream xstream = createXStream();
    return (ServiceAlertBean) xstream.fromXML(value);
  }

  private XStream createXStream() {

    XStream xstream = new XStream();

    xstream.alias("serviceAlert", ServiceAlertBean.class);
    xstream.alias("affects", SituationAffectsBean.class);
    xstream.alias("consequence", SituationConsequenceBean.class);
    xstream.alias("nls", NaturalLanguageStringBean.class);

    return xstream;
  }
}
