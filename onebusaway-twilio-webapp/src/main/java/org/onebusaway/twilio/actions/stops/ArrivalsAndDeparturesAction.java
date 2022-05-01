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
package org.onebusaway.twilio.actions.stops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.impl.AgencyPresenter;
import org.onebusaway.presentation.impl.ArrivalAndDepartureComparator;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.onebusaway.twilio.impl.PhoneArrivalsAndDeparturesModel;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@Results({
    @Result(name = "bookmark-stop", type = "chain", params = {
        "From", "${phoneNumber}", "namespace", "/bookmarks", "actionName",
        "bookmark-stop"}),
    @Result(name = "back", location = "index", type = "redirectAction", params = {
        "From", "${phoneNumber}"}),
    @Result(name = "repeat", location = "arrivals-and-departures", type = "chain"),
    @Result(name = "service-alerts", type = "chain", params = {
        "From", "${phoneNumber}", "namespace", "/alerts", "actionName",
        "service-alerts"})})
public class ArrivalsAndDeparturesAction extends TwilioSupport {

  private static Logger _log = LoggerFactory.getLogger(ArrivalsAndDeparturesAction.class);

  private static final String NO_ROUTE = "NO_ROUTE";

  private TextModification _routeNumberPronunciation;

  private TextModification _destinationPronunciation;

  private List<StopBean> _stops = new ArrayList<StopBean>();
  

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  @Autowired
  public void setRouteNumberPronunciation(
      @Qualifier("routeNumberPronunciation") TextModification routeNumberPronunciation) {
    _routeNumberPronunciation = routeNumberPronunciation;
  }

  public void setStops(List<StopBean> stops) {
    _stops.addAll(stops);
  }

  public List<StopBean> getStops() {
    return _stops;
  }

  @Override
  public String execute() throws Exception {
    _log.debug("in execute with input=" + this.getInput());
    Integer navState = (Integer) sessionMap.get("navState");
    if (navState == null) {
      navState = DISPLAY_DATA;
    }

    if (navState == DISPLAY_DATA) {
      return displayData();
    }
    // navigation options after rendering a stop
    return doRouting();

  }

  private String displayData() {
    PhoneArrivalsAndDeparturesModel model = (PhoneArrivalsAndDeparturesModel) sessionMap.get("_model");
    StopsWithArrivalsAndDeparturesBean result = model.getResult();

    buildPredictedArrivals(result.getArrivalsAndDepartures(), result.getStops());

    setNextAction("stops/arrivals-and-departures");
    _log.debug("setting navState, have stopIds=" + model.getStopIds());
    sessionMap.put("navState", new Integer(DO_ROUTING));
    sessionMap.put("stopIds", model.getStopIds());
    sessionMap.put("stops", result.getStops());
    return SUCCESS;
  }

  private String doRouting() {
    sessionMap.put("navState", new Integer(DISPLAY_DATA));
    _log.debug("getInput(): " + getInput());
    if (PREVIOUS_MENU_ITEM.equals(getInput())) {
      return "back";
    } else if ("2".equals(getInput())) {
      setStops((List<StopBean>) sessionMap.get("stops"));
      setNextAction("bookmarks/bookmark-stop");
      return "bookmark-stop";
    } else if (REPEAT_MENU_ITEM.equals(getInput())) {
      return "repeat";
    } 
    // we didn't understand
    _log.debug("unexpected input=" + getInput());
    setNextAction("stops/index");
    return INPUT;
  }

  protected void buildPredictedArrivals(List<ArrivalAndDepartureBean> arrivals, List<StopBean> list) {
    if (arrivals.isEmpty()) {
      addMessage(Messages.ARRIVAL_INFO_NO_SCHEDULED_ARRIVALS);
    }
    Collections.sort(arrivals, new ArrivalAndDepartureComparator());

    long now = SystemTime.currentTimeMillis();
    boolean hasAlerts = stopsHaveAlerts(list);
    
    for (ArrivalAndDepartureBean adb : arrivals) {

      TripBean trip = adb.getTrip();
      RouteBean route = trip.getRoute();

      addMessage(Messages.ROUTE);

      String routeNumber = RoutePresenter.getNameForRoute(route);
      addText(_routeNumberPronunciation.modify(routeNumber));
      addText(", ");

      String headsign = trip.getTripHeadsign();
      if (headsign != null) {
        // addMessage(Messages.TO);

        String destination = _destinationPronunciation.modify(headsign);
        destination = destination.replaceAll("\\&", "and");
        addText(destination);
        addText(", ");
      }

      if (TransitDataConstants.STATUS_LEGACY_CANCELLED.equalsIgnoreCase(adb.getStatus())) {
        addText("is currently not in service");
        continue;
      }
      
      long t = adb.computeBestDepartureTime();
      boolean isPrediction = adb.hasPredictedDepartureTime();

      int min = (int) ((t - now) / 1000 / 60);

      if (min < 0) {
        min = -min;
        if (min > 60) {
          String message = isPrediction ? Messages.PREDICTED_AT_PAST_DATE
              : Messages.SCHEDULED_AT_PAST_DATE;
          addMessage(message, new Date(t));
        } else {
          String message = isPrediction ? Messages.PREDICTED_IN_PAST
              : Messages.SCHEDULED_IN_PAST;
          addMessage(message, min);
        }
      } else {
        if (min > 60) {
          String message = isPrediction ? Messages.PREDICTED_AT_FUTURE_DATE
              : Messages.SCHEDULED_AT_FUTURE_DATE;
          addMessage(message, new Date(t));
        } else {
          String message = isPrediction ? Messages.PREDICTED_IN_FUTURE
              : Messages.SCHEDULED_IN_FUTURE;
          addMessage(message, min);
        }
      }

      if (TransitDataConstants.STATUS_REROUTE.equals(adb.getStatus())) {
        addText("but is currently on adverse weather re-route.");
      }
      addText(". ");
      if(!hasAlerts && adb.getSituations() != null && adb.getSituations().size() > 0){
        hasAlerts = true;
      }
    }
    
    //processServiceAlertMessages(buildStopRouteAlertsMap());

    
    if(hasAlerts){
      addText(getAlertPresentText());
    }
    addMessage(Messages.ARRIVAL_INFO_DISCLAIMER);

    List<AgencyBean> agencies = AgencyPresenter.getAgenciesForArrivalAndDepartures(arrivals);

    if (!agencies.isEmpty()) {
      addMessage(Messages.ARRIVAL_INFO_DATA_PROVIDED_BY);
      for (int i = 0; i < agencies.size(); i++) {
        AgencyBean agency = agencies.get(i);
        if (i == agencies.size() - 1 && agencies.size() > 1)
          addText(" " + Messages.AND + " ");
        addText(agency.getName());
        addText(",");
      }
    }
    addMessage(Messages.STOP_FOUND_BOOKMARK_THIS_LOCATION);
  }

  private Map<StopRouteKey, List<ServiceAlertBean>> buildStopRouteAlertsMap() {
    List<StopBean> stops = (List<StopBean>) sessionMap.get("stops");
    Map<StopRouteKey, List<ServiceAlertBean>> stopRouteAlertsMap = new TreeMap<StopRouteKey, List<ServiceAlertBean>>();

    if (stops != null) {
      for (StopBean stop : stops) {
        List<ServiceAlertBean> stopAlerts = getServiceAlertsForStop(stop.getId());

        if (stopAlerts.size() > 0) {
          stopRouteAlertsMap.put(new StopRouteKey(stop.getId(), NO_ROUTE),
              stopAlerts);
        } else {
          for (RouteBean route : stop.getRoutes()) {

            if (!stopRouteAlertsMap.containsKey(new StopRouteKey(stop.getId(),
                route.getId()))) {
              List<ServiceAlertBean> routeAlerts = getServiceAlertsForRoute(route.getId());
              if(routeAlerts.size() == 0){
                 routeAlerts = getServiceAlertsForStopRoute(stop.getId(), route.getId());
              }
              stopRouteAlertsMap.put(
                  new StopRouteKey(stop.getId(), route.getId()), routeAlerts);
            }
          }
        }
      }
    }
    return stopRouteAlertsMap;
  }

  private void processServiceAlertMessages(
      Map<StopRouteKey, List<ServiceAlertBean>> stopRouteAlertsMap) {
    
    List<String> routeIds = new ArrayList<String>();
    List<String> stopIds = new ArrayList<String>();
    
    if (!stopRouteAlertsMap.isEmpty()) {
      for (Map.Entry<StopRouteKey, List<ServiceAlertBean>> entry : stopRouteAlertsMap.entrySet()) {

        List<ServiceAlertBean> alerts = entry.getValue();
       
        for (ServiceAlertBean alert : alerts) {
          if(containsActiveAlert(alert, SystemTime.currentTimeMillis())){
            if (entry.getKey().hasRoute()) {
              routeIds.add(entry.getKey().getRoute()); 
              break;
            } else {
              stopIds.add(entry.getKey().getStop());
              break;
            }
          }          
        }
      }
    }
    
    addServiceAlertRouteText(routeIds);
    addServiceAlertStopText(stopIds);
  }
  
  private boolean stopsHaveAlerts(List<StopBean> stops){
	  for(StopBean stop: stops){
		  if(getServiceAlertsForStop(stop.getId()).size() > 0){
			return true;  
		  }
	  }
	  return false;
  }

  private List<ServiceAlertBean> getServiceAlertsForStop(String stopId) {
    SituationQueryBean query = new SituationQueryBean();
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);
    affects.setStopId(stopId);
    ListBean<ServiceAlertBean> alerts = _transitDataService.getServiceAlerts(query);

    if (alerts != null) {
      return alerts.getList();
    }

    return Collections.emptyList();
  }
  

  private List<ServiceAlertBean> getServiceAlertsForRoute(String routeId) {
    SituationQueryBean query = new SituationQueryBean();
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);
    affects.setRouteId(routeId);
    ListBean<ServiceAlertBean> alerts = _transitDataService.getServiceAlerts(query);

    if (alerts != null) {
      return alerts.getList();
    }

    return Collections.emptyList();
  }
  
  private List<ServiceAlertBean> getServiceAlertsForStopRoute(String stopId, String routeId) {
    SituationQueryBean query = new SituationQueryBean();
    SituationQueryBean.AffectsBean affects = new SituationQueryBean.AffectsBean();
    query.getAffects().add(affects);
    affects.setStopId(stopId);
    affects.setRouteId(routeId);
    ListBean<ServiceAlertBean> alerts = _transitDataService.getServiceAlerts(query);

    if (alerts != null) {
      return alerts.getList();
    }

    return Collections.emptyList();
  }

  private void addServiceAlertRouteText(List<String> routeIds) {
    if(routeIds.size() > 1){
      addText("Routes ");
      for(String routeId : routeIds){
        addText(" ");
        addText(_routeNumberPronunciation.modify(routeId));
        addText(",");
      }
      addText(getAlertPresentText());
    } 
    else if(routeIds.size() > 0){
      addText("Route ");
      addText(_routeNumberPronunciation.modify(routeIds.get(0)));
      addMessage(getAlertPresentText());
    } 
  }

  private void addServiceAlertStopText(List<String> stopIds) {
    if(stopIds.size() > 1){
      addText("Stops ");
      for(String stopId : stopIds){
        addText(" ");
        addText(stopId);
        addText(",");
      }
      addText(getAlertPresentText());
    } 
    else if(stopIds.size() > 0){
      addText("Stop ");
      addText(stopIds.get(0));
      addText(getAlertPresentText());
    } 
  }

  private String getAlertPresentText() {
    final String ALERT_TEXT_KEY = "ivr.alert.txt";
    if (System.getProperties().containsKey(ALERT_TEXT_KEY)) {
      return System.getProperty(ALERT_TEXT_KEY);
    }
    return "";
  }
  
  private boolean containsActiveAlert(ServiceAlertBean serviceAlert, long time) {

    if (time == -1 || serviceAlert.getPublicationWindows() == null
        || serviceAlert.getPublicationWindows().size() == 0)
      return true;
    for (TimeRangeBean publicationWindow : serviceAlert.getPublicationWindows()) {
      if ((publicationWindow.getFrom() <= time)
          && (publicationWindow.getTo() >= time)) {
        return true;
      }
    }
    return false;
  }

  private class StopRouteKey implements Comparable<StopRouteKey> {
    private String _route;
    private String _stop;
    private String _agency;
    private boolean _hasRoute = true;

    public StopRouteKey(String stop, String route) {
      AgencyAndId stopAid = AgencyAndId.convertFromString(stop);
      AgencyAndId routeAid = AgencyAndId.convertFromString(route);
      _agency = stopAid.getAgencyId();
      _stop = stopAid.getId();
      if (route.equals(NO_ROUTE)) {
        _hasRoute = false;
        _route = route;
      } else {
        _route = routeAid.getId();
      }
    }

    public String getKey() {
      return _agency + "_" + _stop + "_" + _route;
    }

    public String getRoute() {
      return _route;
    }

    public String getStop() {
      return _stop;
    }

    public String getAgency() {
      return _agency;
    }

    public boolean hasRoute() {
      return _hasRoute;
    }

    @Override
    public int hashCode() {
      return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof StopRouteKey))
        return false;
      StopRouteKey other = (StopRouteKey) obj;
      return this.getKey().equals(other.getKey());
    }

    @Override
    public int compareTo(StopRouteKey other) {
      return this.getKey().compareTo(other.getKey());
    }
  }

}