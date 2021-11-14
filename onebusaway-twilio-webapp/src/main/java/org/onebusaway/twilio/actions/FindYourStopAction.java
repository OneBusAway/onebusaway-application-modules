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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Results({
  @Result(name="back", type="redirectAction", params={"namespace", "/", "actionName", "index","From", "${phoneNumber}",}),
	@Result(name="stops-index", type="redirectAction", params={"namespace", "/stops", "actionName", "index","From", "${phoneNumber}",}),
  @Result(name="search-index", type="redirectAction", params={"namespace", "/search", "actionName", "index","From", "${phoneNumber}",})
})
public class FindYourStopAction extends TwilioSupport implements SessionAware {

  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(FindYourStopAction.class);
  
	private Map sessionMap;
	
	public void setSession(Map map) {
	  this.sessionMap = map;
	}
		
  @Override
  public String execute() throws Exception {
    _log.debug("in HelpAction with input=" + getInput());
        
      	Integer navState = (Integer)sessionMap.get("navState");
		if (navState == null) {
			navState = DISPLAY_DATA;
		}

	  if (navState == DISPLAY_DATA) {
			sessionMap.put("navState", DO_ROUTING);
	  	  return SUCCESS;
    } else {	// Process input and route to the appropriate action.
    	_log.debug("Help: input: " + getInput());
		sessionMap.put("navState", DISPLAY_DATA);
      if (PREVIOUS_MENU_ITEM.equals(getInput())) {
        return "back";
      } else if ("0".equals(getInput())) {
	        clearNextAction();
	        return "help";
	    } else if ("1".equals(getInput())) {
	        clearNextAction();
	        return "stops-index";
	    } else if ("2".equals(getInput())) {
	    	clearNextAction();
	    	return "search-index";
	    } else if ("*".equals(getInput())) {
	    	return "index";
	    } else {
	    	return "";
	    }
    }
  }

}