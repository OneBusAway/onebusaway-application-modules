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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.model.StopSelectionBean;
import org.onebusaway.presentation.services.StopSelectionService;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.StopBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.onebusaway.twilio.actions.TwilioSupport;

@Results ({
  @Result(name="success", type="redirectAction", params={"namespace", "/search", "actionName", "stops-for-route-navigation","From", "${phoneNumber}"}),
	@Result (name="stopFound", location="stop-found", type="chain"),
  @Result(name="arrivals-and-departures", type="chain",
                params={"namespace", "/stops", "actionName", "arrivals-and-departures-for-stop-id"}),

})
public class NavigateDownAction extends TwilioSupport implements SessionAware {
  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(IndexAction.class);

  private StopSelectionService _stopSelectionService;

  private Map sessionMap;
  private NavigationBean _navigation;

  private int _index;

  private StopBean _stop;

  private List<String> _stopIds;

  @Autowired
  public void setStopSelectionService(StopSelectionService stopSelectionService) {
    _stopSelectionService = stopSelectionService;
  }

  public void setNavigation(NavigationBean navigation) {
    _navigation = navigation;
  }

  public NavigationBean getNavigation() {
    return _navigation;
  }

  public void setSession(Map map) {
	  this.sessionMap = map;
  }
		
  public void setIndex(int index) {
    _index = index;
  }

  public StopBean getStop() {
    return _stop;
  }

  // for chaining from index
  public List<String> getStopIds() {
    return _stopIds;
  }

  @Override
  public String execute() throws Exception {
  	  
  	 _index = (Integer)sessionMap.get("index");

	_log.debug("in NavigateDownAction with input: " + getInput() + ", index: " + _index);
	Integer navState = (Integer)sessionMap.get("navState");
	_log.debug("NavigateDownAction:navState: " + navState);
	_log.debug("NavigateDownAction:_index: " + _index);

  if (sessionMap.containsKey("stop")) {
    // we've been here before, go to real-time
    StopBean stop = (StopBean) sessionMap.get("stop");
    _stopIds = Arrays.asList(stop.getId());
    ArrivalsAndDeparturesModel model = new ArrivalsAndDeparturesModel();
    model.setStopIds(_stopIds);
    sessionMap.put("_model", model);
    sessionMap.put("navState", DISPLAY_DATA);
    return "arrivals-and-departures";
  }

	if (_navigation == null) {
		_log.debug("NavigateDownAction:navigation bean is null");
		_navigation = (NavigationBean) sessionMap.get("navigation");
		if (_navigation == null) {
			_log.debug("NavigateDownAction:navigation bean is still null after sessionMap.get()");
		}
	}
	
    //_navigation = new NavigationBean(_navigation);

    List<Integer> indices = new ArrayList<Integer>(
        _navigation.getSelectionIndices());
    indices.add(_index);

    StopSelectionBean selection = _stopSelectionService.getSelectedStops(
        _navigation.getStopsForRoute(), indices);

    List<NameBean> names = new ArrayList<NameBean>(selection.getNames());

    _navigation.setSelectionIndices(indices);
    _navigation.setCurrentIndex(0);
    _navigation.setSelection(selection);
    _navigation.setNames(names);

    if (selection.hasStop()) {
      _stop = selection.getStop();
      sessionMap.put("navState", new Integer(DISPLAY_DATA));
      return "stopFound";
    }

    sessionMap.remove("navState");
    sessionMap.put("navigation", _navigation);
    return SUCCESS;
  }
}
