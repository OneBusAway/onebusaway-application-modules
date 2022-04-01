/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
package org.onebusaway.twilio.actions.search;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

/**
 * Base action for building a list of stops and navigation from it.
 */
public class AbstractNavigationAction extends TwilioSupport implements SessionAware {

  private static Logger _log = LoggerFactory.getLogger(AbstractNavigationAction.class);
  protected TextModification _destinationPronunciation;
  protected NavigationBean _navigation;
  protected Map sessionMap;
  protected int index;

  @Autowired
  public void setDestinationPronunciation(
          @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
    _destinationPronunciation = destinationPronunciation;
  }

  public void setSession(Map map) {
    this.sessionMap = map;
  }

  public int getIndex() { return this.index; }
  public void setIndex(int index) { this.index = index; }

  public void setNavigation(NavigationBean navigation) {
    _navigation = navigation;
  }

  public NavigationBean getNavigation() {
    return _navigation;
  }

  protected void buildStopsList() {
    ActionContext context = ActionContext.getContext();
    ValueStack vs = context.getValueStack();

    NavigationBean navigation = (NavigationBean)sessionMap.get("navigation");
    List<NameBean> names = navigation.getNames();
    index = navigation.getCurrentIndex();
    _log.debug("StopsForRoute.buildStopsList: index: " + index);
    if (index < 0)
      index = 0;

    /**
     * We always listen for the key-press for the previous name in case it takes
     * the user a second to press
     */
    _log.debug("in StopsForRouteNavigationAction, index = " + index + ", names.size = " + names.size());

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

      //AgiActionName action = setNextAction("/search/navigate-to");
      //action.putParam("navigation", navigation);
      //action.putParam("index", 0);
      //action.setExcludeFromHistory(true);

      // Add an extra pause so the user has a chance to make a selection from
      // the previous entry
      addMessage("<Pause length=\"1\"/>");
      //addPause(1000);

      addMessage(Messages.TO_REPEAT);

    } else {

      String key = addNavigationSelectionActionForIndex(navigation, index);

      for (int j = index; j < names.size(); j++) {
        NameBean name = names.get(j);
        if (_navigation.getSelection().getType().equals("stop_name")) {
          // navigate down option is 1
          handleName(name, "1");
        } else {
          // otherwise use index
          handleName(name, "" + (j + 1));
        }
        String type = _navigation.getSelection().getType();
        if ("stop_name".equals(type)) {
          // don't list all the stop names, allow navigation instead
          break;
        }
      }

      addNavigateToAction(navigation, "4", first(index - 1));
      addNavigateToAction(navigation, "6", index + 1);
      addNavigateToAction(navigation, "7", first(index - 10));
      addNavigateToAction(navigation, "9", index + 10);

      //AgiActionName action = setNextAction("/search/navigate-to");
      //action.putParam("navigation", navigation);
      //action.putParam("index", index + 1);
      //action.setExcludeFromHistory(true);
      //setNextAction("navigate-to");
    }

    sessionMap.put("navigation", navigation);
    //addAction("\\*", "/back");

  }


  private int first(int i) {
    if (i < 0)
      i = 0;
    return i;
  }

  private void addNavigateToAction(NavigationBean navigation, String key,
                                   int index) {
//		    AgiActionName action = addAction(key, "/search/navigate-to");
//		    action.putParam("navigation", navigation);
//		    action.putParam("index", index);
//		    action.setExcludeFromHistory(true);
  }

  private String addNavigationSelectionActionForIndex(
          NavigationBean navigation, int index) {
    int keyIndex = (index % 2) + 1;

    String key = Integer.toString(keyIndex);
    //AgiActionName action = addAction(key, "/search/navigate-down");
    //action.putParam("navigation", navigation);
    //action.putParam("index", index);
    return key;
  }


  private void handleName(NameBean name, String key) {

    String type = name.getType();

    if (SelectionNameTypes.DESTINATION.equals(type)) {
      addMessage(Messages.FOR_TRAVEL_FROM);
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
    addText(key + "."); // todo test this as it reverts a previous bug fix

    // Add additional navigation instructions for STOP_NAME type.
    if (SelectionNameTypes.STOP_NAME.equals(type)) {
      addMessage(Messages.NAVIGATION_ACTIONS);
    }
  }


}
