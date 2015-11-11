/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.server.impl;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.onebusaway.admin.service.server.RssServiceAlertsService;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.service_alerts.EEffect;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TimeRange;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtimeConstants;

@Component
public class RssServiceAlertsSerivceImpl implements RssServiceAlertsService {

    private static Logger _log = LoggerFactory.getLogger(RssServiceAlertsSerivceImpl.class);

    private String _defaultAgencyId = null;
    private String _serviceStatusUrlString = null;
    private String _serviceAdvisoryUrlString = null;
    private String _alertSource = "default";
    private HttpClient _httpClient = new HttpClient();
    private SAXBuilder _builder = new SAXBuilder();
    private SimpleDateFormat _sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
    private ScheduledExecutorService _executor;
    private TransitDataService _transitDataService;
    private Map<String, String> _routeShortNameToRouteIdMap;
    private Map<String, ServiceAlertBean> _alertCache;
    private boolean _removeAgencyIds = true;
    private FeedMessage _feed = null;

    @Autowired
    public void setTransitDataService(TransitDataService tds) {
      _transitDataService = tds;
    }
    
    public void setDefaultAgencyId(String agencyId) {
      this._defaultAgencyId = agencyId;
    }
    
    public void setServiceStatusUrlString(String url) {
      _serviceStatusUrlString = url;
    }
    
    public void setServiceAdvisoryUrlString(String url) {
      _serviceAdvisoryUrlString = url;
    }
    
    public void setAlertSource(String source) {
      _alertSource = source;
    }
    
    public boolean isEnabled() {
      return _serviceStatusUrlString != null && _serviceAdvisoryUrlString != null;
    }

    public String getAgencyId() {
      if (_defaultAgencyId != null) return _defaultAgencyId;
      // not configured, default to the first agency
      return _transitDataService.getAgenciesWithCoverage().get(0).getAgency().getId();
    }
    
    @PostConstruct
    public void start() throws Exception {
        _executor = Executors.newSingleThreadScheduledExecutor();
        // re-build internal route cache
        _executor.scheduleAtFixedRate(new RefreshDataTask(), 0, 1, TimeUnit.HOURS);
        // poll feed
        _executor.scheduleAtFixedRate(new PollRssTask(), 0, 5, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void stop() throws IOException {
        if (_executor != null)
            _executor.shutdownNow();
    }

    @Override
    public FeedMessage getServlceAlertFeed() {
      return _feed;
    }
    
    protected List<ServiceAlertBean> pollServiceAdvisoryRssFeed() throws Exception {
      
        List<ServiceAlertBean> alerts = new ArrayList<ServiceAlertBean>();
        if (_serviceAdvisoryUrlString == null) return alerts;
        
        HttpMethod httpget = new GetMethod(_serviceAdvisoryUrlString);
        int response = _httpClient.executeMethod(httpget);
        if (response != HttpStatus.SC_OK) {
            throw new Exception("service status poll failed, returned status code: " + response);
        }

        Document doc = _builder.build(httpget.getResponseBodyAsStream());

        List<Element> elements = doc.getRootElement().getChild("channel").getChildren("item");
        String language = doc.getRootElement().getChild("channel").getChildText("language");
        if(language == null)
            language = "en-us";  //they don't send language for this feed currently, perhaps they'll start?
        for(Element itemElement : elements){
            String title = itemElement.getChild("title").getValue();
            String link = itemElement.getChild("link").getValue();
            String description = itemElement.getChild("description").getValue();
            String pubDateString = itemElement.getChild("pubDate").getValue();
            String guid = itemElement.getChild("guid").getValue();
            Date pubDate = _sdf.parse(pubDateString);
            List<SituationAffectsBean> affectedRouteIds = getRouteIds(title);
            ServiceAlertBean serviceAlertBean = new ServiceAlertBean();
            serviceAlertBean.setSource(_alertSource);
            serviceAlertBean.setAllAffects(affectedRouteIds);
            serviceAlertBean.setSeverity(ESeverity.UNKNOWN);
            serviceAlertBean.setSummaries(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setReason(ServiceAlerts.ServiceAlert.Cause.UNKNOWN_CAUSE.name());
            SituationConsequenceBean situationConsequenceBean = new SituationConsequenceBean();
            situationConsequenceBean.setEffect(EEffect.SIGNIFICANT_DELAYS);
            serviceAlertBean.setConsequences(Arrays.asList(new SituationConsequenceBean[]{situationConsequenceBean}));
            serviceAlertBean.setCreationTime(pubDate.getTime());
            serviceAlertBean.setDescriptions(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setId(new AgencyAndId(getAgencyId(), guid).toString());
            serviceAlertBean.setUrls(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(link, language)}));
            alerts.add(serviceAlertBean);
        }
        return alerts;
    }

    protected List<ServiceAlertBean>  pollServiceStatusRssFeed() throws Exception {
        List<ServiceAlertBean> alerts = new ArrayList<ServiceAlertBean>();  
        if (_serviceStatusUrlString == null) return alerts;
        
        HttpMethod httpget = new GetMethod(_serviceStatusUrlString);
        int response = _httpClient.executeMethod(httpget);
        if (response != HttpStatus.SC_OK) {
            throw new Exception("service status poll failed, returned status code: " + response);
        }

        Document doc = _builder.build(httpget.getResponseBodyAsStream());


        List<Element> elements = doc.getRootElement().getChild("channel").getChildren("item");
        String language = doc.getRootElement().getChild("channel").getChildText("language");
        for(Element itemElement : elements){
            String title = itemElement.getChild("title").getValue();
            String link = itemElement.getChild("link").getValue();
            String description = itemElement.getChild("description").getValue();
            String pubDateString = itemElement.getChild("pubDate").getValue();
            String guid = itemElement.getChild("guid").getValue();
            Date pubDate = _sdf.parse(pubDateString);
            List<SituationAffectsBean> affectedRouteIds = getRouteIds(title);
            ServiceAlertBean serviceAlertBean = new ServiceAlertBean();
            serviceAlertBean.setSource(_alertSource);
            serviceAlertBean.setAllAffects(affectedRouteIds);
            serviceAlertBean.setSeverity(ESeverity.UNKNOWN);
            serviceAlertBean.setSummaries(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setReason(ServiceAlerts.ServiceAlert.Cause.UNKNOWN_CAUSE.name());
            SituationConsequenceBean situationConsequenceBean = new SituationConsequenceBean();
            situationConsequenceBean.setEffect(EEffect.SIGNIFICANT_DELAYS);
            serviceAlertBean.setConsequences(Arrays.asList(new SituationConsequenceBean[]{situationConsequenceBean}));
            serviceAlertBean.setCreationTime(pubDate.getTime());
            serviceAlertBean.setDescriptions(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setId(new AgencyAndId(getAgencyId(), guid).toString());
            serviceAlertBean.setUrls(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(link, language)}));
            alerts.add(serviceAlertBean);
        }
        return alerts;
    }

    private List<SituationAffectsBean> getRouteIds(String description){
        String[] routeShortNames = description.split("\\:")[0].split("\\,");
        List<SituationAffectsBean> affectedRoutes = new ArrayList<SituationAffectsBean>();
        for(int i = 0; i < routeShortNames.length; i++) {
            String routeShortName = routeShortNames[i];
            routeShortName = routeShortName.toUpperCase().trim();
            String routeId = _routeShortNameToRouteIdMap.get(routeShortName);
            if(routeId != null){
                SituationAffectsBean situationAffectsBean = new SituationAffectsBean();
                situationAffectsBean.setAgencyId(getAgencyId());
                situationAffectsBean.setRouteId(routeId);
                affectedRoutes.add(situationAffectsBean);
            }else{
                _log.warn("No route found for route short name " + routeShortName);
            }
        }
        return affectedRoutes;
    }

    private class PollRssTask implements Runnable {


        @Override
        public void run() {
            try {
                if (!isEnabled()) {
                    return;
                }
              
                if(_routeShortNameToRouteIdMap == null)
                    return;

                ListBean<ServiceAlertBean> currentObaAlerts = _transitDataService.getAllServiceAlertsForAgencyId(getAgencyId());
                for(ServiceAlertBean serviceAlertBean : currentObaAlerts.getList()){
                    if(!_alertCache.keySet().contains(serviceAlertBean.getId())
                            && serviceAlertBean.getSource() != null
                            && serviceAlertBean.getSource().equals(_alertSource)){
                        _alertCache.put(serviceAlertBean.getId(), serviceAlertBean);
                    }
                }

                List<ServiceAlertBean> rssAlerts = new ArrayList<ServiceAlertBean>();
                try{
                    rssAlerts.addAll(pollServiceAdvisoryRssFeed());
                }catch (Exception e){
                    _log.warn(e.getMessage());
                    e.printStackTrace();
                }

                try{
                    rssAlerts.addAll(pollServiceStatusRssFeed());
                }catch (Exception e){
                    _log.warn(e.getMessage());
                    e.printStackTrace();
                }

                Map<String, ServiceAlertBean> currentRssAlertMap = new HashMap<String, ServiceAlertBean>();
                for(ServiceAlertBean alert : rssAlerts){
                    currentRssAlertMap.put(alert.getId(), alert);
                }

                Iterator<String> cachedAlertsGuidIter = _alertCache.keySet().iterator();
                //first, check for expired alerts and existing alerts that have been updated
                while(cachedAlertsGuidIter.hasNext()){
                    String guid = cachedAlertsGuidIter.next();
                    if(!currentRssAlertMap.keySet().contains(guid)){
                        _log.info("Removing expired alert with guid " + guid);
                        cachedAlertsGuidIter.remove();
                    }else{
                        ServiceAlertBean currentAlert = _alertCache.get(guid);
                        ServiceAlertBean rssAlert = currentRssAlertMap.get(guid);
                        if(rssAlert.getCreationTime() > currentAlert.getCreationTime()) {
                            _log.info("Updating alert with guid " + guid);
                            _alertCache.put(guid, rssAlert);
                        }
                    }
                }

                //now create alerts for any new guids on the RSS feed
                for(String currentRssGuid : currentRssAlertMap.keySet()){
                    if(!_alertCache.keySet().contains(currentRssGuid)){
                        _log.info("Creating alert with guid " + currentRssGuid);
                        _alertCache.put(currentRssGuid, currentRssAlertMap.get(currentRssGuid));
                    }
                }
                
                updateAlerts(_alertCache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        
        
        private void updateAlerts(Map<String, ServiceAlertBean> wmataAlertCache) {
          FeedMessage.Builder feed = FeedMessage.newBuilder();
          FeedHeader.Builder header = feed.getHeaderBuilder();
          header.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
          fillFeedMessage(feed, wmataAlertCache);
          _feed = feed.build();
        }

        // this code borrowed from AlertsForAgencyAction
        private void fillFeedMessage(FeedMessage.Builder feedEntity,
            Map<String, ServiceAlertBean> wmataAlertCache) {
          
          List<ServiceAlertBean> alerts = new ArrayList<ServiceAlertBean>();
          for (Entry<String, ServiceAlertBean> beanEntry: wmataAlertCache.entrySet()) {
            alerts.add(beanEntry.getValue());
          }
          ListBean<ServiceAlertBean> alertsBean = new ListBean<ServiceAlertBean>();
          alertsBean.setList(alerts);
          fillAlert(feedEntity, alertsBean);
        }

        private void fillAlert(FeedMessage.Builder feed, ListBean<ServiceAlertBean> alerts) {
          for (ServiceAlertBean serviceAlert : alerts.getList()) {
            FeedEntity.Builder entity = feed.addEntityBuilder();
            entity.setId(Integer.toString(feed.getEntityCount()));
            Alert.Builder alert = entity.getAlertBuilder();

            fillTranslations(serviceAlert.getSummaries(),
                alert.getHeaderTextBuilder());
            fillTranslations(serviceAlert.getDescriptions(),
                alert.getDescriptionTextBuilder());

            if (serviceAlert.getActiveWindows() != null) {
              for (TimeRangeBean range : serviceAlert.getActiveWindows()) {
                TimeRange.Builder timeRange = alert.addActivePeriodBuilder();
                if (range.getFrom() != 0) {
                  timeRange.setStart(range.getFrom() / 1000);
                }
                if (range.getTo() != 0) {
                  timeRange.setEnd(range.getTo() / 1000);
                }
              }
            }

            if (serviceAlert.getAllAffects() != null) {
              for (SituationAffectsBean affects : serviceAlert.getAllAffects()) {
                EntitySelector.Builder entitySelector = alert.addInformedEntityBuilder();
                if (affects.getAgencyId() != null) {
                  entitySelector.setAgencyId(affects.getAgencyId());
                }
                if (affects.getRouteId() != null) {
                  entitySelector.setRouteId(normalizeId(affects.getRouteId()));
                }
                if (affects.getTripId() != null) {
                  TripDescriptor.Builder trip = entitySelector.getTripBuilder();
                  trip.setTripId(normalizeId(affects.getTripId()));
                  entitySelector.setTrip(trip);
                }
                if (affects.getStopId() != null) {
                  entitySelector.setStopId(normalizeId(affects.getStopId()));
                }
              }
            }
          }
        }
    }

    private void fillTranslations(List<NaturalLanguageStringBean> input,
        TranslatedString.Builder output) {
      for (NaturalLanguageStringBean nls : input) {
        Translation.Builder translation = output.addTranslationBuilder();
        translation.setText(nls.getValue());
        if (nls.getLang() != null) {
          translation.setLanguage(nls.getLang());
        }
      }
    }
    
    protected String normalizeId(String id) {
      if (_removeAgencyIds) {
        int index = id.indexOf('_');
        if (index != -1) {
          id = id.substring(index + 1);
        }
      }
      return id;
    }
    
    private class RefreshDataTask implements Runnable {

        @Override
        public void run() {
            try {
                ListBean<RouteBean> routes =  _transitDataService.getRoutesForAgencyId(getAgencyId());
                Map<String, String> mutableRouteMap = new HashMap<String, String>();
                for(RouteBean route : routes.getList()){
                    mutableRouteMap.put(route.getShortName().toUpperCase(), route.getId());
                }
                _routeShortNameToRouteIdMap = Collections.unmodifiableMap(mutableRouteMap);
                _alertCache = new HashMap<String, ServiceAlertBean>();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
