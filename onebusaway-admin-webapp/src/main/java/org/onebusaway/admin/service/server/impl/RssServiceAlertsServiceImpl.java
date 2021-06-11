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


import com.google.transit.realtime.GtfsRealtime.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.admin.service.server.IntegratingServiceAlertsService;
import org.onebusaway.alerts.service.ServiceAlerts;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.service_alerts.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RssServiceAlertsServiceImpl implements IntegratingServiceAlertsService {

    private static Logger _log = LoggerFactory.getLogger(RssServiceAlertsServiceImpl.class);

    private String _defaultAgencyId = null;
    private String _serviceStatusUrlString = null;
    private String _serviceAdvisoryUrlString = null;
    private String _alertSource = "default";
    private HttpClient _httpClient = new HttpClient();
    private SAXBuilder _builder = new SAXBuilder();
    private SimpleDateFormat _sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz");
    private ScheduledExecutorService _executor;
    private TransitDataService _transitDataService;
    private ConsoleServiceAlertsService _serviceAlertsService;
    private Map<String, String> _routeShortNameToRouteIdMap;
    // NOTE!  because we cache alerts here, we may hold on to alerts that have been manually deleted from db
    private Map<String, ServiceAlertBean> _alertCache;
    private boolean _removeAgencyIds = true;
    private FeedMessage _feed = null;
    private Locale _locale = null;

    @Autowired
    public void setTransitDataService(TransitDataService tds) {
      _transitDataService = tds;
    }

    @Autowired
    public void setConsoleServiceAlertsService(ConsoleServiceAlertsService service) {
        _serviceAlertsService = service;
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
    
    public void setLocale(Locale locale) {
      _locale = locale;
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
        if (_locale == null)
          _locale = Locale.getDefault();
        
        _executor = Executors.newSingleThreadScheduledExecutor();
        // re-build internal route cache
        _executor.scheduleAtFixedRate(new RefreshDataTask(), 0, 1, TimeUnit.HOURS);
        // poll feed after cache is built above
        _executor.scheduleAtFixedRate(new PollRssTask(), 1, 5, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void stop() throws IOException {
        if (_executor != null)
            _executor.shutdownNow();
    }

    @Override
    public FeedMessage getServiceAlertFeed() {
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
            language = _locale.getLanguage();  //they don't send language for this feed currently, perhaps they'll start?
        if(language.equals("en-us")) {
          // java prefers en
          language = _locale.getLanguage();
        }
        for(Element itemElement : elements){
            String title = itemElement.getChild("title").getValue();
            String link = "";
            if ( itemElement.getChild("link") != null)
                link = itemElement.getChild("link").getValue();
            String description = itemElement.getChild("description").getValue();
            String pubDateString = itemElement.getChild("pubDate").getValue();
            // guid may spread across multiple routes, differentiate based on title
            String guid = itemElement.getChild("guid").getValue() + "_" + title;
            Date pubDate = _sdf.parse(pubDateString);
            List<SituationAffectsBean> affectedRouteIds = getRouteIds(title);
            ServiceAlertBean serviceAlertBean = new ServiceAlertBean();
            serviceAlertBean.setSource(_alertSource+"_advisory");
            serviceAlertBean.setAllAffects(affectedRouteIds);
            serviceAlertBean.setSeverity(ESeverity.UNKNOWN);
            serviceAlertBean.setSummaries(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setReason(ServiceAlerts.ServiceAlert.Cause.UNKNOWN_CAUSE.name());
            SituationConsequenceBean situationConsequenceBean = new SituationConsequenceBean();
            situationConsequenceBean.setEffect(EEffect.SIGNIFICANT_DELAYS);
            serviceAlertBean.setConsequences(Arrays.asList(new SituationConsequenceBean[]{situationConsequenceBean}));
            serviceAlertBean.setCreationTime(pubDate.getTime());
            // don't set description if duplicate of summary
            //serviceAlertBean.setDescriptions(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setId(new AgencyAndId(getAgencyId(), guid).toString());
            if (StringUtils.isNotBlank(link))
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
        if (language == null) {
          language = _locale.getLanguage();
        }
        if (language.equals("en-us")) {
          language = _locale.getLanguage();
        }
        for(Element itemElement : elements){
            String title = itemElement.getChild("title").getValue();
            String link = "";
            if (itemElement.getChild("link") != null)
                link = itemElement.getChild("link").getValue();
            String description = itemElement.getChild("description").getValue();
            String pubDateString = itemElement.getChild("pubDate").getValue();
            String guid = itemElement.getChild("guid").getValue();
            Date pubDate = _sdf.parse(pubDateString);
            List<SituationAffectsBean> affectedRouteIds = getRouteIds(title);
            ServiceAlertBean serviceAlertBean = new ServiceAlertBean();
            serviceAlertBean.setSource(_alertSource+"_alert");
            serviceAlertBean.setAllAffects(affectedRouteIds);
            serviceAlertBean.setSeverity(ESeverity.UNKNOWN);
            serviceAlertBean.setSummaries(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setReason(ServiceAlerts.ServiceAlert.Cause.UNKNOWN_CAUSE.name());
            SituationConsequenceBean situationConsequenceBean = new SituationConsequenceBean();
            situationConsequenceBean.setEffect(EEffect.SIGNIFICANT_DELAYS);
            serviceAlertBean.setConsequences(Arrays.asList(new SituationConsequenceBean[]{situationConsequenceBean}));
            serviceAlertBean.setCreationTime(pubDate.getTime());
            // don't set description if duplicate of summary
            // serviceAlertBean.setDescriptions(Arrays.asList(new NaturalLanguageStringBean[]{new NaturalLanguageStringBean(description, language)}));
            serviceAlertBean.setId(new AgencyAndId(getAgencyId(), guid).toString());
            if (StringUtils.isNotBlank(link))
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
            long start = System.currentTimeMillis();
            List<ServiceAlertBean> toAdd = new ArrayList<>();
            List<AgencyAndId> toRemove = new ArrayList<>();
            List<ServiceAlertBean> toUpdate = new ArrayList<>();

            _log.info("PollRssTask.run enter");
            try {
                if (!isEnabled()) {
                    return;
                }

                if (_routeShortNameToRouteIdMap == null) {
                    _log.info("empty route map, exiting");
                    return;
                }

                ListBean<ServiceAlertBean> currentObaAlerts = _serviceAlertsService.getAllServiceAlertsForAgencyId(getAgencyId());
                for (ServiceAlertBean serviceAlertBean : currentObaAlerts.getList()) {
                    String linkText = "NuLl";
                    if (serviceAlertBean.getUrls() != null
                            && !serviceAlertBean.getUrls().isEmpty()
                            && serviceAlertBean.getUrls().get(0) != null) {
                        linkText = serviceAlertBean.getUrls().get(0).getValue();
                    }
                    _log.info("found existing sa=" + serviceAlertBean.getSummaries().get(0).getValue()
                            + " with source=" + serviceAlertBean.getSource()
                            + " and link=" + linkText);
                    if (!_alertCache.keySet().contains(serviceAlertBean.getId())
                            && serviceAlertBean.getSource() != null
                            /* WMATA_alert or WMATA_advisory */
                            && serviceAlertBean.getSource().contains(_alertSource + "_")) {
                        _log.info("new service alert=" + serviceAlertBean.getSummaries().get(0).getValue()
                            + " and link=" + linkText);
                        _alertCache.put(serviceAlertBean.getId(), serviceAlertBean);
                    }
                }

                List<ServiceAlertBean> rssAlerts = new ArrayList<ServiceAlertBean>();
                try {
                    rssAlerts.addAll(pollServiceAdvisoryRssFeed());
                } catch (Exception e) {
                    _log.warn(e.getMessage());
                    e.printStackTrace();
                }

                try {
                    rssAlerts.addAll(pollServiceStatusRssFeed());
                } catch (Exception e) {
                    _log.warn(e.getMessage());
                    e.printStackTrace();
                }

                Map<String, ServiceAlertBean> currentRssAlertMap = new HashMap<String, ServiceAlertBean>();
                for (ServiceAlertBean alert : rssAlerts) {
                    currentRssAlertMap.put(alert.getId(), alert);
                }

                Iterator<String> cachedAlertsGuidIter = _alertCache.keySet().iterator();
                //first, check for expired alerts and existing alerts that have been updated
                while (cachedAlertsGuidIter.hasNext()) {
                    String guid = cachedAlertsGuidIter.next();
                    if (!currentRssAlertMap.keySet().contains(guid)) {
                        _log.info("Removing expired alert with guid " + guid);
                        try {
                            toRemove.add(AgencyAndId.convertFromString(guid));
                        } catch (Exception any) {
                            _log.error("invalid guid=" + guid);
                        }
                        cachedAlertsGuidIter.remove();
                    } else {
                        ServiceAlertBean currentAlert = _alertCache.get(guid);
                        ServiceAlertBean rssAlert = currentRssAlertMap.get(guid);
                        if (rssAlert.getCreationTime() > currentAlert.getCreationTime()) {
                            _log.info("Updating alert with guid " + guid);
                            _alertCache.put(guid, rssAlert);

                            toUpdate.add(rssAlert);
                        }
                    }
                }

                //now create alerts for any new guids on the RSS feed
                for (String currentRssGuid : currentRssAlertMap.keySet()) {
                    if (!_alertCache.keySet().contains(currentRssGuid)) {
                        _log.info("Creating alert with guid " + currentRssGuid);

                        toAdd.add(currentRssAlertMap.get(currentRssGuid));
                        _alertCache.put(currentRssGuid, currentRssAlertMap.get(currentRssGuid));
                    }
                }

                _serviceAlertsService.removeServiceAlerts(toRemove);
                _serviceAlertsService.updateServiceAlerts(getAgencyId(), toUpdate);
                _serviceAlertsService.createServiceAlerts(getAgencyId(), toAdd);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                long end = System.currentTimeMillis();
                _log.info("PollRssTask.run exit in " + (end - start) + "ms");
            }
        }
    }
    private class RefreshDataTask implements Runnable {

        @Override
        public void run() {

            if (!isEnabled())
            {
                _log.info("exiting refresh cache, not enabled");
                return;
            }

            while(true){
                try {
                    ListBean<RouteBean> routes =  _transitDataService.getRoutesForAgencyId(getAgencyId());
                    Map<String, String> mutableRouteMap = new HashMap<String, String>();
                    for(RouteBean route : routes.getList()){
                      AgencyAndId routeId = AgencyAndId.convertFromString(route.getId());
                        mutableRouteMap.put(route.getShortName().toUpperCase(), routeId.toString());
                    }
                    _routeShortNameToRouteIdMap = Collections.unmodifiableMap(mutableRouteMap);
                    _alertCache = new HashMap<String, ServiceAlertBean>();
                    break;
                } catch (RemoteConnectFailureException rcfe) {
                    _log.warn("TDS hasn't started yet, will re-attempt to load routes in 30 seconds");
                    try {
                        Thread.sleep((30l * 1000l));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
