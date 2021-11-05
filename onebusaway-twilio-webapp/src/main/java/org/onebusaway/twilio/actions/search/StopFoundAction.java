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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.ResultPath;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.services.text.TextModification;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;

//@ResultPath("/stops")
@Results({
//  @Result(name="arrivals-and-departures", location="arrivals-and-departures-for-stop-id", type="chain",
	  @Result(name="back", location="index", type="chain"),
  @Result(name="arrivals-and-departures", type="chain",
  	  params={"namespace", "/stops", "actionName", "arrivals-and-departures-for-stop-id"}),
  @Result(name="bookmark-stop", type="chain",
      params={"namespace", "/bookmarks", "actionName", "bookmark-stop"}),
  	  
//  @Result(name="arrivals-and-departures", location="/stops/arrivals-and-departures-for-stop-id", type="chain"),
//	  @Result(name="success", location="stops-for-route-navigation", type="chain")
})
public class StopFoundAction extends TwilioSupport implements SessionAware {
	private static final long serialVersionUID = 1L;
	private static Logger _log = LoggerFactory.getLogger(StopFoundAction.class);
	
	private List<String> _stopIds;
	private TextModification _destinationPronunciation;
	private TextModification _directionPronunciation;
	private Map sessionMap;
	  
	public List<String> getStopIds() {
		return _stopIds;
	}

	@Autowired
	public void setDestinationPronunciation(
		@Qualifier("destinationPronunciation") TextModification destinationPronunciation) {
		_destinationPronunciation = destinationPronunciation;
	}

	@Autowired
	public void setDirectionPronunciation(
		@Qualifier("directionPronunciation") TextModification directionPronunciation) {
		_directionPronunciation = directionPronunciation;
	}

	public void setSession(Map map) {
	  this.sessionMap = map;
	}

	@Override
	public String execute() throws Exception {
		Integer navState = (Integer)sessionMap.get("navState");
		if (navState == null) {
			navState = DISPLAY_DATA;
		}
		_log.debug("StopsForRouteNavigationAction:navState: " + navState);


		if (navState == DISPLAY_DATA) {
	
			ActionContext context = ActionContext.getContext();
			ValueStack vs = context.getValueStack();	
			StopBean stop = (StopBean) vs.findValue("stop");
			sessionMap.put("stop", stop);
			
			addMessage(Messages.THE_STOP_NUMBER_FOR);
	
			addText(_destinationPronunciation.modify(stop.getName()));
	
			String stopDir = stop.getDirection();
			if (stopDir != null && stopDir.length() > 0) {
				String direction = _directionPronunciation.modify(stopDir);
				addMessage(Messages.DIRECTION_BOUND, direction);
			}
	
			addMessage(Messages.IS);
			addText(stop.getCode() + ".");
	
			addMessage(Messages.STOP_FOUND_ARRIVAL_INFO);
			//AgiActionName arrivalInfoAction = addAction("1", "/stop/arrivalsAndDeparturesForStopId");
			//arrivalInfoAction.putParam("stopIds", Arrays.asList(stop.getId()));
	
			addMessage(Messages.STOP_FOUND_BOOKMARK_THIS_LOCATION);
			//AgiActionName bookmarkAction = addAction("2", "/stop/bookmark");
			//bookmarkAction.putParam("stop", stop);
	
			addMessage(Messages.STOP_FOUND_RETURN_TO_MAIN_MENU);
			//addAction("3", "/index");
	
			//addAction("[04-9]", "/repeat");
	
			addMessage(Messages.HOW_TO_GO_BACK);
			//addAction("\\*", "/back");
	
			addMessage(Messages.TO_REPEAT);
			
			sessionMap.put("navState", new Integer(DO_ROUTING));			  
			return INPUT;
		} else {
      if (PREVIOUS_MENU_ITEM.equals(getInput())) {
        return "back";
      } else if ("1".equals(getInput())) {
				StopBean stop = (StopBean)sessionMap.get("stop");
				_stopIds = Arrays.asList(stop.getId());
				return "arrivals-and-departures";
			} else if ("2".equals(getInput())) {
			  StopBean stop = (StopBean)sessionMap.get("stop");
        _stopIds = Arrays.asList(stop.getId());
        return "bookmark-stop";
			}
			return SUCCESS;
		}
	}
}
