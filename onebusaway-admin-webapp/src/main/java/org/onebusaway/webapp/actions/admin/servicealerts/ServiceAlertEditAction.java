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
import java.util.*;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.service.NotificationService;
import org.onebusaway.admin.service.impl.TwitterServiceImpl;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.Reasons;
import org.onebusaway.presentation.bundles.service_alerts.Severity;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertRecordBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;

@ParentPackage("onebusaway-admin-webapp-default")
@Results({
  @Result(type = "redirectAction", name = "refreshResult", params = {
      "actionName", "service-alerts", "parse", "true"}),
        @Result(name="twitterResult", type="json",
                params={"root", "twitterResult"}),

})
@AllowedMethods({"deleteAlert", "tweetAlert"})
public class ServiceAlertEditAction extends OneBusAwayNYCAdminActionSupport implements
    ModelDriven<ServiceAlertBean> {
  private static Logger _log = LoggerFactory.getLogger(ServiceAlertEditAction.class);
  
  private ServiceAlertBean _model;
  
  private List<AgencyWithCoverageBean> _agencies;
  
  private List<ServiceAlertBean> _situationTemplatesByAgency;
  
  private String _alertId;
  
  private String _agencyId;
  
  private boolean _newServiceAlert = false;
  
  private boolean _favorite = false;
  
  private boolean _fromFavorite = false;
  
  private TransitDataService _transitDataService;

    private ConsoleServiceAlertsService _alerts;
    private String _endTime;
    private String _startTime;
    private Date _endDate;
    private Date _startDate;

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

  //used for the affected agencies list so that a global option is available
    public List<AgencyWithCoverageBean> getAffectedAgencies() {
        ArrayList<AgencyWithCoverageBean> resultList = new ArrayList<AgencyWithCoverageBean>();
        if (isAdminUser()) {
            AgencyWithCoverageBean globalAlert = new AgencyWithCoverageBean();
            globalAlert.setAgency(new AgencyBean());
            globalAlert.getAgency().setId("__ALL_OPERATORS__");
            globalAlert.getAgency().setName("Global Emergency Message");
            resultList.add(globalAlert);
        }
        resultList.addAll(_agencies);
        return resultList;
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
  
  public boolean isFavorite() {
    return _favorite;
  }

  public void setFavorite(boolean favorite) {
	  _favorite = favorite;
  }
  
  public boolean isFromFavorite() {
    return _fromFavorite;
  }

  public void setFromFavorite(boolean fromFavorite) {
	  _fromFavorite =fromFavorite;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

    @Autowired
    public void setAlertsService(ConsoleServiceAlertsService service) {
        _alerts = service;
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

  public List<ServiceAlertBean> getTemplateSummaries(){	  
	  if(_situationTemplatesByAgency == null || _situationTemplatesByAgency.isEmpty())
		  return new ArrayList<ServiceAlertBean>(0);
	  return _situationTemplatesByAgency;
  }

  public String getLink() {
      if (_model.getUrls() == null || _model.getUrls().isEmpty()) return "";
      return _model.getUrls().get(0).getValue();
  }

  public void setLink(String link) {
      if (_model.getUrls() == null) {
          _model.setUrls(new ArrayList<NaturalLanguageStringBean>());
      }
      if (_model.getUrls().get(0) == null) {
          _model.getUrls().set(0, new NaturalLanguageStringBean());
      }
      _model.getUrls().get(0).setValue(link);
  }

  @Override
  public String execute() {
	 try {
		  if (_alertId != null && !_alertId.trim().isEmpty()){
	    	  _model = _alerts.getServiceAlertForId(_alertId);
		      if(_agencyId == null){
		      	_agencyId = ServiceAlertsUtil.getAgencyFromAlertId(_alertId);
		      }
		  }
      } catch (RuntimeException e) {
        _log.error("Unable to retrieve Service Alert", e);
        throw e;
     }


     try {
	      _agencies = _transitDataService.getAgenciesWithCoverage();
	      _situationTemplatesByAgency = new ArrayList<ServiceAlertBean>();
		  for (int i=0; i<_agencies.size(); ++i) {
	        AgencyWithCoverageBean agency = _agencies.get(i);
	        String agencyId = agency.getAgency().getId();
	        ListBean<ServiceAlertRecordBean> result = _alerts.getAllServiceAlertRecordsForAgencyId(agencyId);
	        for(ServiceAlertRecordBean serviceAlertRecord : result.getList())
	        {
	        	if(Boolean.TRUE.equals(serviceAlertRecord.isCopy())){
	        		_situationTemplatesByAgency.add(serviceAlertRecord.getServiceAlertBean());
	        	}   		
	        }
	      }  
      
     } catch (Throwable t) {
	      _log.error("unable to retrieve agencies with coverage", t);
	      _log.error("issue connecting to TDS -- check your configuration in data-sources.xml");
	      throw new RuntimeException("Check your onebusaway-nyc-transit-data-federation-webapp configuration", t);
    }

    try {
        if (isFromFavorite()) {
            setStartDate(null);
            setEndDate(null);
            setStartTime(null);
            setEndTime(null);
        }
    } catch (java.text.ParseException e) {
	     //do something?
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
      _alerts.removeServiceAlert(AgencyAndId.convertFromString(_alertId));
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
        response = _notificationService.tweet(
        	    TwitterServiceImpl.toTweet(_alerts.getServiceAlertForId(_alertId),
                        _notificationService.getNotificationStrategy()));
        _log.info("tweet succeeded with response=" + response);
        _twitterResult = response;
      } catch (IOException ioe) {
        _log.error("tweet failed!", ioe);
        _twitterResult = "Exception: " + ioe.getClass().getName() + ":" + ioe.getMessage();
        return "twitterResult";
      } catch (Throwable t) {
        _log.error("error trying to tweet=", t);
        _twitterResult = "Exception: " + t.getClass().getName() + ":" + t.getMessage();
      }
      _log.info("tweet response=" + response);
    } else {
      _log.info("no notification service provided");
      _twitterResult = "Twitter not configured properly.  Contact your administrator";
    }
    return "twitterResult";
  }

  private String _twitterResult;
  public String getTwitterResult() { return _twitterResult; }

  public String everbridgeAlert() {
    _log.info("everbridge! " + _alertId);
    return "refreshResult";
  }
  
}
