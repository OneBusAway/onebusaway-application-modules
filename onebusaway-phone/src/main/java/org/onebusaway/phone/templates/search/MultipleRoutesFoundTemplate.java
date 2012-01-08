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
package org.onebusaway.phone.templates.search;

import java.util.List;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;
import org.onebusaway.transit_data.model.RouteBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/search/multipleRoutesFound")
public class MultipleRoutesFoundTemplate extends AbstractAgiTemplate {

  private TextModification _routeNumberPronunciation;
  
  @Autowired
  public void setRouteNumberPronunciation(
      @Qualifier("routeNumberPronunciation") TextModification routeNumberPronunciation) {
    _routeNumberPronunciation = routeNumberPronunciation;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack vs = context.getValueStack();
    List<RouteBean> routes = (List<RouteBean>) vs.findValue("routes");
    
    int index = 1;
    
    addMessage(Messages.MULTIPLE_ROUTES_WERE_FOUND);
    
    for( RouteBean route : routes) {
      
      addMessage(Messages.FOR);
      addMessage(Messages.ROUTE);
      
      String routeNumber = route.getShortName();
      addText(_routeNumberPronunciation.modify(routeNumber));
      
      addMessage(Messages.OPERATED_BY);
      addText(route.getAgency().getName());
      
      addMessage(Messages.PLEASE_PRESS);
      
      String key = Integer.toString(index++);
      addText(key);
      AgiActionName action = addAction(key,"/search/tree");
      action.putParam("route", route);
    }

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
  }
}
