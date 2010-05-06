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
package org.onebusaway.where.phone.templates.stops;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onebusaway.where.phone.PronunciationStrategy;
import org.onebusaway.where.phone.templates.Messages;
import org.onebusaway.where.services.StatusService;
import org.onebusaway.where.web.common.client.model.DepartureBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.traditionalcake.probablecalls.AgiActionName;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/stop/byId")
public class ByIdTemplate extends AbstractAgiTemplate {

  private static final long serialVersionUID = 1L;

  private PronunciationStrategy _strategy;

  public ByIdTemplate() {
    super(true);
  }

  @Autowired
  public void setPronunciationStrategy(PronunciationStrategy strategy) {
    _strategy = strategy;
  }

  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack valueStack = context.getValueStack();
    Object stopId = valueStack.findValue("stopId");

    List<DepartureBean> arrivals = buildPredictedArrivalsTemplate(context);

    addMessage(Messages.ARRIVAL_INFO_ON_SPECIFIC_ROUTE);
    AgiActionName byRouteAction = addActionWithParameterFromMatch("1(\\d+)#",
        "/stop/predictionsByRoute", "route", 1);
    byRouteAction.putParam("arrivals", arrivals);
    byRouteAction.putParam("stopId", stopId);

    addMessage(Messages.ARRIVAL_INFO_BOOKMARK_THIS_LOCATION);
    AgiActionName bookmarkAction = addAction("2", "/stop/bookmark");
    bookmarkAction.putParam("stopId", stopId);

    addMessage(Messages.ARRIVAL_INFO_RETURN_TO_MAIN_MENU);
    addAction("3", "/index");

    addAction("(#|[04-9]|1.*\\*)", "/repeat");

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
  }

  @SuppressWarnings("unchecked")
  protected List<DepartureBean> buildPredictedArrivalsTemplate(
      ActionContext context) {

    ValueStack valueStack = context.getValueStack();
    List<DepartureBean> arrivals = (List<DepartureBean>) valueStack.findValue("arrivals");

    if (arrivals.isEmpty()) {
      addMessage(Messages.ARRIVAL_INFO_NO_SCHEDULED_ARRIVALS);
    }

    Collections.sort(arrivals);

    long now = System.currentTimeMillis();

    for (DepartureBean sat : arrivals) {

      addMessage(Messages.ROUTE);

      String routeNumber = sat.getRoute();
      addText(_strategy.getRouteNumberAsText(routeNumber));

      addMessage(Messages.TO);

      addText(_strategy.getDestinationAsText(sat.getDestination()));

      if( StatusService.STATUS_CANCELLED.equals(sat.getStatus()) ) {
        addText("is currently not in service");
        continue;
      }
      
      long t = sat.getBestTime();
      boolean isPrediction = sat.hasPredictedTime();

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
      
      if( StatusService.STATUS_REROUTE.equals(sat.getStatus()) )
        addText("but is currently on adverse weather re-route.");
    }

    addMessage(Messages.ARRIVAL_INFO_DISCLAIMER);
    return arrivals;
  }
}
