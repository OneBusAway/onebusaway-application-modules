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

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Results({
	  @Result(name="back", type="redirectAction", params={"namespace", "/", "actionName", "index"}),
	  @Result(name="route-for-name", location="route-for-name", type="chain"),
	  @Result(name="search-for-code", location="stop-for-code", type="chain"),
		@Result(name="stops-for-route-navigation", location="stops-for-route-navigation", type="chain"),
		@Result(name="arrivals-and-departures", type="chain",
								params={"namespace", "/stops", "actionName", "arrivals-and-departures-for-stop-id"}),

})
public class IndexAction extends TwilioSupport {

	  private static final long serialVersionUID = 1L;
	  private static Logger _log = LoggerFactory.getLogger(IndexAction.class);
	  private String _searchCode;
	  private String _routeName;
	  
	  public String getSearchCode() {
	    return _searchCode;
	  }

		public void setDigits(String digits) {
			String[] digitArray = {digits};
			_parameters.put(INPUT_KEY, digitArray);
		}
	  
	  public void setSearchCode(String searchCode) {
		  _searchCode = searchCode;
	  }
	  
	  public String getRouteName() {
		  return _routeName;
	  }
		  
	  public void setRouteName(String routeName) {
		  _routeName = routeName;
	  }
	  
	  @Override
	  public String execute() throws Exception {
	    _log.debug("in search index with input=" + getInput());

			if (sessionMap.containsKey("stop")) {
				// request meant for realtime
				return "arrivals-and-departures";
			}

			if (sessionMap.containsKey("navigation")) {
				// request meant for navigation
				return "stops-for-route-navigation";
			}

	    if (getInput() != null) {
	      if (PREVIOUS_MENU_ITEM.equals(getInput())) {
	        return "back";
	      }	      
	      setSearchCode(getInput());
	      _log.debug("search.IndexAction:searchCode: " + _searchCode);
	      if (_searchCode.matches("([1-9][0-9]*)")) {
	    	  _routeName = _searchCode;
	    	  return "route-for-name";
	      }	      
	      return "search-for-code";
	    } else {
	      setNextAction("search/index");
	    }	    
	    return INPUT;
	  }
}
