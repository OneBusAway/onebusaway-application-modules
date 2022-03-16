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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.exceptions.InvalidSelectionServiceException;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

@Results({
	  @Result(name="back", location="index", type="chain"),
		// redirect to prevent loop
	  @Result(name="route-selected", type="redirectAction", params={"From", "${phoneNumber}", "namespace", "/search", "actionName", "stops-for-route-navigation"}),
})
public class MultipleRoutesFoundAction extends TwilioSupport implements SessionAware {
		private static final long serialVersionUID = 1L;
		private static Logger _log = LoggerFactory.getLogger(MultipleRoutesFoundAction.class);

		private TextModification _routeNumberPronunciation;
	  private Map sessionMap;
		private NavigationBean _navigation;
		private StopSelectionService _stopSelectionService;

		private RouteBean _route;
	  
	  @Autowired
	  public void setRouteNumberPronunciation(
	      @Qualifier("routeNumberPronunciation") TextModification routeNumberPronunciation) {
	    _routeNumberPronunciation = routeNumberPronunciation;
	  }

		@Autowired
		public void setStopSelectionService(StopSelectionService stopSelectionService) {
			_stopSelectionService = stopSelectionService;
		}

		public void setSession(Map map) {
	  	  this.sessionMap = map;
	  }
		
	  public void setRoute(RouteBean route) {
	    _route = route;
	  }

	  public RouteBean getRoute() {
	    return _route;
	  }

	  @Override
	  public String execute() throws Exception {
		Integer navState = (Integer)sessionMap.get("navState");
		if (navState == null) {
			navState = DISPLAY_DATA;
		}
		_log.debug("MultipleRoutesFound, navState: " + navState);

		if (navState == DISPLAY_DATA) {		
			ActionContext context = ActionContext.getContext();
			ValueStack vs = context.getValueStack();
			List<RouteBean> routes = (List<RouteBean>) vs.findValue("routes");
			if (routes == null) {
				routes = (List<RouteBean>) sessionMap.get("routes");
			}
			int index = 1;
			
			addMessage(Messages.MULTIPLE_ROUTES_WERE_FOUND);
			
			// Keep a map of key options and their corresponding route beans
			Map<Integer, RouteBean> keyMapping = new HashMap<Integer, RouteBean>();
			for( RouteBean route : routes) {
			  
			  addMessage(Messages.FOR);
			  addMessage(Messages.ROUTE);
			  
			  String routeNumber = route.getShortName();
			  addText(_routeNumberPronunciation.modify(routeNumber));
			  
			  addMessage(Messages.OPERATED_BY);
			  addText(route.getAgency().getName());
			  
			  addMessage(Messages.PLEASE_PRESS);
			  
			  String key = Integer.toString(index++);
			  addText(key);
			  
			  keyMapping.put(new Integer(index-1), route);
			}
	
			addMessage(Messages.HOW_TO_GO_BACK);
			//addAction("\\*", "/back");
	
			addMessage(Messages.TO_REPEAT);
			
			sessionMap.put("keyMapping", keyMapping);
			navState = DO_ROUTING;
			sessionMap.put("navState", navState);
			setNextAction("search/multiple-routes-found");
			return INPUT; // get the selection
		} else {	// Do the routing, matching the key pressed with the correct route bean.
			_log.debug("Handling selection of choice of routes.");
			// Handle "back" request ('*' key pressed)
			if (PREVIOUS_MENU_ITEM.equals(getInput())) {
				return "back";
			}
			// input is the route direction selection
			// redirect to its use
			int key = Integer.parseInt(getInput());
			Map<Integer, RouteBean> keyMapping = (Map<Integer, RouteBean>)sessionMap.get("keyMapping");
			_route = (RouteBean)keyMapping.get(key);
			navState = DISPLAY_DATA;
			sessionMap.put("navState", navState);
			// instead of chaining use session map
			sessionMap.remove("routes");
			sessionMap.put("route", _route);
			List<Integer> indices = new ArrayList<>();

			try {
				_navigation = (NavigationBean)sessionMap.get("navigation");

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

				}

				StopSelectionBean selection = _stopSelectionService.getSelectedStops(
								_navigation.getStopsForRoute(), indices);
				List<NameBean> names = new ArrayList<NameBean>(selection.getNames());
				_navigation.setSelection(selection);
				_navigation.setNames(names);
			} catch (InvalidSelectionServiceException isse) {
				_log.error("invalid selection");
				return INPUT;
			}

			_navigation.setSelectionIndices(indices);
			_navigation.setCurrentIndex(0);

			if (_route == null) {
				_log.error("route not found for seleciton=" + getInput() + " and keyMap=" + keyMapping.keySet());
				return INPUT;
			}
			sessionMap.put("navState", new Integer(DISPLAY_DATA));
			//_log.debug("Key " + key + " entered for route: " + _route.getId());
			return "route-selected";
		}
	}
}
