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
package org.onebusaway.phone.templates.stops;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onebusaway.phone.impl.PhoneArrivalsAndDeparturesModel;
import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.client.RoutePresenter;
import org.onebusaway.presentation.impl.AgencyPresenter;
import org.onebusaway.presentation.impl.ArrivalAndDepartureComparator;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/stop/arrivalsAndDepartures")
public class ArrivalsAndDeparturesTemplate extends AbstractAgiTemplate {

  private TextModification _routeNumberPronunciation;

  private TextModification _destinationPronunciation;

  public ArrivalsAndDeparturesTemplate() {
    super(true);
  }

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

  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack valueStack = context.getValueStack();
    PhoneArrivalsAndDeparturesModel model = (PhoneArrivalsAndDeparturesModel) valueStack.findValue("model");
    StopsWithArrivalsAndDeparturesBean result = model.getResult();

    buildPredictedArrivalsTemplate(result.getArrivalsAndDepartures());

    addMessage(Messages.ARRIVAL_INFO_ON_SPECIFIC_ROUTE);
    AgiActionName byRouteAction = addActionWithParameterFromMatch("1(\\d+)#",
        "/stop/arrivalsAndDeparturesForRoute", "route", 1);
    byRouteAction.putParam("model", model);

    addMessage(Messages.ARRIVAL_INFO_BOOKMARK_THIS_LOCATION);
    AgiActionName bookmarkAction = addAction("2", "/stop/bookmark");
    bookmarkAction.putParam("stops", result.getStops());

    addMessage(Messages.ARRIVAL_INFO_RETURN_TO_MAIN_MENU);
    addAction("3", "/index");

    addAction("(#|[04-9]|1.*\\*)", "/repeat");

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
  }

  protected void buildPredictedArrivalsTemplate(
      List<ArrivalAndDepartureBean> arrivals) {

    if (arrivals.isEmpty()) {
      addMessage(Messages.ARRIVAL_INFO_NO_SCHEDULED_ARRIVALS);
    }

    Collections.sort(arrivals, new ArrivalAndDepartureComparator());

    long now = SystemTime.currentTimeMillis();

    for (ArrivalAndDepartureBean adb : arrivals) {

      TripBean trip = adb.getTrip();
      RouteBean route = trip.getRoute();

      addMessage(Messages.ROUTE);

      String routeNumber = RoutePresenter.getNameForRoute(route);
      addText(_routeNumberPronunciation.modify(routeNumber));

      String headsign = trip.getTripHeadsign();
      if (headsign != null) {
        addMessage(Messages.TO);

        String destination = _destinationPronunciation.modify(headsign);
        addText(destination);
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

      if (TransitDataConstants.STATUS_REROUTE.equals(adb.getStatus()))
        addText("but is currently on adverse weather re-route.");
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
  }
}
