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
import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * integrate with Clever's Disruption Management API.  For each detour listed,
 * generate a service alert for the appropriate route.
 */
@Component
public class CleverServiceAlertsServiceImpl implements IntegratingServiceAlertsService {

  private static Logger _log = LoggerFactory.getLogger(CleverServiceAlertsServiceImpl.class);

  private static final String DEFAULT_AGENCY_ID = "71";

  private String _defaultAgencyId = null;
  private HttpClient _httpClient = new HttpClient();
  private GtfsRealtime.FeedMessage _feed = null;
  private ScheduledExecutorService _executor;
  private TransitDataService _transitDataService;
  private ConsoleServiceAlertsService _serviceAlertsService;

  private String _cleverAPIUrlString
          = null;

  private String agencyId = DEFAULT_AGENCY_ID;
  private String detourMessage = null;
  private Map<String, String> cleverRouteToGtfsRouteId = new HashMap<>();

  public String getAgencyId() {
    if (_defaultAgencyId != null) return _defaultAgencyId;
    // not configured, default to the first agency
    return _transitDataService.getAgenciesWithCoverage().get(0).getAgency().getId();
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

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

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
    bean.setId(detour.get("id").getAsString());
    if (detourMessage == null) {
      if (detour.has("desc")) {
        bean.setSummaries(toNLSBeanList(detour.get("desc").getAsString()));
      }
    } else {
      bean.setSummaries(toNLSBeanList(detourMessage));
    }
    if (detour.has("rtdirs")) {
      bean.setAllAffects(parseAffects(detour.get("rtdirs").getAsJsonArray()));
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
      bean.setActiveWindows(parseActiveWindow(startDate, endDate));
    }
    return bean;
  }

  private List<TimeRangeBean> parseActiveWindow(String startDateStr, String endDateStr) {
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
        bean.setRouteId(new AgencyAndId(agencyId, lookupRoute(rtdir.get("rt").getAsString())).toString());
        beans.add(bean);
      }
    }
    return beans;
  }

  private String lookupRoute(String rt) {
    if (this.cleverRouteToGtfsRouteId.containsKey(rt))
      return cleverRouteToGtfsRouteId.get(rt);
    // we don't have a remapping for that key so serve as is
    return rt;
  }

  private List<NaturalLanguageStringBean> toNLSBeanList(String text) {
    List<NaturalLanguageStringBean> beans = new ArrayList<>();
    NaturalLanguageStringBean bean = new NaturalLanguageStringBean();
    bean.setLang("EN");
    bean.setValue(text);
    beans.add(bean);
    return beans;
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

  private class PollDMTask implements Runnable {
    @Override
    public void run() {
      Set<AgencyAndId> currentAlerts = new HashSet<AgencyAndId>();
      Set<ServiceAlertRecord> toAdd = new HashSet<>();
      Set<ServiceAlertRecord> toUpdate = new HashSet<>();

      ArrayList<AgencyAndId> idsInCollection = new ArrayList<>();
      try {

        List<ServiceAlertBean> alertsCollection = pollCleverAPI();

      } catch (Exception any) {
        _log.error("clever polling failed with " + any, any);
      }
    }


    private AgencyAndId handleSingleAlert(ServiceAlertBean alert) {
      return null;
    }
  }

}
