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
package org.onebusaway.twilio.actions.stops;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.SearchQueryBean.EQueryType;
import org.onebusaway.twilio.actions.Messages;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Results({
  @Result(name="success", location="arrivals-and-departures-for-stop-id", type="chain"),
  @Result(name="multipleStopsFound", location="multiple-stops-found", type="chain"),
  @Result(name="noStopsFound", type="redirectAction", params={"From", "${phoneNumber}", "namespace", "/", "actionName", "message-and-back"})
})
public class StopForCodeAction extends TwilioSupport implements SessionAware {
  
  private Map sessionMap;
  private static Logger _log = LoggerFactory.getLogger(StopForCodeAction.class);
  private String _stopCode;

  private List<String> _stopIds;

  private List<StopBean> _stops;
  
  private StopBean _stop;

  public void setStopCode(String stopCode) {
    _stopCode = stopCode;
  }
  
  public void setStop(StopBean stop) {
	_stop = stop;
  }

  public List<String> getStopIds() {
    return _stopIds;
  }

  public List<StopBean> getStops() {
    return _stops;
  }
                              
  public void setSession(Map map) {
	  this.sessionMap = map;
	}
		
public String execute() throws Exception {
    _log.info("in stop for code");
    CoordinateBounds bounds = getDefaultSearchArea();
    if (bounds == null)
      return NEEDS_DEFAULT_SEARCH_LOCATION;

    if (_stopCode == null || _stopCode.length() == 0)
      return INPUT;
    
    if(_stop != null) {
    	_stops = Arrays.asList(_stop);
    }
    else{
    	_log.info("searching on stopCode=" + _stopCode);
	    SearchQueryBean searchQuery = new SearchQueryBean();
	    searchQuery.setBounds(bounds);
	    searchQuery.setMaxCount(5);
	    searchQuery.setType(EQueryType.BOUNDS_OR_CLOSEST);
	    searchQuery.setQuery(_stopCode);

	    StopsBean stopsBean = _transitDataService.getStops(searchQuery);

	    _stops = stopsBean.getStops();
    }
   
    logUserInteraction("query", _stopCode);

    if (_stops.size() == 0) {
      sessionMap.put("messageFromAction", getText(Messages.NO_STOPS_WERE_FOUND));
      sessionMap.put("backAction", "stops-index");
      return "noStopsFound";
    } else if (_stops.size() == 1) {
      StopBean stop = _stops.get(0);
      _stopIds = Arrays.asList(stop.getId());
      return SUCCESS;
    } else {
      sessionMap.put("stops", _stops);
      return "multipleStopsFound";
    }
  }
}
