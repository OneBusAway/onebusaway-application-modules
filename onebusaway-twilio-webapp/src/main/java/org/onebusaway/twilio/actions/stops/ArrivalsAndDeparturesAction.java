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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.impl.AgencyPresenter;
import org.onebusaway.presentation.impl.ArrivalAndDepartureComparator;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.onebusaway.twilio.impl.PhoneArrivalsAndDeparturesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@Results({
    @Result(name="bookmark-stop", type="chain",
      params={"From", "${phoneNumber}", "namespace", "/bookmarks", "actionName", "bookmark-stop"}),
    @Result(name="back", location="index", type="redirectAction", params={"From", "${phoneNumber}"}),
    @Result(name="repeat", location="arrivals-and-departures", type="chain")

})

public class ArrivalsAndDeparturesAction extends TwilioSupport {

  private static Logger _log = LoggerFactory.getLogger(ArrivalsAndDeparturesAction.class);
  
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
    Integer navState = (Integer)sessionMap.get("navState");
    if (navState == null) {
      navState = DISPLAY_DATA;
    }

    if (navState == DISPLAY_DATA) {
      // display results
      PhoneArrivalsAndDeparturesModel model = (PhoneArrivalsAndDeparturesModel) sessionMap.get("_model");
      StopsWithArrivalsAndDeparturesBean result = model.getResult();

      buildPredictedArrivals(result.getArrivalsAndDepartures());
      setNextAction("stops/arrivals-and-departures");
      _log.debug("setting navState, have stopIds=" + model.getStopIds());
      sessionMap.put("navState", new Integer(DO_ROUTING));
      sessionMap.put("stopIds", model.getStopIds());
      sessionMap.put("stops", result.getStops());
      return SUCCESS;
    }
    // navigation options after rendering a stop
    
    sessionMap.put("navState", new Integer(DISPLAY_DATA));
    _log.debug("getInput(): " + getInput());
    if (PREVIOUS_MENU_ITEM.equals(getInput())) {
      return "back";
    }	else if ("2".equals(getInput())) {
      setStops((List<StopBean>)sessionMap.get("stops"));
      setNextAction("bookmarks/bookmark-stop");
      return "bookmark-stop";
    }	else if ("8".equals(getInput())) {
      return "repeat";
    }
    // we didn't understand
    _log.debug("unexpected input=" + getInput());
    setNextAction("stops/index");
    return INPUT;
  }
  
  
  protected void buildPredictedArrivals(List<ArrivalAndDepartureBean> arrivals) {
    if (arrivals.isEmpty()) {
      addMessage(Messages.ARRIVAL_INFO_NO_SCHEDULED_ARRIVALS);
    }
    Collections.sort(arrivals, new ArrivalAndDepartureComparator());

    long now = System.currentTimeMillis();

    for (ArrivalAndDepartureBean adb : arrivals) {

      TripBean trip = adb.getTrip();
      RouteBean route = trip.getRoute();

      addMessage(Messages.ROUTE);

      String routeNumber = RoutePresenter.getNameForRoute(route);
      addText(_routeNumberPronunciation.modify(routeNumber));
      addText(", ");

      String headsign = trip.getTripHeadsign();
      if (headsign != null) {
        //addMessage(Messages.TO);

        String destination = _destinationPronunciation.modify(headsign);
        destination = destination.replaceAll("\\&", "and");
        addText(destination);
        addText(", ");
      }

      if (TransitDataConstants.STATUS_CANCELLED.equals(adb.getStatus())) {
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
    }

    addMessage(Messages.ARRIVAL_INFO_DISCLAIMER);

    List<AgencyBean> agencies = AgencyPresenter.getAgenciesForArrivalAndDepartures(arrivals);

    if (!agencies.isEmpty()) {
      addMessage(Messages.ARRIVAL_INFO_DATA_PROVIDED_BY);
      for (int i = 0; i < agencies.size(); i++) {
        AgencyBean agency = agencies.get(i);
        if (i == agencies.size() - 1 && agencies.size() > 1)
          addText(Messages.AND);
        addText(agency.getName());
        addText(",");
      }
    }
    addMessage(Messages.STOP_FOUND_BOOKMARK_THIS_LOCATION);
  }

}
