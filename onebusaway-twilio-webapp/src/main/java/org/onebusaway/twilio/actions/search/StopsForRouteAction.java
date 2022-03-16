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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
  @Result(name="success", location="stops-for-route-navigation", type="chain"),
	@Result(name="navigation", type="redirectAction", params={"namespace", "/search", "actionName", "stops-for-route-navigation","From", "${phoneNumber}", "Digits", "${input}"}),
	@Result (name="stopFound", location="stop-found", type="chain")
})
public class StopsForRouteAction extends TwilioSupport implements SessionAware {
	  private static final long serialVersionUID = 1L;
	  private static Logger _log = LoggerFactory.getLogger(IndexAction.class);

	  private StopSelectionService _stopSelectionService;

	  private RouteBean _route;

	  private NavigationBean _navigation;

	  private StopBean _stop;
	  private Map sessionMap;

	  @Autowired
	  public void setStopSelectionService(StopSelectionService stopSelectionService) {
	    _stopSelectionService = stopSelectionService;
	  }

	  public void setRoute(RouteBean route) {
	    _route = route;
	  }

	  public RouteBean getRoute() {
	    return _route;
	  }

	  public void setNavigation(NavigationBean navigation) {
	    _navigation = navigation;
	  }

	  public NavigationBean getNavigation() {
	    return _navigation;
	  }

	  public StopBean getStop() {
	    return _stop;
	  }

	  public void setSession(Map map) {
	  	  this.sessionMap = map;
	  }

	  @Override
	  public String execute() throws Exception {

	  	/* Need to check this for testing from the web */
		Integer navState = (Integer)sessionMap.get("navState");
		if (navState == null) {
			_log.debug("StopsForRouteAction:navState is null");
		} else {
			_log.debug("StopsForRouteAction:navState is NOT null, resetting to DISPLAY_DATA");
			navState = DISPLAY_DATA;
			sessionMap.put("navState", new Integer(navState));
		}

			if (_navigation == null) {
				_navigation = (NavigationBean) sessionMap.get("navigation");
			}

			if (_route == null) {
			_route = (RouteBean) sessionMap.remove("route");
		}

			if (_route == null)
			throw new IllegalStateException("In StopsForRoute without route");

		_log.debug("in StopsForRoute with input: " + getInput() + " route.getId: " + _route.getId()); 


		// no previous state, populate the navigation object
		if (_navigation == null) {
			StopsForRouteBean stopsForRoute = _transitDataService.getStopsForRoute(_route.getId());
			List<Integer> selectionIndices = Collections.emptyList();
			StopSelectionBean selection = _stopSelectionService.getSelectedStops(
							stopsForRoute, selectionIndices);
			List<NameBean> names = new ArrayList<NameBean>(selection.getNames());

			_navigation = new NavigationBean();
			_navigation.setRoute(_route);
			_navigation.setStopsForRoute(stopsForRoute);
			_navigation.setSelectionIndices(selectionIndices);
			_navigation.setCurrentIndex(0);
			_navigation.setSelection(selection);
			_navigation.setNames(names);
			// Set navigation bean in session
			sessionMap.put("navigation", _navigation);
		} else {
			// navigation is present, use it
			sessionMap.put("navState", new Integer(DO_ROUTING));
			return "navigation";
		}


			// if a stop has been selected redirect to it
	    if (_navigation.getSelection().hasStop()) {
	      _log.debug("in StopsForRoute with input=" + getInput());
	      _stop = _navigation.getSelection().getStop();
	      return "stopFound";
	    }
    
	    return SUCCESS;
	  }

}
