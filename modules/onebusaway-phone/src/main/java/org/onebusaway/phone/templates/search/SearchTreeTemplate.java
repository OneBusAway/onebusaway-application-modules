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
package org.onebusaway.phone.templates.search;

import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.StopBean;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.traditionalcake.probablecalls.AgiActionName;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AgiTemplateId("/search/tree")
public class SearchTreeTemplate extends AbstractAgiTemplate {

  private static final long serialVersionUID = 1L;

  private TextModification _destinationPronunciation;

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack vs = context.getValueStack();
    Object route = vs.findValue("routeNumber");
    List<Integer> selection = (List<Integer>) vs.findValue("selection");
    StopSelectionBean tree = (StopSelectionBean) vs.findValue("names");

    List<StopBean> stops = tree.getStops();

    if (stops.size() > 7) {

      Set<NameBean> names = tree.getNames();

      int index = 1;

      if (9 < names.size() && names.size() < 100)
        index = 10;

      if (99 < names.size())
        index = 100;

      int zeroIndex = 0;

      for (NameBean name : names) {

        String key = Integer.toString(index++);
        AgiActionName action = addAction(key, "/search/tree");
        action.putParam("routeNumber", route);
        action.putParam("selection", extendSelection(selection, zeroIndex++));

        if (tree.hasStop(name))
          handleStop(tree.getStop(name), key);
        else
          handleName(name, key);
      }

    } else {

      int index = 0;

      for (StopBean stop : stops) {
        String key = Integer.toString(index + 1);
        AgiActionName action = addAction(key, "/search/stop");
        action.putParam("stop", stop);

        handleStop(stop, key);
        index++;
      }
    }

    addAction("(#|0|.+\\*)", "/repeat");

    addMessage(Messages.HOW_TO_GO_BACK);
    addAction("\\*", "/back");

    addMessage(Messages.TO_REPEAT);
  }

  private List<Integer> extendSelection(List<Integer> selection, int index) {
    List<Integer> extended = new ArrayList<Integer>(selection.size() + 1);
    extended.addAll(selection);
    extended.add(index);
    return extended;
  }

  private void handleName(NameBean name, String key) {

    String type = name.getType();

    if (SelectionNameTypes.DESTINATION.equals(type)) {
      addMessage(Messages.FOR_TRAVEL_TO);
      addText(_destinationPronunciation.modify(name.getName()));
    } else if (SelectionNameTypes.REGION_IN.equals(type)) {
      addMessage(Messages.FOR_STOPS_IN);
      addText(_destinationPronunciation.modify(name.getName()));
    } else if (SelectionNameTypes.REGION_AFTER.equals(type)) {
      addMessage(Messages.FOR_STOPS_AFTER);
      addText(_destinationPronunciation.modify(name.getName()));
    } else if (SelectionNameTypes.REGION_BEFORE.equals(type)) {
      addMessage(Messages.FOR_STOPS_BEFORE);
      addText(_destinationPronunciation.modify(name.getName()));
    } else if (SelectionNameTypes.REGION_BETWEEN.equals(type)) {
      addMessage(Messages.FOR_STOPS_BETWEEN);
      addText(_destinationPronunciation.modify(name.getName(0)));
      addMessage(Messages.AND);
      addText(_destinationPronunciation.modify(name.getName(1)));
    } else if (SelectionNameTypes.MAIN_STREET.equals(type)) {
      addMessage(Messages.FOR_STOPS_ALONG);
      addText(_destinationPronunciation.modify(name.getName(0)));
    } else if (SelectionNameTypes.CROSS_STREET.equals(type)) {
      addMessage(Messages.FOR_STOPS_AT);
      addText(_destinationPronunciation.modify(name.getName(0)));
    } else if (SelectionNameTypes.STOP_DESCRIPTION.equals(type)) {
      addMessage(Messages.FOR_STOPS_NUMBER);
      addText(_destinationPronunciation.modify(name.getName(0)));
    }

    addMessage(Messages.PLEASE_PRESS);
    addText(key);
  }

  private void handleStop(StopBean stop, String key) {
    addMessage(Messages.FOR);
    addMessage(_destinationPronunciation.modify(stop.getName()));
    addMessage(Messages.PLEASE_PRESS);
    addText(key);
  }
}
