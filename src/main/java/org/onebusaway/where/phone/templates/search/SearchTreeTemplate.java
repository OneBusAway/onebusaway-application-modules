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
package org.onebusaway.where.phone.templates.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.onebusaway.where.model.SelectionNameTypes;
import org.onebusaway.where.phone.PronunciationStrategy;
import org.onebusaway.where.phone.templates.Messages;
import org.onebusaway.where.web.common.client.model.NameBean;
import org.onebusaway.where.web.common.client.model.NameTreeBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.traditionalcake.probablecalls.AgiActionName;
import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/search/tree")
public class SearchTreeTemplate extends AbstractAgiTemplate {

  private static final long serialVersionUID = 1L;

  private PronunciationStrategy _strategy;

  @Autowired
  public void setPronunciationStrategy(PronunciationStrategy strategy) {
    _strategy = strategy;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack vs = context.getValueStack();
    Object route = vs.findValue("routeNumber");
    List<Integer> selection = (List<Integer>) vs.findValue("selection");
    NameTreeBean tree = (NameTreeBean) vs.findValue("names");

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
        AgiActionName action = addAction(key, "/search/tree");
        action.putParam("routeNumber", route);
        action.putParam("selection", extendSelection(selection, index));

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
      addText(_strategy.getDestinationAsText(name.getName()));
    } else if (SelectionNameTypes.REGION_IN.equals(type)) {
      addMessage(Messages.FOR_STOPS_IN);
      addText(_strategy.getDestinationAsText(name.getName()));
    } else if (SelectionNameTypes.REGION_AFTER.equals(type)) {
      addMessage(Messages.FOR_STOPS_AFTER);
      addText(_strategy.getDestinationAsText(name.getName()));
    } else if (SelectionNameTypes.REGION_BEFORE.equals(type)) {
      addMessage(Messages.FOR_STOPS_BEFORE);
      addText(_strategy.getDestinationAsText(name.getName()));
    } else if (SelectionNameTypes.REGION_BETWEEN.equals(type)) {
      addMessage(Messages.FOR_STOPS_BETWEEN);
      addText(_strategy.getDestinationAsText(name.getName(0)));
      addMessage(Messages.AND);
      addText(_strategy.getDestinationAsText(name.getName(1)));
    } else if (SelectionNameTypes.MAIN_STREET.equals(type)) {
      addMessage(Messages.FOR_STOPS_ALONG);
      addText(_strategy.getDestinationAsText(name.getName(0)));
    } else if (SelectionNameTypes.CROSS_STREET.equals(type)) {
      addMessage(Messages.FOR_STOPS_AT);
      addText(_strategy.getDestinationAsText(name.getName(0)));
    } else if (SelectionNameTypes.STOP_DESCRIPTION.equals(type)) {
      addMessage(Messages.FOR_STOPS_NUMBER);
      addText(_strategy.getDestinationAsText(name.getName(0)));
    }

    addMessage(Messages.PLEASE_PRESS);
    addText(key);
  }

  private void handleStop(StopBean stop, String key) {
    addMessage(Messages.FOR);
    addMessage(_strategy.getStopNameAsText(stop.getName()));
    addMessage(Messages.PLEASE_PRESS);
    addText(key);
  }
}
