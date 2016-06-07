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
  @Result(name="back", location="index", type="chain")
	//	  @Result(name="success", location="stop-found", type="chain")
})
public class StopsForRouteNavigationAction extends TwilioSupport implements SessionAware {
	  private static final long serialVersionUID = 1L;
	  private static Logger _log = LoggerFactory.getLogger(StopsForRouteNavigationAction.class);

	  private TextModification _destinationPronunciation;
	  private SessionManager _sessionManager;
	  private Map sessionMap;
	  private int index;
	  private NavigationBean _navigation;
	  
	  @Autowired
	  public void setDestinationPronunciation(
	      @Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
	    _destinationPronunciation = destinationPronunciation;
	  }
	  
	  @Autowired
	  public void setSessionManager(SessionManager sessionManager) {
		_sessionManager = sessionManager;
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

	  @Override
	  public String execute() throws Exception {
		_log.debug("in StopsForRouteNavigationAction with input=" + getInput());
		
		//Get navigation bean from session
		_navigation = (NavigationBean)sessionMap.get("navigation");
		index = _navigation.getCurrentIndex();
		
		if (_navigation == null) {
			_log.debug("StopsForRouteNavigationAction 1: _navigation is null"); 
			_navigation = (NavigationBean)sessionMap.get("navigation");
			sessionMap.put("navigation", _navigation);
			if (_navigation == null) {
				_log.debug("StopsForRouteNavigationAction 1: _navigation is STILL null"); 
			} else {
				_log.debug("StopsForRouteNavigationAction 1: _navigation.indices: " + _navigation.getSelectionIndices());
				_log.debug("StopsForRouteNavigationAction 1: _navigation.stops: " + _navigation.getStopsForRoute());
			}
		}
		
		Integer navState = (Integer)sessionMap.get("navState");
		if (navState == null) {
			navState = DISPLAY_DATA;
		}
		_log.debug("StopsForRouteNavigationAction:navState: " + navState);
		
		
		//if (getInput() != null) {
		if (navState == DISPLAY_DATA) {
			//buildPredictedArrivals(result.getArrivalsAndDepartures());
			buildStopsList();
			_log.debug("in StopsForRouteNavigationAction with input " + getInput()); 
			
			//clearInput();
			sessionMap.put("navState", new Integer(DO_ROUTING));
			sessionMap.put("navigation", _navigation);
			setNextAction("search/stops-for-route-navigation");
			//return "stops-for-route-navigation";
			return INPUT;
		} else {	// Process input and route to the appropriate action.
			_log.debug("StopsForRouteNavigationAction: DO_ROUTING for index: " + index); 
			//sessionMap.put("navState", new Integer(DISPLAY_DATA));
			//sessionMap.put("navigation", _navigation);
			if (_navigation == null) {
				_log.debug("StopsForRouteNavigationAction 2: _navigation is null"); 
				_navigation = (NavigationBean)sessionMap.get("navigation");
				if (_navigation == null) {
					_log.debug("StopsForRouteNavigationAction 2: _navigation is STILL null"); 
				} else {
					_log.debug("StopsForRouteNavigationAction 2: _navigation.indices: " + _navigation.getSelectionIndices());
					_log.debug("StopsForRouteNavigationAction 2: _navigation.stops: " + _navigation.getStopsForRoute());
				}
			}
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
				index++;
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("7")) {
				index += 10;
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("6")) {
				index--;
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else if (keysPressed.equals("9")) {
				index -= 10;
				_log.debug("Chaining to transfer-to, index = " + index);
				sessionMap.put("index", index);
				return "navigate-to";
			} else return INPUT;
			//setNextAction("search/stop-found");
			//return INPUT;
		}
	}
	
	//protected void buildPredictedArrivals(List<ArrivalAndDepartureBean> arrivals) {
	protected void buildStopsList() {
		ActionContext context = ActionContext.getContext();
	    ValueStack vs = context.getValueStack();

	    //NavigationBean navigation = (NavigationBean) vs.findValue("navigation");
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

	        NameBean name = names.get(index);
	        handleName(name, key);

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
		    addText(key);
		  }
}
