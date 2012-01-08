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

import java.util.Arrays;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;
import org.onebusaway.transit_data.model.StopBean;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@AgiTemplateId("/search/stopFound")
public class StopFoundTemplate extends AbstractAgiTemplate {

  private TextModification _destinationPronunciation;

  private TextModification _directionPronunciation;

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  @Autowired
  public void setDirectionPronunciation(
      @Qualifier("directionPronunciation") TextModification directionPronunciation) {
    _directionPronunciation = directionPronunciation;
  }

  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack vs = context.getValueStack();

    StopBean stop = (StopBean) vs.findValue("stop");

    addMessage(Messages.THE_STOP_NUMBER_FOR);

    addText(_destinationPronunciation.modify(stop.getName()));

    String direction = _directionPronunciation.modify(stop.getDirection());
    addMessage(Messages.DIRECTION_BOUND, direction);

    addText(Messages.IS);
    addText(stop.getCode());

    addMessage(Messages.STOP_FOUND_ARRIVAL_INFO);
    AgiActionName arrivalInfoAction = addAction("1", "/stop/arrivalsAndDeparturesForStopId");
    arrivalInfoAction.putParam("stopIds", Arrays.asList(stop.getId()));

    addMessage(Messages.STOP_FOUND_BOOKMARK_THIS_LOCATION);
    AgiActionName bookmarkAction = addAction("2", "/stop/bookmark");
    bookmarkAction.putParam("stop", stop);

    addMessage(Messages.STOP_FOUND_RETURN_TO_MAIN_MENU);
    addAction("3", "/index");

    addAction("[04-9]", "/repeat");

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
  }
}
