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
package org.onebusaway.twilio.actions.search;

import java.util.List;
import java.util.Map;

import org.onebusaway.presentation.services.SelectionNameTypes;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.onebusaway.twilio.services.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@Results({
	@Result(name="navigate-down", location="navigate-down", type="chain"),
	@Result(name="navigate-to", location="navigate-to", type="chain"),
	@Result (name="stopFound", location="stop-found", type="chain"),
	@Result(name="stops-for-route-navigation", location="stops-for-route-navigation", type="chain"),
  @Result(name="back", location="index", type="chain"),
	@Result(name="stops-for-route", location="stops-for-route", type="redirectAction", params={"namespace", "/search", "actionName", "stops-for-route","From", "${phoneNumber}"}),
	@Result(name="directions-for-route", location="directions-for-route", type="redirectAction", params={"namespace", "/search", "actionName", "directions-for-route","From", "${phoneNumber}"})
})
public class StopsForRouteNavigationAction extends AbstractNavigationAction {
	  private static final long serialVersionUID = 1L;
	  private static Logger _log = LoggerFactory.getLogger(StopsForRouteNavigationAction.class);

	  @Override
	  public String execute() throws Exception {
		_log.debug("in StopsForRouteNavigationAction with input=" + getInput());
		
		//Get navigation bean from session
		_navigation = (NavigationBean)sessionMap.get("navigation");
		if (_navigation == null) {
			return INPUT;
		}
		index = _navigation.getCurrentIndex();

		Integer navState = (Integer)sessionMap.get("navState");
		if (navState == null) {
			navState = DISPLAY_DATA;
		}
		_log.debug("StopsForRouteNavigationAction:navState: " + navState);
		
		
		if (navState.equals(DISPLAY_DATA)) {
			buildStopsList();
			_log.debug("in StopsForRouteNavigationAction with input " + getInput()); 
			
			sessionMap.put("navState", new Integer(DO_ROUTING));
			sessionMap.put("navigation", _navigation);
			setNextAction("search/stops-for-route-navigation");

			if ("destination".equals(_navigation.getSelection().getType())) {
				// moved this logic to directions-for-route to simplify this action
				return "directions-for-route";
			}

			return INPUT;
		} else {	// Process input and route to the appropriate action.
			_log.debug("StopsForRouteNavigationAction: DO_ROUTING for index: " + index); 
			sessionMap.put("navigation", _navigation);
      if (PREVIOUS_MENU_ITEM.equals(getInput())) {
        return "back";
      }	      
      
			String keysPressed = getInput();      
			if (keysPressed.equals("1")) {         
				_log.debug("Chaining to transfer-down, index = " + index);
				sessionMap.put("index", index);
				return "navigate-down";
			} else if (keysPressed.equals("4")) {
				index--;  // back one stop
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("7")) {
				index -= 10; // skip back
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("6")) {
				index++; // forward one stop
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("9")) {
				index += 10; // skip forward
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("8")) {
				_log.debug("repeating");
				buildStopsList();
				return INPUT;
			} else if (keysPressed.equals('*')) {
				_log.debug("chaining to index to go back");
				return "back";
			} else {
				// invalid input
				addMessage(Messages.TO_REPEAT);
				addMessage(Messages.HOW_TO_GO_BACK);
				return INPUT;
			}
		}
	}
	
}
