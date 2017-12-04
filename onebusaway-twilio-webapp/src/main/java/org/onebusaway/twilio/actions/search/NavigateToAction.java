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

import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Results({
	  @Result(name="success", location="stops-for-route-navigation", type="chain")
})
public class NavigateToAction extends TwilioSupport implements SessionAware {

  private static final long serialVersionUID = 1L;
  private static Logger _log = LoggerFactory.getLogger(IndexAction.class);

  private Map sessionMap;
  private NavigationBean _navigation;

  private int _index;

  public void setSession(Map map) {
	  this.sessionMap = map;
  }
		
  public void setNavigation(NavigationBean navigation) {
    _navigation = navigation;
  }

  public NavigationBean getNavigation() {
    return _navigation;
  }

  public void setIndex(int index) {
    _index = index;
  }

  @Override
  public String execute() throws Exception {
  	  
  	_index = (Integer)sessionMap.get("index");

  	_log.debug("in NavigateToAction with input: " + getInput() + ", index: " + _index); 
  		
  	_navigation.setCurrentIndex(_index);

    sessionMap.put("navState", new Integer(DISPLAY_DATA)); //Get input
    sessionMap.put("navigation", _navigation);
    return SUCCESS;
  }

}
