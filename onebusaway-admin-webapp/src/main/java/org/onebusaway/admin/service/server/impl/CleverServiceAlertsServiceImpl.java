/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.transit.realtime.GtfsRealtime;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.onebusaway.admin.service.server.ConsoleServiceAlertsService;
import org.onebusaway.admin.service.server.IntegratingServiceAlertsService;
import org.onebusaway.alerts.impl.ServiceAlertLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.*;
import org.onebusaway.transit_data.services.TransitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * integrate with Clever's Disruption Management API.  For each detour listed,
 * generate a service alert for the appropriate route.
 */
@Component
public class CleverServiceAlertsServiceImpl implements IntegratingServiceAlertsService {

  private static Logger _log = LoggerFactory.getLogger(CleverServiceAlertsServiceImpl.class);

  private static final String DEFAULT_AGENCY_ID = "71";
  private static final String DEFAULT_SOURCE = "clever";

  private String _defaultAgencyId = DEFAULT_AGENCY_ID;
  private HttpClient _httpClient = new HttpClient();
  private GtfsRealtime.FeedMessage _feed = null;
  private ScheduledExecutorService _executor;
  private TransitDataService _transitDataService;
  private ConsoleServiceAlertsService _serviceAlertsService;

  private String _cleverAPIUrlString
          = null;

  private String source = DEFAULT_SOURCE;
  private String detourMessage = "Detour for";
  private Map<String, String> cleverRouteToGtfsRouteId = new HashMap<>();

  public String getAgencyId() {
    if (_defaultAgencyId != null) return _defaultAgencyId;
    // not configured, default to the first agency
    return _transitDataService.getAgenciesWithCoverage().get(0).getAgency().getId();
  }
  public String getSource() {
    return source;
  }

  @Autowired
  public void setTransitDataService(TransitDataService tds) {
    _transitDataService = tds;
  }

  @Autowired
  public void setConsoleServiceAlertsService(ConsoleServiceAlertsService service) {
    _serviceAlertsService = service;
  }

  public void setCleverAPIUrl(String urlWithApiKey) {
    this._cleverAPIUrlString = urlWithApiKey;
  }

  public void setDefaultAgencyId(String agencyId) {
    this._defaultAgencyId = agencyId;
  }

  public void setAlertSource(String source) { this.source = source; }

  public void setRouteMapping(Map<String, String> map) {
    this.cleverRouteToGtfsRouteId = map;
  }

  public void setDetourMessage(String msg) {
    this.detourMessage = msg;
  }
  @Override
  public GtfsRealtime.FeedMessage getServiceAlertFeed() {
    return _feed;
  }

  @PostConstruct
  public void start() throws Exception {
    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(new CleverServiceAlertsServiceImpl.PollDMTask(), 1, 5, TimeUnit.MINUTES);
  }

  @PreDestroy
  public void stop() throws IOException {
    if (_executor != null)
      _executor.shutdownNow();
  }

  List<ServiceAlertBean> pollCleverAPI() throws Exception {
    // reach out to Clever
    HttpMethod httpget = new GetMethod(_cleverAPIUrlString);
    int response = _httpClient.executeMethod(httpget);
    if (response != HttpStatus.SC_OK) {
      throw new Exception("service status poll failed, returned status code: " + response);
    }

    List<ServiceAlertBean> serviceAlerts = parseAlertsFromStream(httpget.getResponseBodyAsStream());
    return serviceAlerts;


  }

   List<ServiceAlertBean> parseAlertsFromStream(InputStream responseBodyAsStream) {
     JsonParser parser = new JsonParser();
     JsonObject response = null;

     try {
       response = (JsonObject)parser.parse(IOUtils.toString(responseBodyAsStream));
     } catch (IOException e) {
       _log.error("parse exception " + e, e);
     }

     if (response.has("bustime-response")) {
       JsonElement bustimeResponse = response.get("bustime-response");
       if (bustimeResponse.getAsJsonObject().has("dtrs")) {
         JsonArray detours = bustimeResponse.getAsJsonObject().get("dtrs").getAsJsonArray();
         return parseDetours(detours);
       }
     }


     return null;
  }

  private List<ServiceAlertBean> parseDetours(JsonArray detours) {
    if (detours == null) return null;
    List<ServiceAlertBean> alerts = new ArrayList<>();
    Iterator<JsonElement> iterator = detours.iterator();
    while (iterator.hasNext()) {
      ServiceAlertBean bean = parseDetour(iterator.next().getAsJsonObject());
      if (bean != null) {
        alerts.add(bean);
      }
    }
    return alerts;
  }

  private ServiceAlertBean parseDetour(JsonObject detour) {
    ServiceAlertBean bean = new ServiceAlertBean();
    if (detour.has("st") && "0".equals(detour.get("st").getAsString())) {
      // detour is cancelled, do not serve alert
      return null;
    }
    AgencyAndId aid = ServiceAlertLibrary.agencyAndId(getAgencyId(), detour.get("id").getAsString());
    bean.setId(aid.toString());
    bean.setSource(getSource());
    bean.setSeverity(ESeverity.UNKNOWN);
    if (detour.has("desc")) {
        bean.setDescriptions(toNLSBeanList(detour.get("desc").getAsString()));
    }
    if (detour.has("rtdirs")) {
      bean.setAllAffects(parseAffects(detour.get("rtdirs").getAsJsonArray()));
      if (detourMessage != null)
        bean.setSummaries(toNLSBeanList(parseSummaries(detour.get("rtdirs").getAsJsonArray())));
      else {//use feed desc if necessary
        bean.setSummaries(toNLSBeanList(detour.get("desc").getAsString()));
      }
    }
    String startDate = null;
    String endDate = null;
    if (detour.has("startdt")) {
      startDate = detour.get("startdt").getAsString();
    }
    if (detour.has("enddt")) {
      endDate = detour.get("enddt").getAsString();
    }
    if (startDate != null || endDate != null) {
      bean.setPublicationWindows(parsePublicationWindow(startDate, endDate));
    }
    return bean;
  }

  private List<TimeRangeBean> parsePublicationWindow(String startDateStr, String endDateStr) {
    List<TimeRangeBean> beans = new ArrayList<>();
    TimeRangeBean bean = new TimeRangeBean();
    beans.add(bean);
    if (startDateStr != null) {
      bean.setFrom(parseDate(startDateStr));
    }
    if (endDateStr != null) {
      bean.setTo(parseDate(endDateStr));
    }
    return beans;
  }

  private List<SituationAffectsBean> parseAffects(JsonArray rtdirs) {
    List<SituationAffectsBean> beans = new ArrayList<>();
    Iterator<JsonElement> iterator = rtdirs.iterator();
    while (iterator.hasNext()) {
      JsonObject rtdir = iterator.next().getAsJsonObject();
      if (rtdir.has("rt")) {
        SituationAffectsBean bean = new SituationAffectsBean();
        bean.setRouteId(new AgencyAndId(getAgencyId(), lookupRoute(rtdir.get("rt").getAsString())).toString());
        beans.add(bean);
      }
    }
    return beans;
  }

  private String parseSummaries(JsonArray rtdirs) {
    StringBuilder builder = new StringBuilder(detourMessage);
    List rts = new ArrayList();
    Iterator<JsonElement> iterator = rtdirs.iterator();
    while (iterator.hasNext()) {
      JsonObject rtdir = iterator.next().getAsJsonObject();
      if (rtdir.has("rt")) {
        String rtdirString = lookupRoute(rtdir.get("rt").getAsString());
        if (rtdir.has("dir")) {
          rtdirString += " " + rtdir.get("dir").getAsString();
        }
        rts.add(rtdirString);
      }
    }
    builder.append(" ");
    builder.append(String.join(", ", rts));
    return builder.toString();
  }

  private String lookupRoute(String rt) {
    if (this.cleverRouteToGtfsRouteId.containsKey(rt))
      return cleverRouteToGtfsRouteId.get(rt);
    // we don't have a remapping for that key so serve as is
    return rt;
  }

  private List<NaturalLanguageStringBean> toNLSBeanList(String text) {
    List<NaturalLanguageStringBean> beans = new ArrayList<>();
    beans.add(toNLSBean(text));
    return beans;
  }

  private NaturalLanguageStringBean toNLSBean(String text) {
    NaturalLanguageStringBean bean = new NaturalLanguageStringBean();
    bean.setLang(Locale.getDefault().getLanguage());
    bean.setValue(text);
    return bean;
  }

  public Long parseDate(String dateStr) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
    try {
      return sdf.parse(dateStr).getTime();
    } catch (ParseException e) {
      _log.error("invalid date " + dateStr, e);
    }
    return null;
  }

  void runPollDMTask() {
    List<ServiceAlertBean> toAdd = new ArrayList<>();
    List<ServiceAlertBean> toUpdate = new ArrayList<>();
    List<AgencyAndId> toRemove = new ArrayList<>();
    try {

      //current Clever feed alerts
      List<ServiceAlertBean> incomingCleverAlerts = pollCleverAPI();
      if (incomingCleverAlerts == null) {
        incomingCleverAlerts = new ArrayList<>();//don't let it stay null
      }
      //current oba alerts (for the agency for this feed)
      ListBean<ServiceAlertBean> currentObaAlerts = _serviceAlertsService.getAllServiceAlertsForAgencyId(getAgencyId());
      if (currentObaAlerts == null) {
        currentObaAlerts = new ListBean<>();//don't let it stay null
      }
      //all alerts in oba currently sourced to clever
      List<ServiceAlertBean> cleverAlertsInOba = currentObaAlerts.getList().stream()
              .filter(alert -> getSource().equals(alert.getSource()))
              .collect(Collectors.toList());

      //the id's of all the incoming clever alerts
      Set<String> incomingCleverAlertIds = incomingCleverAlerts.stream()
              .map(alert -> alert.getId())
              .collect(Collectors.toSet());
      //id's of all current oba alerts
      Set<String> alertIdsInOba = currentObaAlerts.getList().stream()
              .map(alert -> alert.getId())
              .collect(Collectors.toSet());
      //the id's of all the clever sourced alerts currently in OBA
      Set<String> cleverAlertIdsInOba = cleverAlertsInOba.stream()
              .map(alert -> alert.getId())
              .collect(Collectors.toSet());

      //add incoming clever alerts that have id's not already in oba
      toAdd.addAll(
              incomingCleverAlerts.stream()
              .filter(alert -> !(alertIdsInOba.contains(alert.getId())))
              .collect(Collectors.toList())
      );

      //update using incoming clever alerts if the id exists and it is still sourced to clever
      toUpdate.addAll(
              incomingCleverAlerts.stream()
              .filter(alert -> cleverAlertIdsInOba.contains(alert.getId()))
              .collect(Collectors.toList())
      );

      //keep only the orphaned clever alerts that are in oba by removing from this list all that are incoming
      cleverAlertIdsInOba.removeAll(incomingCleverAlertIds);//NOTE: cleverAlertIdsInOba List is compromised here

      toRemove.addAll(
              cleverAlertIdsInOba.stream()
              .map(id -> ServiceAlertLibrary.agencyAndIdAndId(getAgencyId(), id))
              .collect(Collectors.toList())
      );

      _serviceAlertsService.removeServiceAlerts(toRemove);
      _serviceAlertsService.updateServiceAlerts(getAgencyId(), toUpdate);
      _serviceAlertsService.createServiceAlerts(getAgencyId(), toAdd);

    } catch (Exception any) {
      _log.error("clever polling failed with " + any, any);
    }
  }

  private class PollDMTask implements Runnable {
    @Override
    public void run() {
      if (_cleverAPIUrlString != null) {
        runPollDMTask();
      }
    }
  }

}
