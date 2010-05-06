/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.phone.templates.stops;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.impl.ArrivalAndDepartureComparator;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopWithArrivalsAndDeparturesBean;
import org.onebusaway.transit_data.model.TransitDataConstants;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.traditionalcake.probablecalls.AgiActionName;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@AgiTemplateId("/stop/arrivalsAndDepartures")
public class ArrivalsAndDeparturesTemplate extends AbstractAgiTemplate {

  private static final long serialVersionUID = 1L;

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
    StopWithArrivalsAndDeparturesBean model = (StopWithArrivalsAndDeparturesBean) valueStack.findValue("model");
    StopBean stop = model.getStop();

    buildPredictedArrivalsTemplate(model.getArrivalsAndDepartures());

    addMessage(Messages.ARRIVAL_INFO_ON_SPECIFIC_ROUTE);
    AgiActionName byRouteAction = addActionWithParameterFromMatch("1(\\d+)#",
        "/stop/arrivalsAndDeparturesForRoute", "route", 1);
    byRouteAction.putParam("model", model);

    addMessage(Messages.ARRIVAL_INFO_BOOKMARK_THIS_LOCATION);
    AgiActionName bookmarkAction = addAction("2", "/stop/bookmark");
    bookmarkAction.putParam("stop", stop);

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

    long now = System.currentTimeMillis();

    for (ArrivalAndDepartureBean sat : arrivals) {

      addMessage(Messages.ROUTE);

      String routeNumber = sat.getRouteShortName();
      addText(_routeNumberPronunciation.modify(routeNumber));

      addMessage(Messages.TO);

      String destination = _destinationPronunciation.modify(sat.getTripHeadsign());
      addText(destination);

      if (TransitDataConstants.STATUS_CANCELLED.equals(sat.getStatus())) {
        addText("is currently not in service");
        continue;
      }

      long t = sat.computeBestDepartureTime();
      boolean isPrediction = sat.hasPredictedDepartureTime();

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

      if (TransitDataConstants.STATUS_REROUTE.equals(sat.getStatus()))
        addText("but is currently on adverse weather re-route.");
    }

    addMessage(Messages.ARRIVAL_INFO_DISCLAIMER);
  }
}
