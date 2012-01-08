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

import org.onebusaway.phone.actions.search.NavigationBean;
import org.onebusaway.phone.templates.Messages;
import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.probablecalls.AgiActionName;
import org.onebusaway.probablecalls.agitemplates.AbstractAgiTemplate;
import org.onebusaway.probablecalls.agitemplates.AgiTemplateId;
import org.onebusaway.transit_data.model.NameBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@AgiTemplateId("/search/navigation")
public class StopsForRouteNavigationTemplate extends AbstractAgiTemplate {

  private TextModification _destinationPronunciation;

  @Autowired
  public void setDestinationPronunciation(
      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  @Override
  public void buildTemplate(ActionContext context) {

    ValueStack vs = context.getValueStack();

    NavigationBean navigation = (NavigationBean) vs.findValue("navigation");
    List<NameBean> names = navigation.getNames();
    int index = navigation.getCurrentIndex();
    if (index < 0)
      index = 0;

    /**
     * We always listen for the key-press for the previous name in case it takes
     * the user a second to press
     */
    if (index > 0)
      addNavigationSelectionActionForIndex(navigation, index - 1);

    /**
     * If we're at the first entry and there is a second, we allow the user to
     * jump ahead
     */
    if (index == 0 && names.size() > 1) {
      addNavigationSelectionActionForIndex(navigation, index + 1);
    }

    if (index >= names.size()) {

      AgiActionName action = setNextAction("/search/navigate-to");
      action.putParam("navigation", navigation);
      action.putParam("index", 0);
      action.setExcludeFromHistory(true);

      // Add an extra pause so the user has a chance to make a selection from
      // the previous entry
      addPause(1000);

      addMessage(Messages.TO_REPEAT);

    } else {

      String key = addNavigationSelectionActionForIndex(navigation, index);

      NameBean name = names.get(index);
      handleName(name, key);

      addNavigateToAction(navigation, "4", first(index - 1));
      addNavigateToAction(navigation, "6", index + 1);
      addNavigateToAction(navigation, "7", first(index - 10));
      addNavigateToAction(navigation, "9", index + 10);

      AgiActionName action = setNextAction("/search/navigate-to");
      action.putParam("navigation", navigation);
      action.putParam("index", index + 1);
      action.setExcludeFromHistory(true);
    }

    addAction("\\*", "/back");
  }

  private int first(int i) {
    if (i < 0)
      i = 0;
    return i;
  }

  private void addNavigateToAction(NavigationBean navigation, String key,
      int index) {
    AgiActionName action = addAction(key, "/search/navigate-to");
    action.putParam("navigation", navigation);
    action.putParam("index", index);
    action.setExcludeFromHistory(true);
  }

  private String addNavigationSelectionActionForIndex(
      NavigationBean navigation, int index) {
    int keyIndex = (index % 2) + 1;

    String key = Integer.toString(keyIndex);
    AgiActionName action = addAction(key, "/search/navigate-down");
    action.putParam("navigation", navigation);
    action.putParam("index", index);
    return key;
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
    } else if (SelectionNameTypes.STOP_NAME.equals(type)) {
      addMessage(Messages.FOR);
      addText(_destinationPronunciation.modify(name.getName(0)));
    } else if (SelectionNameTypes.STOP_DESCRIPTION.equals(type)) {
      addMessage(Messages.FOR_STOPS_NUMBER);
      addText(_destinationPronunciation.modify(name.getName(0)));
    }

    addMessage(Messages.PLEASE_PRESS);
    addText(key);
  }
}
