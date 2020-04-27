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
package org.onebusaway.twilio.actions;

import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.users.services.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Results ({
  @Result(name="registration", location="/registration", type="chain"),
  @Result(name="repeat", location="welcome", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="help", location="index", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="contact", location="contact", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="stops-index", location="stops/index", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="find-your-stop", location="find-your-stop", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="bookmarks-index", location="bookmarks/index", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="bookmarks-manage", location="bookmarks/manage", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="most-recent-stop", location="stops/most-recent-stop", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="search-index", location="search/index", type="redirectAction", params={"From", "${phoneNumber}"})
})
public class WelcomeAction extends TwilioSupport implements SessionAware {
  
  private static final long serialVersionUID = 1L;
	private static Logger _log = LoggerFactory.getLogger(WelcomeAction.class);
    
	private CurrentUserService _currentUserService;
	private Map sessionMap;
	    
  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }
  
	public void setSession(Map map) {
	  this.sessionMap = map;
	}
		
  @Override
  public String execute() {
    logUserInteraction();
    Integer navState = (Integer)sessionMap.get("navState");
    _log.debug("execute - navState: " + navState);
    // Added the welcomeJustDisplayed flag for the case where a user hangs up with navState = DO_ROUTING, and
    // then calls again before the session times out.
    String welcomeJustDisplayed = (String)sessionMap.get("welcomeJustDisplayed");
    _log.debug("execute - welcomeJustDisplayed: " + welcomeJustDisplayed +  ", navState=" + navState);
    if (navState != null) {
		if ("0".equals(getInput()) && navState == DO_ROUTING && "false".equals(welcomeJustDisplayed)) {
			// under these circumstances go to contact page not help
			_log.debug("custom contact");
			return "contact";
		}
	}
		if (navState == null || welcomeJustDisplayed == null || welcomeJustDisplayed.equals("false")) {
			navState = DISPLAY_DATA;
		}

		
		if (navState == DISPLAY_DATA) {
			if ( _currentUserService.hasPhoneNumberRegistration() ) {
				return "registration";
			}
			sessionMap.put("navState", DO_ROUTING);
			sessionMap.put("welcomeJustDisplayed", "true");
			return SUCCESS;
		} else {	// Process input and route to the appropriate action.
			sessionMap.put("navState", DISPLAY_DATA);
			sessionMap.put("welcomeJustDisplayed", "false");
			int key = 0;
			if (getInput() != null) {
        if (getInput().length() > 1 || !Character.isDigit(getInput().charAt(0))) {
          return "repeat";
        } else {
          key = Integer.parseInt(getInput());
        }
      }
			_log.debug("key: " + key);
			switch(key) {
				case 0: return "help";
				case 1: return "stops-index";
				case 2: return "find-your-stop";
				case 3: return "bookmarks-index";
				case 4: return "bookmarks-manage";
				case 5: return "most-recent-stop";
				case 6: return "search-index";
				default: return "help";
			}
		}
  }
}

